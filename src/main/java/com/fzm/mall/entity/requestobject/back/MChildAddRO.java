package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MChildAddRO {
    @Schema(description = "地址")
    private String address;

    @Schema(description = "子角色，0无")
    private Integer childRole;
}
