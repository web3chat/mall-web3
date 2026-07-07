package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MApplyStatusRO {
    @Schema(description = "状态，0不同意，1同意")
    private Integer status;
}
