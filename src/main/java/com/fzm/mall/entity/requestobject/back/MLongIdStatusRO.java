package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MLongIdStatusRO {
    @Schema(description = "编号")
    private Long id;
    @Schema(description = "0下架，1上架")
    private Integer status;
}
