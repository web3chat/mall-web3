package com.fzm.mall.entity.queryobject.back;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fzm.mall.entity.queryobject.VaguePageQO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MUserPageQO extends VaguePageQO {
    @Schema(description = "状态，-1全部", example = "-1")
    private Integer status;

    @Schema(description = "开始时间")
    private Long startTime;
    @Schema(description = "结束时间")
    private Long endTime;

    @JsonIgnore
    private String parentAddress;
}
