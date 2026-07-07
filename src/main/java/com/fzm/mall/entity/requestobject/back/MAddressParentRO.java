package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MAddressParentRO {
    @Schema(description = "地址")
    private String address;

    @Schema(description = "上级用户的地址")
    private String parentAddress;
}
