package com.fzm.mall.entity.viewobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderExpressVO {
    @Schema(description = "订单编号")
    private String orderId;

    @Schema(description = "商户地址")
    private String sellerAddress;
    @Schema(description = "买家地址")
    private String buyerAddress;

    @Schema(description = "商品编号")
    private String goodsId;
    @Schema(description = "skuId")
    private String skuId;

    @Schema(description = "合约编号")
    private Integer ctId;
    @Schema(description = "tokenIds")
    @JsonProperty("tokenIds")
    private String tokenIdJson;
    @Schema(description = "数量")
    private Integer num;

    @Schema(description = "哈希")
    private String txHash;

    @Schema(description = """
            状态
            0待发货
            1发货中
            2已收货
            3申请退货
            4不同意退货
            5已同意退货，用户填写退货单号
            6退货中
            7已退货
            """)
    private Integer status;

    @Schema(description = "收货人名称")
    private String name;
    @Schema(description = "收货人手机号")
    private String phone;
    @Schema(description = "收货人详细地址")
    private String detail;
    @Schema(description = "收货人备注")
    private String note;

    @Schema(description = "物流编号")
    private String expressCode;

    @Schema(description = "下单时间")
    private Long orderTime;
    @Schema(description = "发货时间")
    private Long expressTime;
    @Schema(description = "收货时间")
    private Long confirmTime;
    @Schema(description = "自动收货时间")
    private Long confirmAutoTime;

    @Schema(description = "退货备注")
    private String refundNote;
    @Schema(description = "退货物流编号")
    private String refundExpressCode;
    @Schema(description = "退货申请时间")
    private Long refundApplyTime;
    @Schema(description = "退货审核时间")
    private Long refundAuditTime;
    @Schema(description = "退货确认时间")
    private Long refundConfirmTime;
    @Schema(description = "退货自动确认时间")
    private Long refundConfirmAutoTime;

    @Schema(description = "商户同意后，链上转账状态：0未执行，1成功，2失败")
    private Integer refundTxStatus;
    @Schema(description = "商户同意后，链上转账哈希")
    private String refundTxHash;
    @Schema(description = "商户同意后，链上转账备注")
    private String refundTxNote;
}
