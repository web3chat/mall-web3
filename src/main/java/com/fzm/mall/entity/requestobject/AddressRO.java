package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AddressRO {
    @Schema(description = "地址")
    private String address;
}
