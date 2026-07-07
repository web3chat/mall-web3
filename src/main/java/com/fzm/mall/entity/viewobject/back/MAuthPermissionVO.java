package com.fzm.mall.entity.viewobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MAuthPermissionVO {
    @Schema(description = "权限编号")
    private Integer id;
    @Schema(description = "父级编号")
    private Integer parentId;
    @Schema(description = "名称")
    private String name;

    @Schema(description = "是否已选")
    private Boolean checked;
}
