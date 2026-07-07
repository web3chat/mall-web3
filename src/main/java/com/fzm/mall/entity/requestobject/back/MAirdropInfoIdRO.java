package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MAirdropInfoIdRO {
    @Schema(description = "编号")
    private Long infoId;
}
