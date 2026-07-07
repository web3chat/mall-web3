package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MCoinNumberRO {
    @Schema(description = "币类型，0BTY，1USD")
    private Integer coinType;
    @Schema(description = "数量")
    private BigDecimal number;
}
