package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MSysNoticeLocaleDetailRO {
    @Schema(description = "标题，最多32个字符")
    private String title;
    @Schema(description = "内容")
    private String content;
}
