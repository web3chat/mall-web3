package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRO {
    @Schema(description = "地址")
    private String address;
    @Schema(description = "签名原文")
    private String content;
    @Schema(description = "签名")
    private String signature;
}
