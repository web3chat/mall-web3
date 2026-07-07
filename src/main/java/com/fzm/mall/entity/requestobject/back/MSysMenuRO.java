package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MSysMenuRO {
    @Schema(description = "编号")
    private Integer menuId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "排序，越小越前")
    private Integer showOrder;
}
