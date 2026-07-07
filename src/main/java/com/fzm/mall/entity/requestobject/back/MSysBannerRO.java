package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MSysBannerRO {
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "标题")
    private String title;
    @Schema(description = "图片")
    private String cover;
    @Schema(description = "目标")
    private String target;
    @Schema(description = "排序，越小越前")
    private Integer showOrder;

    @Schema(description = "开始时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long endTime;
}
