package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GoodsIdRO {
    @Schema(description = "商品编号")
    private String goodsId;
}
