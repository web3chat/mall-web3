package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MGoodsStatusRO {
    @Schema(description = "商品编号")
    private String goodsId;
    @Schema(description = "3下架，4上架")
    private Integer status;
}
