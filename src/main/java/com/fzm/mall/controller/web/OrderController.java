package com.fzm.mall.controller.web;

import com.alibaba.fastjson2.JSON;
import com.fzm.mall.component.OrderComponent;
import com.fzm.mall.constant.SystemConstant;
import com.fzm.mall.constant.enums.ChainEnum;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.OrderEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.GoodsSkuDO;
import com.fzm.mall.entity.dataobject.OrderExpressDO;
import com.fzm.mall.entity.dataobject.OrderInfoDO;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.properties.ChainPaymentProperties;
import com.fzm.mall.entity.queryobject.OrderExpressPageQO;
import com.fzm.mall.entity.queryobject.OrderInfoPageQO;
import com.fzm.mall.entity.queryobject.back.MOrderExpressPageQO;
import com.fzm.mall.entity.queryobject.back.MOrderInfoPageQO;
import com.fzm.mall.entity.requestobject.*;
import com.fzm.mall.entity.requestobject.back.MOrderExpressRO;
import com.fzm.mall.entity.viewobject.*;
import com.fzm.mall.redis.cache.PriceCacheComponent;
import com.fzm.mall.service.GoodsService;
import com.fzm.mall.service.OrderService;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.util.*;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "订单")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/order", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {
    private final OrderService orderService;
    private final GoodsService goodsService;
    private final UserAdminService userAdminService;
    private final OrderComponent orderComponent;
    private final PriceCacheComponent priceCacheComponent;
    private final ChainPaymentProperties chainPaymentProperties;

    @Operation(summary = "分页查询")
    @PostMapping("/info/page")
    public ResponseVO<PageVO<OrderVO>> pageInfo(@RequestBody OrderInfoPageQO infoPageQO) {
        MOrderInfoPageQO pageQO = BeanCopierUtils.copy(infoPageQO, MOrderInfoPageQO.class);
        pageQO.setBuyerAddress(ThreadInfo.getInfo().getAddress());
        pageQO.setDeleteStatus(CommonEnum.BoolEnum.NO.getStatus());

        PageInfo<OrderInfoDO> pageInfo = orderService.pageInfo(pageQO);

        List<String> skuIds = pageInfo.getList().stream().map(OrderInfoDO::getSkuId).distinct().toList();
        Map<String, GoodsSkuDO> skuDOMap = goodsService.listSkuBySkuIds(skuIds).stream().collect(Collectors.toMap(GoodsSkuDO::getSkuId, o -> o));

        List<String> addressList = pageInfo.getList().stream().map(OrderInfoDO::getSellerAddress).distinct().toList();
        Map<String, UserAdminDO> adminDOMap = userAdminService.listByAddressList(addressList).stream().collect(Collectors.toMap(UserAdminDO::getAddress, o -> o));

        List<OrderVO> orderVOS = pageInfo.getList().stream().map(o -> {
            OrderVO orderVO = new OrderVO();
            orderVO.setInfo(BeanCopierUtils.copy(o, OrderInfoVO.class));
            orderVO.setMeta(FormatVOUtils.getTokenMetaVO(skuDOMap.get(o.getSkuId())));
            orderVO.setMerchant(FormatVOUtils.getMerchantVO(adminDOMap.get(o.getSellerAddress())));
            return orderVO;
        }).toList();

        return PageVOUtils.pageVO(pageInfo, orderVOS);
    }

    @Operation(summary = "分页查询-提货")
    @PostMapping("/express/page")
    public ResponseVO<PageVO<OrderVO>> pageExpress(@RequestBody OrderExpressPageQO expressPageQO) {
        MOrderExpressPageQO pageQO = BeanCopierUtils.copy(expressPageQO, MOrderExpressPageQO.class);
        pageQO.setBuyerAddress(ThreadInfo.getInfo().getAddress());
        pageQO.setDeleteStatus(CommonEnum.BoolEnum.NO.getStatus());

        PageInfo<OrderExpressDO> pageInfo = orderService.pageExpress(pageQO);

        List<String> skuIds = pageInfo.getList().stream().map(OrderExpressDO::getSkuId).distinct().toList();
        Map<String, GoodsSkuDO> skuDOMap = goodsService.listSkuBySkuIds(skuIds).stream().collect(Collectors.toMap(GoodsSkuDO::getSkuId, o -> o));

        List<String> addressList = pageInfo.getList().stream().map(OrderExpressDO::getSellerAddress).distinct().toList();
        Map<String, UserAdminDO> adminDOMap = userAdminService.listByAddressList(addressList).stream().collect(Collectors.toMap(UserAdminDO::getAddress, o -> o));

        List<OrderVO> orderVOS = pageInfo.getList().stream().map(o -> {
            OrderVO orderVO = new OrderVO();
            orderVO.setExpress(BeanCopierUtils.copy(o, OrderExpressVO.class));
            orderVO.setMeta(FormatVOUtils.getTokenMetaVO(skuDOMap.get(o.getSkuId())));
            orderVO.setMerchant(FormatVOUtils.getMerchantVO(adminDOMap.get(o.getSellerAddress())));
            return orderVO;
        }).toList();

        return PageVOUtils.pageVO(pageInfo, orderVOS);
    }

    @Operation(summary = "详情")
    @Parameter(name = "orderId", description = "订单编号", in = ParameterIn.QUERY)
    @GetMapping("/info/detail")
    public ResponseVO<OrderVO> detailInfo(@RequestParam("orderId") String orderId) {
        AssertUtils.isNotBlank(orderId, ResponseEnum.invalid_parameter);
        OrderInfoDO infoDO = orderService.getInfoByOrderId(orderId);
        AssertUtils.isNotNull(infoDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(infoDO.getBuyerAddress().equals(ThreadInfo.getInfo().getAddress()), ResponseEnum.invalid_parameter);

        GoodsSkuDO skuDO = goodsService.getSkuBySkuId(infoDO.getSkuId());
        UserAdminDO userAdminDO = userAdminService.getByAddress(infoDO.getSellerAddress());

        boolean canRefund = infoDO.getStatus() == OrderEnum.InfoStatusEnum.paid.getStatus() && (TimeUtils.nowTimestamp() - infoDO.getOrderTime() < SystemConstant.Time.order_can_refund_time_gap);

        OrderInfoVO infoVO = BeanCopierUtils.copy(infoDO, OrderInfoVO.class);
        infoVO.setCanRefund(canRefund);

        OrderVO orderVO = new OrderVO();
        orderVO.setInfo(infoVO);
        orderVO.setMeta(FormatVOUtils.getTokenMetaVO(skuDO));
        orderVO.setMerchant(FormatVOUtils.getMerchantVO(userAdminDO));

        return ResponseUtils.success(orderVO);
    }

    @Operation(summary = "详情-提货")
    @Parameter(name = "orderId", description = "订单编号", in = ParameterIn.QUERY)
    @GetMapping("/express/detail")
    public ResponseVO<OrderVO> detailExpress(@RequestParam("orderId") String orderId) {
        AssertUtils.isNotBlank(orderId, ResponseEnum.invalid_parameter);
        OrderExpressDO expressDO = orderService.getExpressByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(expressDO.getBuyerAddress().equals(ThreadInfo.getInfo().getAddress()), ResponseEnum.invalid_parameter);

        GoodsSkuDO skuDO = goodsService.getSkuBySkuId(expressDO.getSkuId());
        UserAdminDO userAdminDO = userAdminService.getByAddress(skuDO.getAddress());

        OrderVO orderVO = new OrderVO();
        orderVO.setExpress(BeanCopierUtils.copy(expressDO, OrderExpressVO.class));
        orderVO.setMeta(FormatVOUtils.getTokenMetaVO(skuDO));
        orderVO.setMerchant(FormatVOUtils.getMerchantVO(userAdminDO));

        return ResponseUtils.success(orderVO);
    }

    @Operation(summary = "下单")
    @PostMapping("/info/submit")
    public ResponseVO<String> order(@RequestBody OrderRO orderRO) {
        AssertUtils.isNotBlank(orderRO.getSkuId(), ResponseEnum.invalid_parameter);
        AssertUtils.isNotNull(orderRO.getNum(), ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(orderRO.getNum() > 0 && orderRO.getNum() <= 50, ResponseEnum.Maximum_num_per_order);

        CommonEnum.BoolEnum expressTypeEnum = CommonEnum.BoolEnum.exist(orderRO.getExpressType());
        AssertUtils.isNotNull(expressTypeEnum, ResponseEnum.invalid_parameter);
        if (expressTypeEnum == CommonEnum.BoolEnum.YES) {
            AssertUtils.isNotNull(orderRO.getExpressDetail(), ResponseEnum.invalid_parameter);
        }

        String orderId = orderService.order(orderRO.getSkuId(), orderRO.getNum(), ThreadInfo.getInfo().getAddress(), orderRO.getExpressType(), JSON.toJSONString(orderRO.getExpressDetail()));

        return ResponseUtils.success(orderId);
    }

    @Operation(summary = "支付方式")
    @Parameter(name = "orderId", description = "订单编号", in = ParameterIn.QUERY)
    @GetMapping("/info/pay-type")
    public ResponseVO<List<PriceVO>> payType(@RequestParam("orderId") String orderId) {
        AssertUtils.isNotBlank(orderId, ResponseEnum.invalid_parameter);
        OrderInfoDO infoDO = orderService.getInfoByOrderId(orderId);
        AssertUtils.isNotNull(infoDO, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(infoDO.getBuyerAddress().equals(ThreadInfo.getInfo().getAddress()), ResponseEnum.invalid_parameter);

        UserAdminDO sellerDO = userAdminService.getByAddress(infoDO.getSellerAddress());

        List<PriceVO> priceVOS = new ArrayList<>();

        for (ChainEnum.TypeEnum typeEnum : ChainEnum.TypeEnum.values()) {
            ChainPaymentProperties.Detail properties = chainPaymentProperties.getByChainEnum(typeEnum);
            if (!properties.getEnable()) {
                continue;
            }

            BigDecimal coinPrice = priceCacheComponent.getPrice(typeEnum.getCoinEnum());
            if (coinPrice == null || coinPrice.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            priceCacheComponent.setPriceOfAddress(typeEnum.getCoinEnum(), ThreadInfo.getInfo().getAddress(), coinPrice);

            PriceVO priceVO = new PriceVO();
            priceVO.setChainType(typeEnum.getType());
            priceVO.setCoinType(typeEnum.getCoinEnum().getType());
            priceVO.setPrice(coinPrice);
            priceVO.setAmount(infoDO.getAmount().divide(coinPrice, properties.getCalcDecimals(), RoundingMode.UP));
            priceVO.setPayAddress(sellerDO.getInsideAddress());
            priceVOS.add(priceVO);
        }

        return ResponseUtils.success(priceVOS);
    }

    @Operation(summary = "支付")
    @PostMapping("/info/pay")
    public ResponseVO<Object> pay(@RequestBody OrderPayRO payRO) {
        AssertUtils.isNotBlank(payRO.getOrderId(), ResponseEnum.invalid_parameter);
        ChainEnum.TypeEnum chainTypeEnum = ChainEnum.TypeEnum.exist(payRO.getChainType());
        AssertUtils.isNotNull(chainTypeEnum, ResponseEnum.invalid_parameter);
        ChainPaymentProperties.Detail detail = chainPaymentProperties.getByChainEnum(chainTypeEnum);
        AssertUtils.isNotNull(detail, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(detail.getEnable(), ResponseEnum.invalid_parameter);
        String txHash = ParamsUtils.txHash(payRO.getTxHash());

        BigDecimal payPrice = priceCacheComponent.getPriceOfAddress(chainTypeEnum.getCoinEnum(), ThreadInfo.getInfo().getAddress());
        AssertUtils.isNotNull(payPrice, ResponseEnum.too_many_requests_plz_try_again_later);

        orderService.pay(payRO.getOrderId(), chainTypeEnum, detail, payPrice, txHash, ThreadInfo.getInfo().getAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "撤销订单")
    @PostMapping("/info/cancel")
    public ResponseVO<Object> cancelInfo(@RequestBody OrderIdRO orderIdRO) {
        AssertUtils.isNotBlank(orderIdRO.getOrderId(), ResponseEnum.invalid_parameter);

        orderService.cancelInfo(orderIdRO.getOrderId(), ThreadInfo.getInfo().getAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "申请退款")
    @PostMapping("/info/refund/apply")
    public ResponseVO<Object> refundApplyInfo(@RequestBody OrderInfoRefundRO refundRO) {
        AssertUtils.isNotBlank(refundRO.getOrderId(), ResponseEnum.invalid_parameter);
        String txHash = ParamsUtils.txHash(refundRO.getTxHash());

        orderService.refundApplyInfo(refundRO.getOrderId(), txHash, refundRO.getNote(), ThreadInfo.getInfo().getAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "删除订单")
    @PostMapping("/info/delete")
    public ResponseVO<Object> deleteInfo(@RequestBody OrderIdRO orderIdRO) {
        AssertUtils.isNotBlank(orderIdRO.getOrderId(), ResponseEnum.invalid_parameter);

        orderService.deleteInfo(orderIdRO.getOrderId(), ThreadInfo.getInfo().getAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/express/confirm")
    public ResponseVO<Object> confirmExpress(@RequestBody OrderIdRO orderIdRO) {
        AssertUtils.isNotBlank(orderIdRO.getOrderId(), ResponseEnum.invalid_parameter);

        orderService.confirmExpress(orderIdRO.getOrderId(), ThreadInfo.getInfo().getAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "申请退货")
    @PostMapping("/express/refund/apply")
    public ResponseVO<Object> refundApplyExpress(@RequestBody OrderExpressRefundRO refundRO) {
        AssertUtils.isNotBlank(refundRO.getOrderId(), ResponseEnum.invalid_parameter);

        orderService.refundApplyExpress(refundRO.getOrderId(), refundRO.getNote(), ThreadInfo.getInfo().getAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "申请退货-撤销")
    @PostMapping("/express/refund/apply-cancel")
    public ResponseVO<Object> refundApplyCancelExpress(@RequestBody OrderIdRO orderIdRO) {
        AssertUtils.isNotBlank(orderIdRO.getOrderId(), ResponseEnum.invalid_parameter);

        orderService.refundApplyCancelExpress(orderIdRO.getOrderId(), ThreadInfo.getInfo().getAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "退货单号")
    @PostMapping("/express/refund/express")
    public ResponseVO<Object> refundExpress(@RequestBody MOrderExpressRO expressRO) {
        AssertUtils.isNotBlank(expressRO.getOrderId(), ResponseEnum.invalid_parameter);
        AssertUtils.isNotBlank(expressRO.getExpressCode(), ResponseEnum.invalid_parameter);

        List<String> codes = Arrays.stream(expressRO.getExpressCode().replaceAll("，", ",").split(",")).filter(StringUtils::isNotBlank).distinct().toList();
        AssertUtils.isNotEmpty(codes, "快递单号错误");
        String expressCode = String.join(",", codes);

        orderService.refundExpress(expressRO.getOrderId(), expressCode, ThreadInfo.getInfo().getAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "查询物流")
    @Parameters({
            @Parameter(name = "orderId", description = "订单编号", in = ParameterIn.QUERY),
            @Parameter(name = "type", description = "类型，0发货物流，1退货物流", in = ParameterIn.QUERY)
    })
    @GetMapping("/express/logistics")
    public ResponseVO<List<LogisticsVO>> logistics(@RequestParam("orderId") String orderId, @RequestParam("type") Integer type) {
        AssertUtils.isNotBlank(orderId, ResponseEnum.invalid_parameter);
        AssertUtils.isNotNull(CommonEnum.BoolEnum.exist(type), ResponseEnum.invalid_parameter);

        List<LogisticsVO> vos = orderComponent.getLogisticsVOByOrderId(orderId, "", ThreadInfo.getInfo().getAddress(), type);

        return ResponseUtils.success(vos);
    }
}
