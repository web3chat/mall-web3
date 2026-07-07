package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysBannerVO {
    @Schema(description = "编号")
    private Long id;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "图片")
    private String cover;
    @Schema(description = "目标")
    private String target;

}
