package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MLogIdAuditRO {
    @Schema(description = "记录编号")
    private Long logId;

    @Schema(description = "0不同意，1同意")
    private Integer status;

    @Schema(description = "备注，可为空")
    private String note;
}
