package com.fzm.mall.crontab;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fzm.mall.component.ChainEventInsideComponent;
import com.fzm.mall.constant.ChainConstant;
import com.fzm.mall.constant.enums.ChainEnum;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.OrderEnum;
import com.fzm.mall.entity.dataobject.*;
import com.fzm.mall.entity.properties.ChainPaymentProperties;
import com.fzm.mall.entity.properties.ContractProperties;
import com.fzm.mall.mapper.*;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.RedisLockComponent;
import com.fzm.mall.redis.key.RedisLockKey;
import com.fzm.mall.service.OrderService;
import com.fzm.mall.third.chain.contract.ERC1155Manager;
import com.fzm.mall.third.chain.contract.FzmERC1155;
import com.fzm.mall.third.chain.entity.TxResult;
import com.fzm.mall.third.chain.entity.TxResultEnum;
import com.fzm.mall.third.chain.util.Web3jUtils;
import com.fzm.mall.util.AesUtils;
import com.fzm.mall.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderCrontab {
    private final RedisLockComponent redisLockComponent;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderExpressMapper orderExpressMapper;
    private final OrderBlindBoxMapper orderBlindBoxMapper;
    private final ChainContractMapper chainContractMapper;
    private final UserAdminMapper userAdminMapper;
    private final OrderService orderService;
    private final ContractProperties contractProperties;
    private final ChainPaymentProperties chainPaymentProperties;
    private final ChainEventInsideComponent chainEventInsideComponent;


    @Scheduled(cron = "* * * * * ?")
    public void infoExpire() {
        List<OrderInfoDO> infoDOS = orderInfoMapper.listExpired(TimeUtils.nowTimestamp(), OrderEnum.InfoStatusEnum.wait.getStatus());
        if (CollectionUtils.isEmpty(infoDOS)) {
            return;
        }

        for (OrderInfoDO infoDO : infoDOS) {
            String lockKey = String.format(RedisLockKey.Order.info_expire, infoDO.getOrderId());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("订单定时器，支付超时，订单编号：{}", infoDO.getOrderId());

            try {
                orderService.expireInfo(infoDO, OrderEnum.InfoStatusEnum.expired);
            } catch (Exception ignored) {
            }

            redisLockComponent.unlock(lockKey, lockKey);
        }
    }

    @Scheduled(cron = "* * * * * ?")
    public void infoPaidTransfer() {
        List<OrderInfoDO> infoDOS = orderInfoMapper.listTransfer(OrderEnum.InfoStatusEnum.paid.getStatus(), OrderEnum.TxStatusEnum.wait.getStatus());
        if (CollectionUtils.isEmpty(infoDOS)) {
            return;
        }


        ChainContractDO contractDO = chainContractMapper.getByCtId(ChainConstant.contract_default_contract_id);
        ERC1155Manager erc1155Manager = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), contractDO.getAddress());

        // 转币
        List<OrderInfoDO> transferInfoDOS = infoDOS.stream().filter(o -> o.getExpressType() == CommonEnum.BoolEnum.NO.getStatus()).toList();
        Map<String, List<OrderInfoDO>> groupMap = transferInfoDOS.stream().collect(Collectors.groupingBy(OrderInfoDO::getSellerAddress));
        for (Map.Entry<String, List<OrderInfoDO>> entry : groupMap.entrySet()) {
            UserAdminDO userAdminDO = userAdminMapper.getByAddress(entry.getKey());

            String lockKey = String.format(RedisLockKey.Chain.nonce_of_address, userAdminDO.getInsideAddress());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("订单定时器，支付转币，执行开始，商户地址：{}", userAdminDO.getAddress());

            List<String> orderIds = new ArrayList<>();
            List<FzmERC1155.MultiTX> multiTxs = new ArrayList<>();
            for (OrderInfoDO infoDO : entry.getValue()) {
                orderIds.add(infoDO.getOrderId());

                List<BigInteger> ids = JSON.parseArray(infoDO.getTokenIdJson(), Long.class).stream().map(BigInteger::valueOf).toList();
                multiTxs.add(new FzmERC1155.MultiTX(infoDO.getBuyerAddress(), ids));
            }
            TxResult<String> txResult = erc1155Manager.muxBatchTransferNFT(userAdminDO.getInsidePrivateKey(), multiTxs);

            OrderInfoDO updateDO = new OrderInfoDO();
            updateDO.setOrderIds(orderIds);
            updateDO.setOriginalStatus(OrderEnum.InfoStatusEnum.paid.getStatus());
            updateDO.setOriginalTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
            if (txResult.getStatus() == TxResultEnum.FAILED) {
                updateDO.setTxStatus(OrderEnum.TxStatusEnum.fail.getStatus());
                updateDO.setTxNote(txResult.getError());
            } else {
                updateDO.setTxStatus(OrderEnum.TxStatusEnum.success.getStatus());
                updateDO.setTxHash(txResult.getResult());
                updateDO.setTxNote("");
            }

            int i = orderInfoMapper.updateByOrderId(updateDO);
            if (i != orderIds.size()) {
                log.error("订单定时器，支付转币，结果保存失败：{}", updateDO);
            }

            redisLockComponent.unlock(lockKey, lockKey);

            chainEventInsideComponent.handleEvent(contractDO, txResult.getResult());

            log.info("订单定时器，支付转币，执行结束，商户地址：{}，执行结果：{}", userAdminDO.getAddress(), txResult);
        }

        // 直接提货
        List<OrderInfoDO> expressInfoDOS = infoDOS.stream().filter(o -> o.getExpressType() == CommonEnum.BoolEnum.YES.getStatus()).toList();
        for (OrderInfoDO infoDO : expressInfoDOS) {
            UserAdminDO userAdminDO = userAdminMapper.getByAddress(infoDO.getSellerAddress());

            String lockKey = String.format(RedisLockKey.Chain.nonce_of_address, userAdminDO.getInsideAddress());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            List<BigInteger> ids = JSON.parseArray(infoDO.getTokenIdJson(), Long.class).stream().map(BigInteger::valueOf).toList();

            JSONObject expressDetail = JSON.parseObject(infoDO.getExpressJson());
            expressDetail.put("expressType", infoDO.getExpressType());
            expressDetail.put("orderId", infoDO.getOrderId());
            String encode = AesUtils.encode(expressDetail.toJSONString());

            TxResult<String> txResult = erc1155Manager.expressNFTByMerchant(userAdminDO.getInsidePrivateKey(), infoDO.getBuyerAddress(), ids, encode);

            OrderInfoDO updateDO = new OrderInfoDO();
            updateDO.setOrderId(infoDO.getOrderId());
            updateDO.setOriginalStatus(OrderEnum.InfoStatusEnum.paid.getStatus());
            updateDO.setOriginalTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
            if (txResult.getStatus() == TxResultEnum.FAILED) {
                updateDO.setTxStatus(OrderEnum.TxStatusEnum.fail.getStatus());
                updateDO.setTxNote(txResult.getError());
            } else {
                updateDO.setTxStatus(OrderEnum.TxStatusEnum.success.getStatus());
                updateDO.setTxHash(txResult.getResult());
                updateDO.setTxNote("");
            }
            int ok = orderInfoMapper.updateByOrderId(updateDO);
            if (ok == 0) {
                log.error("订单定时器，支付直接提货，结果保存失败：{}", updateDO);
            }

            redisLockComponent.unlock(lockKey, lockKey);

            chainEventInsideComponent.handleEvent(contractDO, txResult.getResult());

            log.info("订单定时器，支付直接提货，执行结束，商户地址：{}，执行结果：{}", userAdminDO.getAddress(), txResult);
        }
    }

    @Scheduled(cron = "* * * * * ?")
    public void infoUnfreeze() {
        long nowTimestamp = TimeUtils.nowTimestamp();

        List<OrderInfoDO> infoDOS = orderInfoMapper.listUnfreeze(OrderEnum.UnfreezeStatusEnum.freeze.getStatus(), nowTimestamp);

        for (OrderInfoDO infoDO : infoDOS) {
            String lockKey = String.format(RedisLockKey.Order.unfreeze, infoDO.getOrderId());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            try {
                orderService.unfreezeInfo(infoDO);
                log.info("订单解冻商户金额成功，orderId: {}", infoDO.getOrderId());
            } catch (Exception e) {
                log.warn("订单解冻商户金额异常，orderId: {}", infoDO.getOrderId(), e);
            } finally {
                redisLockComponent.unlock(lockKey, lockKey);
            }
        }

    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void infoRefundAuditSuccessTransfer() {
        // 同意的
        List<OrderInfoDO> infoDOS = orderInfoMapper.listRefundTransfer(OrderEnum.InfoStatusEnum.refund_success.getStatus(), OrderEnum.TxStatusEnum.wait.getStatus());
        if (CollectionUtils.isEmpty(infoDOS)) {
            return;
        }

        for (OrderInfoDO infoDO : infoDOS) {
            ChainEnum.CoinEnum coinEnum = ChainEnum.CoinEnum.exist(infoDO.getPayCoin());

            ChainPaymentProperties.Detail properties = chainPaymentProperties.getByCoinEnum(coinEnum);
            if (properties == null) {
                continue;
            }

            UserAdminDO userAdminDO = userAdminMapper.getByAddress(infoDO.getSellerAddress());
            String lockKey = String.format(RedisLockKey.Chain.nonce_of_address, userAdminDO.getInsideAddress());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("订单定时器，退款同意转币，执行开始，订单编号：{}", infoDO.getOrderId());

            TxResult<String> txResult = Web3jUtils.transfer(properties.getChainUrl(), properties.getChainId(), properties.getContractAddress(), userAdminDO.getInsidePrivateKey(), infoDO.getBuyerAddress(), infoDO.getRefundAmount(), properties.getDecimals());

            OrderInfoDO updateDO = new OrderInfoDO();
            updateDO.setOrderId(infoDO.getOrderId());
            updateDO.setOriginalStatus(OrderEnum.InfoStatusEnum.refund_success.getStatus());
            updateDO.setOriginalRefundTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
            if (txResult.getStatus() == TxResultEnum.FAILED) {
                updateDO.setRefundTxStatus(OrderEnum.TxStatusEnum.fail.getStatus());
                updateDO.setRefundTxNote(txResult.getError());
            } else {
                updateDO.setRefundTxStatus(OrderEnum.TxStatusEnum.success.getStatus());
                updateDO.setRefundTxHash(txResult.getResult());
                updateDO.setRefundTxNote("");
            }

            int i = orderInfoMapper.updateByOrderId(updateDO);
            if (i == 0) {
                log.error("订单定时器，退款同意转币，结果保存失败：{}", updateDO);
            }

            redisLockComponent.unlock(lockKey, lockKey);

            log.info("订单定时器，退款同意转币，执行结束，订单编号：{}，执行结果：{}", infoDO.getOrderId(), txResult);
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void infoRefundAuditFailTransfer() {
        // 不同意的
        List<OrderInfoDO> infoDOS = orderInfoMapper.listRefundTransfer(OrderEnum.InfoStatusEnum.refund_fail.getStatus(), OrderEnum.TxStatusEnum.wait.getStatus());
        if (CollectionUtils.isEmpty(infoDOS)) {
            return;
        }

        // 转币
        ChainContractDO contractDO = chainContractMapper.getByCtId(ChainConstant.contract_default_contract_id);

        Map<String, List<OrderInfoDO>> groupMap = infoDOS.stream().collect(Collectors.groupingBy(OrderInfoDO::getSellerAddress));
        for (Map.Entry<String, List<OrderInfoDO>> entry : groupMap.entrySet()) {
            UserAdminDO userAdminDO = userAdminMapper.getByAddress(entry.getKey());

            String lockKey = String.format(RedisLockKey.Chain.nonce_of_address, userAdminDO.getInsideAddress());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("订单定时器，退款不同意转币，执行开始，商户地址：{}", userAdminDO.getAddress());

            List<String> orderIds = new ArrayList<>();
            List<FzmERC1155.MultiTX> multiTxs = new ArrayList<>();
            for (OrderInfoDO infoDO : entry.getValue()) {
                orderIds.add(infoDO.getOrderId());

                List<BigInteger> ids = JSON.parseArray(infoDO.getTokenIdJson(), Long.class).stream().map(BigInteger::valueOf).toList();
                multiTxs.add(new FzmERC1155.MultiTX(infoDO.getBuyerAddress(), ids));
            }
            TxResult<String> txResult = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), contractDO.getAddress()).muxBatchTransferNFT(userAdminDO.getInsidePrivateKey(), multiTxs);

            OrderInfoDO updateDO = new OrderInfoDO();
            updateDO.setOrderIds(orderIds);
            updateDO.setOriginalStatus(OrderEnum.InfoStatusEnum.refund_fail.getStatus());
            updateDO.setOriginalRefundTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
            if (txResult.getStatus() == TxResultEnum.FAILED) {
                updateDO.setRefundTxStatus(OrderEnum.TxStatusEnum.fail.getStatus());
                updateDO.setRefundTxNote(txResult.getError());
            } else {
                updateDO.setRefundTxStatus(OrderEnum.TxStatusEnum.success.getStatus());
                updateDO.setRefundTxHash(txResult.getResult());
                updateDO.setRefundTxNote("");
            }

            int i = orderInfoMapper.updateByOrderId(updateDO);
            if (i != orderIds.size()) {
                log.error("订单定时器，退款不同意转币，结果保存失败：{}", updateDO);
            }

            redisLockComponent.unlock(lockKey, lockKey);

            chainEventInsideComponent.handleEvent(contractDO, txResult.getResult());

            log.info("订单定时器，退款不同意转币，执行结束，商户地址：{}，执行结果：{}", userAdminDO.getAddress(), txResult);
        }

    }

    @Scheduled(cron = "* * * * * ?")
    public void expressConfirmAuto() {
        List<OrderExpressDO> expressDOS = orderExpressMapper.listAutoConfirm(TimeUtils.nowTimestamp(), OrderEnum.ExpressStatusEnum.express.getStatus());
        if (CollectionUtils.isEmpty(expressDOS)) {
            return;
        }

        for (OrderExpressDO expressDO : expressDOS) {
            String lockKey = String.format(RedisLockKey.Order.express_confirm, expressDO.getOrderId());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("订单定时器，自动收货，订单编号：{}", expressDO.getOrderId());

            try {
                orderService.confirmExpress(expressDO.getOrderId(), expressDO.getBuyerAddress());
            } catch (Exception ignored) {
            }

            redisLockComponent.unlock(lockKey, lockKey);
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void expressRefundConfirmTransfer() {
        List<OrderExpressDO> expressDOS = orderExpressMapper.listTransfer(OrderEnum.ExpressStatusEnum.refund_confirm.getStatus(), OrderEnum.TxStatusEnum.wait.getStatus());
        if (CollectionUtils.isEmpty(expressDOS)) {
            return;
        }

        // 转币
        ChainContractDO contractDO = chainContractMapper.getByCtId(ChainConstant.contract_default_contract_id);

        Map<String, List<OrderExpressDO>> groupMap = expressDOS.stream().collect(Collectors.groupingBy(OrderExpressDO::getSellerAddress));
        for (Map.Entry<String, List<OrderExpressDO>> entry : groupMap.entrySet()) {
            UserAdminDO userAdminDO = userAdminMapper.getByAddress(entry.getKey());

            String lockKey = String.format(RedisLockKey.Chain.nonce_of_address, userAdminDO.getInsideAddress());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("退货定时器，退货转币，执行开始，商户地址：{}", userAdminDO.getAddress());

            List<String> orderIds = new ArrayList<>();
            List<FzmERC1155.MultiTX> multiTxs = new ArrayList<>();
            for (OrderExpressDO expressDO : entry.getValue()) {
                orderIds.add(expressDO.getOrderId());
                List<BigInteger> ids = JSON.parseArray(expressDO.getTokenIdJson(), Long.class).stream().map(BigInteger::valueOf).toList();
                multiTxs.add(new FzmERC1155.MultiTX(expressDO.getBuyerAddress(), ids));
            }
            TxResult<String> txResult = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), contractDO.getAddress()).muxBatchTransferNFT(userAdminDO.getInsidePrivateKey(), multiTxs);

            OrderExpressDO updateDO = new OrderExpressDO();
            updateDO.setOrderIds(orderIds);
            updateDO.setOriginalStatus(OrderEnum.ExpressStatusEnum.refund_confirm.getStatus());
            updateDO.setOriginalRefundTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
            if (txResult.getStatus() == TxResultEnum.FAILED) {
                updateDO.setRefundTxStatus(OrderEnum.TxStatusEnum.fail.getStatus());
                updateDO.setRefundTxNote(txResult.getError());
            } else {
                updateDO.setRefundTxStatus(OrderEnum.TxStatusEnum.success.getStatus());
                updateDO.setRefundTxHash(txResult.getResult());
                updateDO.setRefundTxNote("");
            }

            int i = orderExpressMapper.updateByOrderId(updateDO);
            if (i != orderIds.size()) {
                log.error("退货定时器，退货转币，结果保存失败：{}", updateDO);
            }

            redisLockComponent.unlock(lockKey, lockKey);

            chainEventInsideComponent.handleEvent(contractDO, txResult.getResult());

            log.info("退货定时器，退货转币，执行结束，商户地址：{}，执行结果：{}", userAdminDO.getAddress(), txResult);
        }

    }

    @Scheduled(cron = "* * * * * ?")
    public void expressRefundConfirmAuto() {
        List<OrderExpressDO> expressDOS = orderExpressMapper.listRefundAutoConfirm(TimeUtils.nowTimestamp(), OrderEnum.ExpressStatusEnum.refund_ing.getStatus());
        if (CollectionUtils.isEmpty(expressDOS)) {
            return;
        }

        for (OrderExpressDO expressDO : expressDOS) {
            String lockKey = String.format(RedisLockKey.Order.refund_confirm, expressDO.getOrderId());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("退货定时器，退货确认，订单编号：{}", expressDO.getOrderId());

            try {
                orderService.refundConfirm(expressDO.getOrderId(), expressDO.getSellerAddress());
            } catch (Exception ignored) {
            }

            redisLockComponent.unlock(lockKey, lockKey);
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void blindBoxTransfer() {
        List<OrderBlindBoxDO> boxDOS = orderBlindBoxMapper.listByTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
        if (CollectionUtils.isEmpty(boxDOS)) {
            return;
        }

        // 转币
        ChainContractDO contractDO = chainContractMapper.getByCtId(ChainConstant.contract_default_contract_id);

        Map<String, List<OrderBlindBoxDO>> groupMap = boxDOS.stream().collect(Collectors.groupingBy(OrderBlindBoxDO::getSellerAddress));
        for (Map.Entry<String, List<OrderBlindBoxDO>> entry : groupMap.entrySet()) {
            UserAdminDO userAdminDO = userAdminMapper.getByAddress(entry.getKey());

            String lockKey = String.format(RedisLockKey.Chain.nonce_of_address, userAdminDO.getInsideAddress());
            boolean lock = redisLockComponent.lock(lockKey, lockKey, RedisCacheExpireEnum.expire_minutes_10);
            if (!lock) {
                continue;
            }

            log.info("订单定时器，盲盒转币，执行开始，商户地址：{}", userAdminDO.getAddress());

            List<String> orderIds = new ArrayList<>();
            List<FzmERC1155.MultiTX> multiTxs = new ArrayList<>();
            for (OrderBlindBoxDO boxDO : entry.getValue()) {
                orderIds.add(boxDO.getOrderId());

                multiTxs.add(new FzmERC1155.MultiTX(boxDO.getBuyerAddress(), Collections.singletonList(BigInteger.valueOf(boxDO.getRewardTokenId()))));
            }
            TxResult<String> txResult = ERC1155Manager.init(contractProperties.getChainUrl(), contractProperties.getChainId(), contractDO.getAddress()).muxBatchTransferNFT(userAdminDO.getInsidePrivateKey(), multiTxs);

            OrderBlindBoxDO updateDO = new OrderBlindBoxDO();
            updateDO.setOrderIds(orderIds);
            updateDO.setOriginalTxStatus(OrderEnum.TxStatusEnum.wait.getStatus());
            if (txResult.getStatus() == TxResultEnum.FAILED) {
                updateDO.setRewardTxStatus(OrderEnum.TxStatusEnum.fail.getStatus());
                updateDO.setRewardTxNote(txResult.getError());
            } else {
                updateDO.setRewardTxStatus(OrderEnum.TxStatusEnum.success.getStatus());
                updateDO.setRewardTxHash(txResult.getResult());
                updateDO.setRewardTxNote("");
            }

            int i = orderBlindBoxMapper.updateByOrderId(updateDO);
            if (i != orderIds.size()) {
                log.error("订单定时器，盲盒转币，结果保存失败：{}", updateDO);
            }

            redisLockComponent.unlock(lockKey, lockKey);

            chainEventInsideComponent.handleEvent(contractDO, txResult.getResult());

            log.info("订单定时器，盲盒转币，执行结束，商户地址：{}，执行结果：{}", userAdminDO.getAddress(), txResult);
        }

    }
}
