package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MSysNoticeLocaleRO {
    @Schema(description = "语言")
    private String lang;
    private MSysNoticeLocaleDetailRO locale;
}
