package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MLogIdRO {
    @Schema(description = "记录编号")
    private Long logId;
}
