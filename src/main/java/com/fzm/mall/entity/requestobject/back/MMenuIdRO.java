package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MMenuIdRO {
    @Schema(description = "编号")
    private Integer menuId;
}
