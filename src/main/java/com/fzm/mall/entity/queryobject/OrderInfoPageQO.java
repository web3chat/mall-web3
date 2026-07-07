package com.fzm.mall.entity.queryobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderInfoPageQO extends VaguePageQO {
    @Schema(description = "状态，-1全部，0待支付，1已支付，2已撤销，3已超时", example = "-1")
    private Integer status;
}
