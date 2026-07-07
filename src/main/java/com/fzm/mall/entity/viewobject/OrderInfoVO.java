package com.fzm.mall.entity.viewobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderInfoVO {
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

    @Schema(description = "购买到的tokenIds")
    @JsonProperty("tokenIds")
    private String tokenIdJson;

    @Schema(description = "下单数量")
    private Integer num;
    @Schema(description = "单价")
    private BigDecimal price;
    @Schema(description = "总价")
    private BigDecimal amount;

    @Schema(description = "支付链，0BTY，1BSC")
    private Integer payChain;
    @Schema(description = "行情")
    private BigDecimal payPrice;
    @Schema(description = "支付总价")
    private BigDecimal payAmount;

    @Schema(description = "支付哈希")
    private String payHash;

    @Schema(description = "0待支付，1已支付，2已撤销，3已超时，4申请退款，5不同意，6同意")
    private Integer status;

    @Schema(description = "下单时间")
    private Long orderTime;
    @Schema(description = "支付时间")
    private Long payTime;
    @Schema(description = "过期时间")
    private Long expireTime;
    @Schema(description = "撤销时间")
    private Long cancelTime;

    @Schema(description = "支付成功后，链上转账状态：0未执行，1成功，2失败")
    private Integer txStatus;
    @Schema(description = "支付成功后，链上转账哈希")
    private String txHash;
    @Schema(description = "支付成功后，链上转账备注")
    private String txNote;

    @Schema(description = "退款备注")
    private String refundNote;

    @Schema(description = "退款用户打币哈希")
    private String refundApplyTxHash;

    @Schema(description = "退款行情")
    private BigDecimal refundPrice;
    @Schema(description = "退款总量")
    private BigDecimal refundAmount;

    @Schema(description = "商户同意后，链上转账状态：0未执行，1成功，2失败")
    private Integer refundTxStatus;
    @Schema(description = "商户同意后，链上转账哈希")
    private String refundTxHash;
    @Schema(description = "商户同意后，链上转账备注")
    private String refundTxNote;

    @Schema(description = "是否可退款")
    private Boolean canRefund;
}
