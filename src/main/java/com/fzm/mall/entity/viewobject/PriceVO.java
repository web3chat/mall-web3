package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceVO {
    @Schema(description = "链类型，0BTY，1BSC")
    private Integer chainType;
    @Schema(description = "币类型，0BTY，1USDT")
    private Integer coinType;

    @Schema(description = "行情")
    private BigDecimal price;
    @Schema(description = "总价")
    private BigDecimal amount;

    @Schema(description = "支付地址")
    private String payAddress;
}
