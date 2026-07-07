package com.fzm.mall.entity.viewobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MSysMenuVO {
    @Schema(description = "编号")
    private Integer menuId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面")
    private String cover;

    @Schema(description = "状态，0未上线，1已上线")
    private Integer status;
    @Schema(description = "排序，越小越前")
    private Integer showOrder;

    @Schema(description = "创建时间")
    private Long createTime;

}
