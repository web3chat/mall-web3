package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AuthorizationVO {
    @Schema(description = "令牌")
    private String authorization;
}
