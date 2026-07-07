package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MOrderExpressRO {
    @Schema(description = "订单编号")
    private String orderId;

    @Schema(description = "快递单号，多个单号用逗号分隔")
    private String expressCode;
}
