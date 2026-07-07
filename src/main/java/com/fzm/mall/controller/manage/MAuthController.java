package com.fzm.mall.controller.manage;

import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.AuthRoleDO;
import com.fzm.mall.entity.requestobject.back.MRolePermissionRO;
import com.fzm.mall.entity.requestobject.back.MRoleRO;
import com.fzm.mall.entity.viewobject.back.MAuthPermissionTreeVO;
import com.fzm.mall.entity.viewobject.back.MAuthRoleVO;
import com.fzm.mall.service.AuthService;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "权限")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class MAuthController {
    private final AuthService authService;

    @Operation(summary = "角色列表")
    @GetMapping("/role/list")
    public ResponseVO<List<MAuthRoleVO>> listRole() {
        List<AuthRoleDO> dos = authService.listRoleByAddress(ThreadInfo.getInfo().getParentAddress());
        List<MAuthRoleVO> vos = BeanCopierUtils.copyList(dos, MAuthRoleVO.class);
        return ResponseUtils.success(vos);
    }

    @Operation(summary = "角色添加/编辑")
    @PostMapping("/role/add-update")
    public ResponseVO<Object> addUpdateRole(@RequestBody MRoleRO roleRO) {
        String name = roleRO.getName();
        AssertUtils.isNotBlank(name, "name为空");
        AssertUtils.isTrue(name.length() <= 32, "name最多32个字");

        authService.addOrUpdateRole(ThreadInfo.getInfo().getParentAddress(), roleRO.getChildRole(), name);

        return ResponseUtils.success();
    }

    @Operation(summary = "角色删除")
    @PostMapping("/role/delete")
    public ResponseVO<Object> deleteRole(@RequestBody MRoleRO roleRO) {
        AuthRoleDO roleDO = authService.getRoleByChildRole(roleRO.getChildRole());
        AssertUtils.isNotNull(roleDO, "角色编号错误");
        AssertUtils.isTrue(roleDO.getAddress().equals(ThreadInfo.getInfo().getParentAddress()), "角色编号错误");

        authService.deleteRoleByChildRole(roleRO.getChildRole());

        return ResponseUtils.success();
    }

    @Operation(summary = "角色的权限列表")
    @Parameter(name = "childRole", description = "子角色编号", in = ParameterIn.QUERY)
    @GetMapping("/permission/list")
    public ResponseVO<List<MAuthPermissionTreeVO>> listPermission(@RequestParam("childRole") Integer childRole) {
        AssertUtils.isNotNull(childRole, "角色编号错误");
        AuthRoleDO roleDO = authService.getRoleByChildRole(childRole);
        AssertUtils.isNotNull(roleDO, "角色编号错误");
        AssertUtils.isTrue(roleDO.getAddress().equals(ThreadInfo.getInfo().getParentAddress()), "角色编号错误");

        List<MAuthPermissionTreeVO> tree = authService.getPermissionTree(ThreadInfo.getInfo(), childRole);

        return ResponseUtils.success(tree);
    }

    @Operation(summary = "角色的权限修改")
    @PostMapping("/permission/update")
    public ResponseVO<Object> updatePermission(@RequestBody MRolePermissionRO rolePermissionRO) {
        AssertUtils.isNotNull(rolePermissionRO.getChildRole(), "角色编号错误");
        AuthRoleDO roleDO = authService.getRoleByChildRole(rolePermissionRO.getChildRole());
        AssertUtils.isNotNull(roleDO, "角色编号错误");
        AssertUtils.isTrue(roleDO.getAddress().equals(ThreadInfo.getInfo().getParentAddress()), "角色编号错误");

        authService.updateChildRolePermission(ThreadInfo.getInfo().getRole(), rolePermissionRO.getChildRole(), rolePermissionRO.getPermissionIds());

        return ResponseUtils.success();
    }
}
