package com.fzm.mall.entity.viewobject.back;

import com.fzm.mall.entity.viewobject.SysBannerVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MSysBannerVO extends SysBannerVO {
    @Schema(description = "排序，越小越前")
    private Integer showOrder;

    @Schema(description = "状态，0未上线，1已上线")
    private Integer status;

    @Schema(description = "开始时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long endTime;

    @Schema(description = "创建时间")
    private Long createTime;
}
