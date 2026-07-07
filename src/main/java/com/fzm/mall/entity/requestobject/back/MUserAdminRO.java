package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MUserAdminRO {
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像")
    private String headUrl;
}
