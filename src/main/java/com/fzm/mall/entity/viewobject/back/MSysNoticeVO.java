package com.fzm.mall.entity.viewobject.back;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MSysNoticeVO {
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "类目")
    @JsonProperty("classifies")
    private String classifyJson;

    @Schema(description = "标题，最多32个字符")
    private String title;
    @Schema(description = "内容")
    private String content;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "状态，0未上线，1已上线")
    private Integer status;
    @Schema(description = "置顶，0否，1是")
    private Integer top;
    @Schema(description = "滚动，0否，1是")
    private Integer scroll;

    @Schema(description = "上架时间")
    private Long activeTime;
    @Schema(description = "创建时间")
    private Long createTime;

}
