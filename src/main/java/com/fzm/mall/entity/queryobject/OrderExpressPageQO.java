package com.fzm.mall.entity.queryobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderExpressPageQO extends VaguePageQO {
    @Schema(description = "状态，-1全部，0待发货，1已发货，2已收货", example = "-1")
    private Integer status;
}
