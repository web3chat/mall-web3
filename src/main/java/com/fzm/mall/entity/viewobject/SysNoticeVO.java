package com.fzm.mall.entity.viewobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysNoticeVO {
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "类目")
    @JsonProperty("classifies")
    private String classifyJson;

    @Schema(description = "标题")
    private String title;
    @Schema(description = "内容")
    private String content;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "上架时间")
    private Long activeTime;

}
