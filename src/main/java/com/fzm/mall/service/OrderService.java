package com.fzm.mall.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fzm.mall.constant.ChainConstant;
import com.fzm.mall.constant.SystemConstant;
import com.fzm.mall.constant.enums.ChainEnum;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.GoodsEnum;
import com.fzm.mall.constant.enums.OrderEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.*;
import com.fzm.mall.entity.properties.ChainPaymentProperties;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.entity.queryobject.back.MOrderExpressPageQO;
import com.fzm.mall.entity.queryobject.back.MOrderInfoPageQO;
import com.fzm.mall.mapper.*;
import com.fzm.mall.redis.RedisIdComponent;
import com.fzm.mall.redis.cache.PriceCacheComponent;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.util.TransferUtils;
import com.fzm.mall.third.chain.util.Web3jUtils;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderService {
    private final GoodsSpuMapper goodsSpuMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final GoodsWhiteMapper goodsWhiteMapper;
    private final OrderLimitMapper orderLimitMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderExpressMapper orderExpressMapper;
    private final OrderBlindBoxMapper orderBlindBoxMapper;
    private final SysHashUsedMapper sysHashUsedMapper;
    private final RedisIdComponent redisIdComponent;
    private final UserBackAssetService userBackAssetService;
    private final UserAdminService userAdminService;
    private final ContractProperties contractProperties;
    private final ChainContractMapper chainContractMapper;
    private final PriceCacheComponent priceCacheComponent;
    private final ChainPaymentProperties chainPaymentProperties;

    @Transactional(rollbackFor = Exception.class)
    public String order(String skuId, Integer buyNum, String buyerAddress, Integer expressType, String expressDetailJson) {
        long nowTimestamp = TimeUtils.nowTimestamp();

        GoodsSkuDO skuDO = goodsSkuMapper.getBySkuId(skuId);
        AssertUtils.isNotNull(skuDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(skuDO.getStatus() == GoodsEnum.MintStatusEnum.mint_success.getStatus(), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(skuDO.getStock() >= buyNum, ResponseEnum.insufficient_goods_stock);
        AssertUtils.isTrue(buyNum % skuDO.getOrderPack() == 0, ResponseEnum.invalid_parameter);

        GoodsSpuDO spuDO = goodsSpuMapper.getByGoodsId(skuDO.getGoodsId());
        AssertUtils.isTrue(spuDO.getStatus() == GoodsEnum.SpuStatusEnum.mint_sell_ing.getStatus(), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(spuDO.getSaleTime() <= nowTimestamp, ResponseEnum.invalid_parameter);

        // 普通商品、盲盒商品
        AssertUtils.isTrue(GoodsEnum.SpuTypeEnum.canBuy(spuDO.getType()), ResponseEnum.invalid_parameter);
        // 不能买盲盒奖品
        if (spuDO.getType() == GoodsEnum.SpuTypeEnum.blind_box.getType()) {
            AssertUtils.isTrue(skuDO.getBlindBoxType() == CommonEnum.BoolEnum.YES.getStatus(), ResponseEnum.invalid_parameter);
        }

        // 限购
        addOrderLimit(spuDO, skuDO, buyNum, buyerAddress, nowTimestamp);

        // 增加销量
        int i = goodsSpuMapper.addSalesByGoodsId(spuDO.getGoodsId(), buyNum);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        i = goodsSkuMapper.addSalesBySkuId(skuId, buyNum);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);

        // 保存订单
        String orderId = redisIdComponent.nextOrderId();

        OrderInfoDO orderInfoDO = new OrderInfoDO();
        orderInfoDO.setOrderId(orderId);
        orderInfoDO.setSellerAddress(spuDO.getAddress());
        orderInfoDO.setBuyerAddress(buyerAddress);
        orderInfoDO.setGoodsId(skuDO.getGoodsId());
        orderInfoDO.setSkuId(skuId);
        orderInfoDO.setTokenIdJson("[]");
        orderInfoDO.setNum(buyNum);
        orderInfoDO.setPrice(skuDO.getPrice());
        orderInfoDO.setAmount(skuDO.getPrice().multiply(BigDecimal.valueOf(buyNum)));
        orderInfoDO.setPayChain(ChainEnum.TypeEnum.BTY.getType());
        orderInfoDO.setPayCoin(ChainEnum.TypeEnum.BTY.getCoinEnum().getType());
        orderInfoDO.setPayPrice(BigDecimal.ZERO);
        orderInfoDO.setPayAmount(BigDecimal.ZERO);
        orderInfoDO.setPayHash("");
        orderInfoDO.setStatus(OrderEnum.InfoStatusEnum.wait.getStatus());
        orderInfoDO.setOrderTime(nowTimestamp);
        orderInfoDO.setExpireTime(nowTimestamp + SystemConstant.Time.order_expire_time_gap);
        orderInfoDO.setTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
        orderInfoDO.setTxHash("");
        orderInfoDO.setTxNote("");
        orderInfoDO.setUnfreezeStatus(OrderEnum.UnfreezeStatusEnum.none.getStatus());
        orderInfoDO.setUnfreezeTime(0L);
        orderInfoDO.setClientIp(ThreadInfo.getInfo().getClientIp());
        orderInfoDO.setUserAgent(ThreadInfo.getInfo().getUserAgent());
        orderInfoDO.setDeleteStatus(CommonEnum.BoolEnum.NO.getStatus());
        orderInfoDO.setRefundNote("[]");
        orderInfoDO.setRefundApplyTxHash("");
        orderInfoDO.setRefundPrice(BigDecimal.ZERO);
        orderInfoDO.setRefundAmount(BigDecimal.ZERO);
        orderInfoDO.setRefundTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
        orderInfoDO.setRefundTxHash("");
        orderInfoDO.setRefundTxNote("");
        orderInfoDO.setExpressType(expressType);
        orderInfoDO.setExpressJson(expressDetailJson);

        i = orderInfoMapper.insert(orderInfoDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);

        return orderId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void pay(String orderId, ChainEnum.TypeEnum chainTypeEnum, ChainPaymentProperties.Detail detail, BigDecimal payPrice, String txHash, String address) {
        OrderInfoDO infoDO = orderInfoMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(infoDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(infoDO.getBuyerAddress().equals(address), ResponseEnum.invalid_parameter);

        UserAdminDO sellerDO = userAdminService.getByAddress(infoDO.getSellerAddress());

        BigDecimal verifyAmount = infoDO.getAmount().divide(payPrice, detail.getCalcDecimals(), RoundingMode.UP);

        TransferUtils.PayResult payResult = TransferUtils.payment(detail, txHash, address, sellerDO.getInsideAddress(), verifyAmount);
        // 真实的交易哈希和数量
        txHash = payResult.hash();
        BigDecimal payAmount = payResult.amount();

        String selectTxHash = sysHashUsedMapper.getByTxHash(txHash);
        AssertUtils.isBlank(selectTxHash, ResponseEnum.transaction_hash_already_used);

        long nowTimestamp = TimeUtils.nowTimestamp();
        int i;
        try {
            i = sysHashUsedMapper.insert(txHash, nowTimestamp);
        } catch (Exception e) {
            i = 0;
        }
        AssertUtils.isTrue(i == 1, ResponseEnum.transaction_hash_already_used);

        // 修改订单状态
        OrderInfoDO updateInfoDO = new OrderInfoDO();
        updateInfoDO.setOrderId(orderId);
        updateInfoDO.setPayChain(chainTypeEnum.getType());
        updateInfoDO.setPayCoin(chainTypeEnum.getCoinEnum().getType());
        updateInfoDO.setPayPrice(payPrice);
        updateInfoDO.setPayAmount(payAmount);
        updateInfoDO.setPayHash(txHash);
        updateInfoDO.setPayTime(nowTimestamp);
        updateInfoDO.setStatus(OrderEnum.InfoStatusEnum.paid.getStatus());
        updateInfoDO.setOriginalStatus(OrderEnum.InfoStatusEnum.wait.getStatus());
        updateInfoDO.setUnfreezeStatus(OrderEnum.UnfreezeStatusEnum.freeze.getStatus());
        updateInfoDO.setOldUnfreezeStatus(OrderEnum.UnfreezeStatusEnum.none.getStatus());
        updateInfoDO.setUnfreezeTime(nowTimestamp + SystemConstant.Time.unfreeze_time_gap);

        i = orderInfoMapper.updateByOrderId(updateInfoDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);

        // 冻结
        JSONObject extendData = new JSONObject();
        extendData.put("orderId", infoDO.getOrderId());
        userBackAssetService.orderFreeze(infoDO.getSellerAddress(), chainTypeEnum.getCoinEnum().getType(), payAmount, extendData);

        // tokenIds
        GoodsSkuDO skuDO = goodsSkuMapper.getBySkuId(infoDO.getSkuId());
        List<Long> tokenIds = redisIdComponent.getTokenIds(skuDO.getSkuId(), skuDO.getTokenPrefix(), infoDO.getNum());

        // 修改订单tokenIds
        updateInfoDO = new OrderInfoDO();
        updateInfoDO.setOrderId(orderId);
        updateInfoDO.setTokenIdJson(JSON.toJSONString(tokenIds));

        i = orderInfoMapper.updateByOrderId(updateInfoDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelInfo(String orderId, String address) {
        OrderInfoDO infoDO = orderInfoMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(infoDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(infoDO.getBuyerAddress().equals(address), ResponseEnum.invalid_parameter);

        expireInfo(infoDO, OrderEnum.InfoStatusEnum.cancel);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteInfo(String orderId, String address) {
        OrderInfoDO infoDO = orderInfoMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(infoDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(infoDO.getBuyerAddress().equals(address), ResponseEnum.invalid_parameter);

        OrderInfoDO updateInfoDO = new OrderInfoDO();
        updateInfoDO.setOrderId(orderId);
        updateInfoDO.setDeleteStatus(CommonEnum.BoolEnum.YES.getStatus());
        int i = orderInfoMapper.updateByOrderId(updateInfoDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    @Transactional(rollbackFor = Exception.class)
    public void expireInfo(OrderInfoDO infoDO, OrderEnum.InfoStatusEnum toStatueEnum) {
        //
        OrderInfoDO updateInfoDO = new OrderInfoDO();
        updateInfoDO.setOrderId(infoDO.getOrderId());
        updateInfoDO.setStatus(toStatueEnum.getStatus());
        updateInfoDO.setOriginalStatus(OrderEnum.InfoStatusEnum.wait.getStatus());
        if (toStatueEnum == OrderEnum.InfoStatusEnum.cancel) {
            updateInfoDO.setCancelTime(TimeUtils.nowTimestamp());
        }

        int i = orderInfoMapper.updateByOrderId(updateInfoDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);

        // 减少用户购买数量
        subOrderLimit(infoDO.getGoodsId(), infoDO.getSkuId(), infoDO.getNum(), infoDO.getBuyerAddress());

        // 恢复销量
        i = goodsSpuMapper.subSalesByGoodsId(infoDO.getGoodsId(), infoDO.getNum());
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        i = goodsSkuMapper.subSalesBySkuId(infoDO.getSkuId(), infoDO.getNum());
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfreezeInfo(OrderInfoDO infoDO) {
        OrderInfoDO updateInfoDO = new OrderInfoDO();
        updateInfoDO.setOrderId(infoDO.getOrderId());
        updateInfoDO.setUnfreezeStatus(OrderEnum.UnfreezeStatusEnum.unfreeze.getStatus());
        updateInfoDO.setOldUnfreezeStatus(OrderEnum.UnfreezeStatusEnum.freeze.getStatus());

        int ok = orderInfoMapper.updateByOrderId(updateInfoDO);
        AssertUtils.isTrue(ok == 1, ResponseEnum.too_many_requests_plz_try_again_later);

        // 解冻
        JSONObject extendData = new JSONObject();
        extendData.put("orderId", infoDO.getOrderId());
        userBackAssetService.orderUnfreeze(infoDO.getSellerAddress(), infoDO.getPayCoin(), infoDO.getPayAmount(), extendData);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundApplyInfo(String orderId, String txHash, String note, String address) {
        OrderInfoDO infoDO = orderInfoMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(infoDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(infoDO.getBuyerAddress().equals(address), ResponseEnum.invalid_parameter);

        AssertUtils.isTrue(infoDO.getStatus() == OrderEnum.InfoStatusEnum.paid.getStatus(), ResponseEnum.invalid_parameter);

        long timestamp = TimeUtils.nowTimestamp();
        AssertUtils.isTrue(timestamp - infoDO.getOrderTime() < SystemConstant.Time.order_can_refund_time_gap, ResponseEnum.invalid_parameter);

        UserAdminDO sellerDO = userAdminService.getByAddress(infoDO.getSellerAddress());
        ChainContractDO contractDO = chainContractMapper.getByCtId(ChainConstant.contract_default_contract_id);
        TransferUtils.Erc1155Result erc1155Result = TransferUtils.erc1155(contractProperties.getChainUrl(), contractDO.getAddress(), txHash, address, sellerDO.getInsideAddress(), JSON.parseArray(infoDO.getTokenIdJson(), Long.class));

        List<OrderInfoRefundNoteDO> refundNoteAry = getInfoRefundNoteAry("user", note, infoDO);

        OrderInfoDO updateDO = new OrderInfoDO();
        updateDO.setOrderId(orderId);
        updateDO.setRefundNote(JSON.toJSONString(refundNoteAry));
        updateDO.setRefundApplyTxHash(erc1155Result.hash());
        updateDO.setStatus(OrderEnum.InfoStatusEnum.refund_apply.getStatus());
        updateDO.setOriginalStatus(OrderEnum.InfoStatusEnum.paid.getStatus());

        int i = orderInfoMapper.updateByOrderId(updateDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundAuditInfo(String orderId, CommonEnum.BoolEnum statusEnum, String note, String address) {
        OrderInfoDO infoDO = orderInfoMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(infoDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(infoDO.getSellerAddress().equals(address), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(infoDO.getStatus() == OrderEnum.InfoStatusEnum.refund_apply.getStatus(), ResponseEnum.invalid_parameter);

        List<OrderInfoRefundNoteDO> refundNoteAry = getInfoRefundNoteAry("merchant", note, infoDO);

        OrderInfoDO updateDO = new OrderInfoDO();
        updateDO.setOrderId(orderId);
        updateDO.setOriginalStatus(OrderEnum.InfoStatusEnum.refund_apply.getStatus());
        updateDO.setRefundNote(JSON.toJSONString(refundNoteAry));
        if (statusEnum == CommonEnum.BoolEnum.NO) {
            updateDO.setStatus(OrderEnum.InfoStatusEnum.refund_fail.getStatus());
        } else {
            ChainEnum.CoinEnum coinEnum = ChainEnum.CoinEnum.exist(infoDO.getPayCoin());
            ChainPaymentProperties.Detail properties = chainPaymentProperties.getByCoinEnum(coinEnum);
            AssertUtils.isNotNull(properties, ResponseEnum.too_many_requests_plz_try_again_later);

            BigDecimal coinPrice = priceCacheComponent.getPrice(coinEnum);
            AssertUtils.isNotNull(coinPrice, ResponseEnum.too_many_requests_plz_try_again_later);
            AssertUtils.isTrue(coinPrice.compareTo(BigDecimal.ZERO) > 0, ResponseEnum.too_many_requests_plz_try_again_later);

            UserAdminDO userAdminDO = userAdminService.getByAddress(infoDO.getSellerAddress());

            BigDecimal refundAmount = infoDO.getAmount().divide(coinPrice, properties.getCalcDecimals(), RoundingMode.DOWN);
            TxResult<BigDecimal> txResult = Web3jUtils.getBalance(properties.getChainUrl(), properties.getContractAddress(), userAdminDO.getInsideAddress(), properties.getDecimals());
            AssertUtils.isTrue(txResult.getResult().compareTo(refundAmount) > 0, ResponseEnum.insufficient_balance);

            updateDO.setRefundPrice(coinPrice);
            updateDO.setRefundAmount(refundAmount);

            updateDO.setStatus(OrderEnum.InfoStatusEnum.refund_success.getStatus());

            // TODO 回收 TokenID 用于再次售卖
            // 比较麻烦目前不做，退款的TokenID直接废弃
        }

        int i = orderInfoMapper.updateByOrderId(updateDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }


    /**
     * 发货
     */
    @Transactional(rollbackFor = Exception.class)
    public void express(String orderId, String expressCode, String address) {
        OrderExpressDO expressDO = orderExpressMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getSellerAddress().equals(address), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getStatus() == OrderEnum.ExpressStatusEnum.wait.getStatus(), ResponseEnum.invalid_parameter);

        List<OrderExpressRefundNoteDO> refundNoteAry = getExpressRefundNoteAry("merchant", "express", OrderEnum.ExpressStatusEnum.express, expressDO);

        long nowTimestamp = TimeUtils.nowTimestamp();

        OrderExpressDO updateExpressDO = new OrderExpressDO();
        updateExpressDO.setOrderId(orderId);
        updateExpressDO.setExpressCode(expressCode);
        updateExpressDO.setExpressTime(nowTimestamp);
        updateExpressDO.setConfirmAutoTime(nowTimestamp + SystemConstant.Time.express_auto_confirm_time_gap);
        updateExpressDO.setRefundNote(JSON.toJSONString(refundNoteAry));
        updateExpressDO.setStatus(OrderEnum.ExpressStatusEnum.express.getStatus());
        updateExpressDO.setOriginalStatus(OrderEnum.ExpressStatusEnum.wait.getStatus());

        int i = orderExpressMapper.updateByOrderId(updateExpressDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    /**
     * 确认收货
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmExpress(String orderId, String address) {
        OrderExpressDO expressDO = orderExpressMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getBuyerAddress().equals(address), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getStatus() == OrderEnum.ExpressStatusEnum.express.getStatus(), ResponseEnum.invalid_parameter);

        List<OrderExpressRefundNoteDO> refundNoteAry = getExpressRefundNoteAry("user", "confirm", OrderEnum.ExpressStatusEnum.confirm, expressDO);

        OrderExpressDO updateExpressDO = new OrderExpressDO();
        updateExpressDO.setOrderId(orderId);
        updateExpressDO.setConfirmTime(TimeUtils.nowTimestamp());
        updateExpressDO.setRefundNote(JSON.toJSONString(refundNoteAry));
        updateExpressDO.setStatus(OrderEnum.ExpressStatusEnum.confirm.getStatus());
        updateExpressDO.setOriginalStatus(OrderEnum.ExpressStatusEnum.express.getStatus());

        int i = orderExpressMapper.updateByOrderId(updateExpressDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundApplyExpress(String orderId, String note, String address) {
        OrderExpressDO expressDO = orderExpressMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getBuyerAddress().equals(address), ResponseEnum.invalid_parameter);

        AssertUtils.isTrue(expressDO.getStatus() == OrderEnum.ExpressStatusEnum.wait.getStatus()
                || expressDO.getStatus() == OrderEnum.ExpressStatusEnum.express.getStatus()
                || expressDO.getStatus() == OrderEnum.ExpressStatusEnum.confirm.getStatus(), ResponseEnum.invalid_parameter);

        long timestamp = TimeUtils.nowTimestamp();
        AssertUtils.isTrue(timestamp - expressDO.getOrderTime() < SystemConstant.Time.express_can_refund_time_gap, ResponseEnum.invalid_parameter);

        List<OrderExpressRefundNoteDO> refundNoteAry = getExpressRefundNoteAry("user", note, OrderEnum.ExpressStatusEnum.refund_apply, expressDO);

        OrderExpressDO updateExpressDO = new OrderExpressDO();
        updateExpressDO.setOrderId(orderId);
        updateExpressDO.setRefundNote(JSON.toJSONString(refundNoteAry));
        updateExpressDO.setRefundApplyTime(timestamp);
        updateExpressDO.setStatus(OrderEnum.ExpressStatusEnum.refund_apply.getStatus());
        updateExpressDO.setOriginalStatusList(List.of(OrderEnum.ExpressStatusEnum.wait.getStatus(), OrderEnum.ExpressStatusEnum.express.getStatus(), OrderEnum.ExpressStatusEnum.confirm.getStatus()));

        int i = orderExpressMapper.updateByOrderId(updateExpressDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundApplyCancelExpress(String orderId, String address) {
        OrderExpressDO expressDO = orderExpressMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getBuyerAddress().equals(address), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getStatus() == OrderEnum.ExpressStatusEnum.refund_apply.getStatus(), ResponseEnum.invalid_parameter);

        List<OrderExpressRefundNoteDO> refundNoteAry = getExpressRefundNoteAry("user", "apply cancel", OrderEnum.ExpressStatusEnum.refund_apply, expressDO);
        int oldStatus = refundNoteAry.get(refundNoteAry.size() - 2).getFromStatus();
        refundNoteAry = getExpressRefundNoteAry("user", "apply cancel", oldStatus, expressDO);

        OrderExpressDO updateExpressDO = new OrderExpressDO();
        updateExpressDO.setOrderId(orderId);
        updateExpressDO.setRefundNote(JSON.toJSONString(refundNoteAry));
        updateExpressDO.setRefundApplyTime(TimeUtils.nowTimestamp());
        updateExpressDO.setStatus(oldStatus);
        updateExpressDO.setOriginalStatus(OrderEnum.ExpressStatusEnum.refund_apply.getStatus());

        int i = orderExpressMapper.updateByOrderId(updateExpressDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    private List<OrderInfoRefundNoteDO> getInfoRefundNoteAry(String type, String note, OrderInfoDO infoDO) {
        List<OrderInfoRefundNoteDO> noteDOS;
        try {
            noteDOS = JSON.parseArray(infoDO.getRefundNote(), OrderInfoRefundNoteDO.class);
        } catch (Exception e) {
            noteDOS = new ArrayList<>();
        }

        OrderInfoRefundNoteDO noteDO = new OrderInfoRefundNoteDO();
        noteDO.setType(type);
        noteDO.setTime(TimeUtils.nowTimestamp());
        noteDO.setNote(note);
        noteDOS.add(noteDO);

        return noteDOS;
    }

    private List<OrderExpressRefundNoteDO> getExpressRefundNoteAry(String type, String note, OrderEnum.ExpressStatusEnum toStatusEnum, OrderExpressDO expressDO) {
        return getExpressRefundNoteAry(type, note, toStatusEnum.getStatus(), expressDO);
    }

    private List<OrderExpressRefundNoteDO> getExpressRefundNoteAry(String type, String note, int toStatus, OrderExpressDO expressDO) {
        List<OrderExpressRefundNoteDO> noteDOS;
        try {
            noteDOS = JSON.parseArray(expressDO.getRefundNote(), OrderExpressRefundNoteDO.class);
        } catch (Exception e) {
            noteDOS = new ArrayList<>();
        }

        OrderExpressRefundNoteDO noteDO = new OrderExpressRefundNoteDO();
        noteDO.setType(type);
        noteDO.setTime(TimeUtils.nowTimestamp());
        noteDO.setNote(note);
        noteDO.setFromStatus(expressDO.getStatus());
        noteDO.setToStatus(toStatus);
        noteDOS.add(noteDO);

        return noteDOS;
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundAuditExpress(String orderId, CommonEnum.BoolEnum statusEnum, CommonEnum.BoolEnum needExpressEnum, String note, String address) {
        OrderExpressDO expressDO = orderExpressMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getSellerAddress().equals(address), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getStatus() == OrderEnum.ExpressStatusEnum.refund_apply.getStatus(), ResponseEnum.invalid_parameter);

        OrderExpressDO updateExpressDO = new OrderExpressDO();
        updateExpressDO.setOrderId(orderId);
        updateExpressDO.setRefundAuditTime(TimeUtils.nowTimestamp());
        updateExpressDO.setOriginalStatus(OrderEnum.ExpressStatusEnum.refund_apply.getStatus());

        List<OrderExpressRefundNoteDO> refundNoteAry;
        if (statusEnum == CommonEnum.BoolEnum.NO) {
            refundNoteAry = getExpressRefundNoteAry("merchant", note, OrderEnum.ExpressStatusEnum.refund_audit_fail, expressDO);

            int oldStatus = refundNoteAry.get(refundNoteAry.size() - 2).getFromStatus();
            if (oldStatus == OrderEnum.ExpressStatusEnum.confirm.getStatus()) {
                oldStatus = OrderEnum.ExpressStatusEnum.refund_audit_fail.getStatus();
            }
            updateExpressDO.setStatus(oldStatus);
        } else {
            if (needExpressEnum == CommonEnum.BoolEnum.NO) {
                refundNoteAry = getExpressRefundNoteAry("merchant", note, OrderEnum.ExpressStatusEnum.refund_confirm, expressDO);
                updateExpressDO.setStatus(OrderEnum.ExpressStatusEnum.refund_confirm.getStatus());
            } else {
                refundNoteAry = getExpressRefundNoteAry("merchant", note, OrderEnum.ExpressStatusEnum.refund_audit_success, expressDO);
                updateExpressDO.setStatus(OrderEnum.ExpressStatusEnum.refund_audit_success.getStatus());
            }
        }
        updateExpressDO.setRefundNote(JSON.toJSONString(refundNoteAry));

        int i = orderExpressMapper.updateByOrderId(updateExpressDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundExpress(String orderId, String expressCode, String address) {
        OrderExpressDO expressDO = orderExpressMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getBuyerAddress().equals(address), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getStatus() == OrderEnum.ExpressStatusEnum.refund_audit_success.getStatus(), ResponseEnum.invalid_parameter);

        List<OrderExpressRefundNoteDO> refundNoteAry = getExpressRefundNoteAry("user", "refund express", OrderEnum.ExpressStatusEnum.refund_ing, expressDO);

        long nowTimestamp = TimeUtils.nowTimestamp();

        OrderExpressDO updateExpressDO = new OrderExpressDO();
        updateExpressDO.setOrderId(orderId);
        updateExpressDO.setRefundNote(JSON.toJSONString(refundNoteAry));
        updateExpressDO.setRefundExpressCode(expressCode);
        updateExpressDO.setRefundConfirmAutoTime(nowTimestamp + SystemConstant.Time.refund_express_auto_time_gap);
        updateExpressDO.setStatus(OrderEnum.ExpressStatusEnum.refund_ing.getStatus());
        updateExpressDO.setOriginalStatus(OrderEnum.ExpressStatusEnum.refund_audit_success.getStatus());

        int i = orderExpressMapper.updateByOrderId(updateExpressDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundConfirm(String orderId, String address) {
        OrderExpressDO expressDO = orderExpressMapper.getByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getSellerAddress().equals(address), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getStatus() == OrderEnum.ExpressStatusEnum.refund_ing.getStatus(), ResponseEnum.invalid_parameter);

        List<OrderExpressRefundNoteDO> refundNoteAry = getExpressRefundNoteAry("merchant", "refund express", OrderEnum.ExpressStatusEnum.refund_confirm, expressDO);

        OrderExpressDO updateExpressDO = new OrderExpressDO();
        updateExpressDO.setOrderId(orderId);
        updateExpressDO.setRefundNote(JSON.toJSONString(refundNoteAry));
        updateExpressDO.setRefundConfirmTime(TimeUtils.nowTimestamp());
        updateExpressDO.setStatus(OrderEnum.ExpressStatusEnum.refund_confirm.getStatus());
        updateExpressDO.setOriginalStatus(OrderEnum.ExpressStatusEnum.refund_ing.getStatus());

        int i = orderExpressMapper.updateByOrderId(updateExpressDO);
        AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
    }

    public OrderInfoDO getInfoByOrderId(String orderId) {
        return orderInfoMapper.getByOrderId(orderId);
    }

    public OrderExpressDO getExpressByOrderId(String orderId) {
        return orderExpressMapper.getByOrderId(orderId);
    }

    public OrderBlindBoxDO getBlindBoxByTxHash(String txHash) {
        return orderBlindBoxMapper.getByTxHash(txHash);
    }

    public PageInfo<OrderInfoDO> pageInfo(MOrderInfoPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<OrderInfoDO> dos = orderInfoMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }

    public PageInfo<OrderExpressDO> pageExpress(MOrderExpressPageQO pageQO) {
        PageHelper.startPage(pageQO.getPage(), pageQO.getSize());
        List<OrderExpressDO> dos = orderExpressMapper.listByPageQO(pageQO);
        return new PageInfo<>(dos);
    }

    public List<OrderExpressDO> listExpress(MOrderExpressPageQO pageQO) {
        return orderExpressMapper.listByPageQO(pageQO);
    }


    private void addOrderLimit(GoodsSpuDO spuDO, GoodsSkuDO skuDO, Integer num, String address, long nowTimestamp) {
        // 等于0表示达到上限，其他均可购买

        int limitSku = getLimitBySaleType(spuDO, skuDO, address, nowTimestamp);
        AssertUtils.isFalse(limitSku == 0, ResponseEnum.order_quantity_exceeds_purchase_limit);
        int limitSpu = getLimitBySaleType(spuDO, null, address, nowTimestamp);
        AssertUtils.isFalse(limitSpu == 0, ResponseEnum.order_quantity_exceeds_purchase_limit);

        addUpdateLimit(spuDO.getGoodsId(), skuDO.getSkuId(), address, num, limitSku);

        String defSkuId = getDefSkuId(null);
        addUpdateLimit(spuDO.getGoodsId(), defSkuId, address, num, limitSku);
    }

    private void addUpdateLimit(String goodsId, String skuId, String address, int num, int limit) {
        OrderLimitDO limitSkuDO = orderLimitMapper.getByGoodsIdAddress(goodsId, skuId, address);
        if (limitSkuDO == null) {
            limitSkuDO = new OrderLimitDO();
            limitSkuDO.setGoodsId(goodsId);
            limitSkuDO.setSkuId(skuId);
            limitSkuDO.setAddress(address);
            limitSkuDO.setNum(num);
            int i;
            try {
                i = orderLimitMapper.insert(limitSkuDO);
            } catch (Exception e) {
                i = 0;
            }
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        } else {
            int i = orderLimitMapper.addNum(goodsId, skuId, address, num, limit);
            AssertUtils.isTrue(i == 1, ResponseEnum.order_quantity_exceeds_purchase_limit);
        }
    }

    private void subOrderLimit(String goodsId, String skuId, Integer num, String address) {
        OrderLimitDO selectSkuLimitDO = orderLimitMapper.getByGoodsIdAddress(goodsId, skuId, address);
        if (selectSkuLimitDO != null) {
            int i = orderLimitMapper.subNum(goodsId, skuId, address, num);
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }

        String defSkuId = getDefSkuId(null);
        OrderLimitDO selectSpuLimitDO = orderLimitMapper.getByGoodsIdAddress(goodsId, defSkuId, address);
        if (selectSpuLimitDO != null) {
            int i = orderLimitMapper.subNum(goodsId, defSkuId, address, num);
            AssertUtils.isTrue(i == 1, ResponseEnum.too_many_requests_plz_try_again_later);
        }
    }

    public void setOrderLimit(GoodsSpuDO spuDO, GoodsSkuDO skuDO, String address) {
        int limit = getLimitBySaleType(spuDO, skuDO, address, TimeUtils.nowTimestamp());
        if (limit > 0) {
            OrderLimitDO limitDO = orderLimitMapper.getByGoodsIdAddress(spuDO.getGoodsId(), getDefSkuId(skuDO), address);
            if (limitDO != null) {
                limit = Math.max(limit - limitDO.getNum(), 0);
            }
        }

        if (skuDO == null) {
            spuDO.setOrderLimit(limit);
        } else {
            skuDO.setOrderLimit(limit);
        }
    }

    private int getLimitBySaleType(GoodsSpuDO spuDO, GoodsSkuDO skuDO, String address, long nowTimestamp) {
        Integer saleType = spuDO.getSaleType();
        // 常规销售 或者 变为常规销售
        if (saleType == GoodsEnum.SaleTypeEnum.normal.getType() || spuDO.getSaleTimeNormal() <= nowTimestamp) {
            return skuDO == null ? spuDO.getOrderLimit() : skuDO.getOrderLimit();
        }
        // 白名单销售
        if (saleType == GoodsEnum.SaleTypeEnum.white.getType()) {
            if (StringUtils.isBlank(address)) {
                return 0;
            }
            GoodsWhiteDO whiteDO = goodsWhiteMapper.getByGoodsIdSkuIdAddress(spuDO.getGoodsId(), getDefSkuId(skuDO), address);
            return whiteDO == null ? (skuDO == null ? 0 : skuDO.getOrderLimit()) : whiteDO.getNum();
        }
        return 0;
    }

    private String getDefSkuId(GoodsSkuDO skuDO) {
        return skuDO == null ? "0" : skuDO.getSkuId();
    }
}
