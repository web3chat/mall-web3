package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MGoodsHiddenStatusRO {
    @Schema(description = "商品编号")
    private String goodsId;
    @Schema(description = "0否，1是")
    private Integer hiddenStatus;
}
