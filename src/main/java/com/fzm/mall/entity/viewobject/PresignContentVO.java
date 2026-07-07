package com.fzm.mall.entity.viewobject;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignContentVO(
        @Schema(description = "原始消息")
        String content
) {

}
