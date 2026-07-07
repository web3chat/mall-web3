package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderPayRO {
    @Schema(description = "订单编号")
    private String orderId;

    @Schema(description = "链类型，0BTY，1BSC")
    private Integer chainType;

    @Schema(description = "交易哈希")
    private String txHash;
}
