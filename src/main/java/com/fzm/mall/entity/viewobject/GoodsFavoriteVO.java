package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GoodsFavoriteVO {
    @Schema(description = "收藏时间")
    private Long createTime;
}
