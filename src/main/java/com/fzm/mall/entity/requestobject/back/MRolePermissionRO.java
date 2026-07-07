package com.fzm.mall.entity.requestobject.back;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MRolePermissionRO {
    @Schema(description = "角色编号")
    private Integer childRole;

    @Schema(description = "权限编号数组")
    private List<Integer> permissionIds;
}
