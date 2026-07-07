package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderIdRO {
    @Schema(description = "订单编号")
    private String orderId;
}
