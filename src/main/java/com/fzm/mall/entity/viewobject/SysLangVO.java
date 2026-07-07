package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SysLangVO {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "语音")
    private String lang;
}
