package com.fzm.mall.controller.manage;

import com.fzm.mall.component.ExcelWriteComponent;
import com.fzm.mall.component.OrderComponent;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.GoodsSkuDO;
import com.fzm.mall.entity.dataobject.OrderExpressDO;
import com.fzm.mall.entity.dataobject.OrderInfoDO;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.queryobject.back.MOrderExpressPageQO;
import com.fzm.mall.entity.queryobject.back.MOrderInfoPageQO;
import com.fzm.mall.entity.requestobject.OrderIdRO;
import com.fzm.mall.entity.requestobject.back.MOrderExpressRO;
import com.fzm.mall.entity.requestobject.back.MOrderExpressRefundAuditRO;
import com.fzm.mall.entity.requestobject.back.MOrderInfoRefundAuditRO;
import com.fzm.mall.entity.viewobject.LogisticsVO;
import com.fzm.mall.entity.viewobject.OrderExpressVO;
import com.fzm.mall.entity.viewobject.back.MOrderInfoVO;
import com.fzm.mall.entity.viewobject.back.MOrderVO;
import com.fzm.mall.service.GoodsService;
import com.fzm.mall.service.OrderService;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.FormatVOUtils;
import com.fzm.mall.util.PageVOUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "订单")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/order", produces = MediaType.APPLICATION_JSON_VALUE)
public class MOrderController {
    private final OrderService orderService;
    private final GoodsService goodsService;
    private final UserAdminService userAdminService;
    private final OrderComponent orderComponent;
    private final ExcelWriteComponent excelWriteComponent;

    @Operation(summary = "分页查询")
    @PostMapping("/info/page")
    public ResponseVO<PageVO<MOrderVO>> pageInfo(@RequestBody MOrderInfoPageQO pageQO) {
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            pageQO.setSellerAddress(ThreadInfo.getInfo().getParentAddress());
        }
        pageQO.setDeleteStatus(CommonEnum.BoolEnum.NO.getStatus());

        PageInfo<OrderInfoDO> pageInfo = orderService.pageInfo(pageQO);

        List<String> skuIds = pageInfo.getList().stream().map(OrderInfoDO::getSkuId).distinct().toList();
        Map<String, GoodsSkuDO> skuDOMap = goodsService.listSkuBySkuIds(skuIds).stream().collect(Collectors.toMap(GoodsSkuDO::getSkuId, o -> o));

        List<String> addressList = pageInfo.getList().stream().map(OrderInfoDO::getSellerAddress).distinct().toList();
        Map<String, UserAdminDO> adminDOMap = userAdminService.listByAddressList(addressList).stream().collect(Collectors.toMap(UserAdminDO::getAddress, o -> o));

        List<MOrderVO> orderVOS = pageInfo.getList().stream().map(o -> {
            MOrderVO orderVO = new MOrderVO();
            orderVO.setInfo(BeanCopierUtils.copy(o, MOrderInfoVO.class));
            orderVO.setMeta(FormatVOUtils.getTokenMetaVO(skuDOMap.get(o.getSkuId())));
            orderVO.setMerchant(FormatVOUtils.getMerchantVO(adminDOMap.get(o.getSellerAddress())));
            return orderVO;
        }).toList();

        return PageVOUtils.pageVO(pageInfo, orderVOS);
    }

    @Operation(summary = "分页查询-提货")
    @PostMapping("/express/page")
    public ResponseVO<PageVO<MOrderVO>> pageExpress(@RequestBody MOrderExpressPageQO pageQO) {
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            pageQO.setSellerAddress(ThreadInfo.getInfo().getParentAddress());
        }
        pageQO.setDeleteStatus(CommonEnum.BoolEnum.NO.getStatus());

        PageInfo<OrderExpressDO> pageInfo = orderService.pageExpress(pageQO);

        List<String> skuIds = pageInfo.getList().stream().map(OrderExpressDO::getSkuId).distinct().toList();
        Map<String, GoodsSkuDO> skuDOMap = goodsService.listSkuBySkuIds(skuIds).stream().collect(Collectors.toMap(GoodsSkuDO::getSkuId, o -> o));

        List<String> addressList = pageInfo.getList().stream().map(OrderExpressDO::getSellerAddress).distinct().toList();
        Map<String, UserAdminDO> adminDOMap = userAdminService.listByAddressList(addressList).stream().collect(Collectors.toMap(UserAdminDO::getAddress, o -> o));

        List<MOrderVO> orderVOS = pageInfo.getList().stream().map(o -> {
            MOrderVO orderVO = new MOrderVO();
            orderVO.setExpress(BeanCopierUtils.copy(o, OrderExpressVO.class));
            orderVO.setMeta(FormatVOUtils.getTokenMetaVO(skuDOMap.get(o.getSkuId())));
            orderVO.setMerchant(FormatVOUtils.getMerchantVO(adminDOMap.get(o.getSellerAddress())));
            return orderVO;
        }).toList();

        return PageVOUtils.pageVO(pageInfo, orderVOS);
    }

    @Operation(summary = "提货-导出")
    @PostMapping("/express/excel")
    public ResponseEntity<ByteArrayResource> excelExpress(@RequestBody MOrderExpressPageQO pageQO) {
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            pageQO.setSellerAddress(ThreadInfo.getInfo().getParentAddress());
        }
        pageQO.setDeleteStatus(CommonEnum.BoolEnum.NO.getStatus());

        List<OrderExpressDO> dos = orderService.listExpress(pageQO);
        AssertUtils.isFalse(dos.size() > 100000, ResponseEnum.too_many_requests_plz_try_again_later);

        byte[] bytes = excelWriteComponent.writeExcelOrderExpressList(dos);
        if (bytes == null) {
            return null;
        }

        HttpHeaders httpHeaders = excelWriteComponent.getHttpHeaders();
        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    @Operation(summary = "详情")
    @Parameter(name = "orderId", description = "订单编号", in = ParameterIn.QUERY)
    @GetMapping("/info/detail")
    public ResponseVO<MOrderVO> detailInfo(@RequestParam("orderId") String orderId) {
        AssertUtils.isNotBlank(orderId, ResponseEnum.invalid_parameter);
        OrderInfoDO infoDO = orderService.getInfoByOrderId(orderId);
        AssertUtils.isNotNull(infoDO, ResponseEnum.invalid_parameter);
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            AssertUtils.isTrue(infoDO.getSellerAddress().equals(ThreadInfo.getInfo().getParentAddress()), ResponseEnum.invalid_parameter);
        }

        GoodsSkuDO skuDO = goodsService.getSkuBySkuId(infoDO.getSkuId());
        UserAdminDO userAdminDO = userAdminService.getByAddress(infoDO.getSellerAddress());

        MOrderVO orderVO = new MOrderVO();
        orderVO.setInfo(BeanCopierUtils.copy(infoDO, MOrderInfoVO.class));
        orderVO.setMeta(FormatVOUtils.getTokenMetaVO(skuDO));
        orderVO.setMerchant(FormatVOUtils.getMerchantVO(userAdminDO));

        return ResponseUtils.success(orderVO);
    }

    @Operation(summary = "退款审核")
    @PostMapping("/info/refund/audit")
    public ResponseVO<Object> refundAuditInfo(@RequestBody MOrderInfoRefundAuditRO auditRO) {
        AssertUtils.isNotBlank(auditRO.getOrderId(), ResponseEnum.invalid_parameter);

        CommonEnum.BoolEnum statusEnum = CommonEnum.BoolEnum.exist(auditRO.getStatus());
        AssertUtils.isNotNull(statusEnum, ResponseEnum.invalid_parameter);

        orderService.refundAuditInfo(auditRO.getOrderId(), statusEnum, auditRO.getNote(), ThreadInfo.getInfo().getParentAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "详情-提货")
    @Parameter(name = "orderId", description = "订单编号", in = ParameterIn.QUERY)
    @GetMapping("/express/detail")
    public ResponseVO<MOrderVO> detailExpress(@RequestParam("orderId") String orderId) {
        AssertUtils.isNotBlank(orderId, ResponseEnum.invalid_parameter);
        OrderExpressDO expressDO = orderService.getExpressByOrderId(orderId);
        AssertUtils.isNotNull(expressDO, ResponseEnum.invalid_parameter);
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            AssertUtils.isTrue(expressDO.getSellerAddress().equals(ThreadInfo.getInfo().getParentAddress()), ResponseEnum.invalid_parameter);
        }

        GoodsSkuDO skuDO = goodsService.getSkuBySkuId(expressDO.getSkuId());
        UserAdminDO userAdminDO = userAdminService.getByAddress(skuDO.getAddress());

        MOrderVO orderVO = new MOrderVO();
        orderVO.setExpress(BeanCopierUtils.copy(expressDO, OrderExpressVO.class));
        orderVO.setMeta(FormatVOUtils.getTokenMetaVO(skuDO));
        orderVO.setMerchant(FormatVOUtils.getMerchantVO(userAdminDO));

        return ResponseUtils.success(orderVO);
    }

    @Operation(summary = "发货")
    @PostMapping("/express/express")
    public ResponseVO<Object> express(@RequestBody MOrderExpressRO expressRO) {
        AssertUtils.isNotBlank(expressRO.getOrderId(), ResponseEnum.invalid_parameter);
        AssertUtils.isNotBlank(expressRO.getExpressCode(), ResponseEnum.invalid_parameter);

        List<String> codes = Arrays.stream(expressRO.getExpressCode().replaceAll("，", ",").split(",")).filter(StringUtils::isNotBlank).distinct().toList();
        AssertUtils.isNotEmpty(codes, "快递单号错误");
        String expressCode = String.join(",", codes);

        orderService.express(expressRO.getOrderId(), expressCode, ThreadInfo.getInfo().getParentAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "退货审核")
    @PostMapping("/express/refund/audit")
    public ResponseVO<Object> refundAuditExpress(@RequestBody MOrderExpressRefundAuditRO auditRO) {
        AssertUtils.isNotBlank(auditRO.getOrderId(), ResponseEnum.invalid_parameter);

        CommonEnum.BoolEnum statusEnum = CommonEnum.BoolEnum.exist(auditRO.getStatus());
        AssertUtils.isNotNull(statusEnum, ResponseEnum.invalid_parameter);

        CommonEnum.BoolEnum needExpressEnum = CommonEnum.BoolEnum.exist(auditRO.getNeedExpress());
        AssertUtils.isNotNull(needExpressEnum, ResponseEnum.invalid_parameter);

        orderService.refundAuditExpress(auditRO.getOrderId(), statusEnum, needExpressEnum, auditRO.getNote(), ThreadInfo.getInfo().getParentAddress());

        return ResponseUtils.success();
    }

    @Operation(summary = "确认退货")
    @PostMapping("/express/refund/confirm")
    public ResponseVO<Object> refundConfirm(@RequestBody OrderIdRO orderIdRO) {
        AssertUtils.isNotBlank(orderIdRO.getOrderId(), ResponseEnum.invalid_parameter);

        orderService.refundConfirm(orderIdRO.getOrderId(), ThreadInfo.getInfo().getParentAddress());

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

        String sellerAddress = "";
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            sellerAddress = ThreadInfo.getInfo().getParentAddress();
        }
        List<LogisticsVO> vos = orderComponent.getLogisticsVOByOrderId(orderId, sellerAddress, "", type);

        return ResponseUtils.success(vos);
    }
}
