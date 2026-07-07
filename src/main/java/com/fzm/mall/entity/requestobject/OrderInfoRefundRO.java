package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderInfoRefundRO {
    @Schema(description = "订单编号")
    private String orderId;

    @Schema(description = "退款打币哈希")
    private String txHash;

    @Schema(description = "备注")
    private String note;
}
