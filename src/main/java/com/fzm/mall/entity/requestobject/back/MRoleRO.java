package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class MRoleRO {
    @Schema(description = "角色编号")
    private Integer childRole;
    @Schema(description = "名称")
    private String name;
}
