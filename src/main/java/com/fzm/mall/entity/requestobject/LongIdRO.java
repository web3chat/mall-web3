package com.fzm.mall.entity.requestobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LongIdRO {
    @Schema(description = "编号")
    private Long id;
}
