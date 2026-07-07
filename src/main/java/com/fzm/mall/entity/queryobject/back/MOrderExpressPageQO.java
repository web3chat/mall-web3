package com.fzm.mall.entity.queryobject.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fzm.mall.entity.queryobject.VaguePageQO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MOrderExpressPageQO extends VaguePageQO {
    @Schema(description = "状态，-1全部，0待发货，1已发货，2已收货", example = "-1")
    private Integer status;

    @JsonIgnore
    private Integer deleteStatus;

    @JsonIgnore
    private String sellerAddress;

    @JsonIgnore
    private String buyerAddress;
}
