package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GoodsWhiteVO {
    @Schema(description = "地址")
    private String address;
    @Schema(description = "数量")
    private Integer num;
}
