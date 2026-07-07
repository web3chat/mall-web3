package com.fzm.mall.controller.manage;

import com.fzm.mall.annotation.UnPermission;
import com.fzm.mall.component.AuthRoleComponent;
import com.fzm.mall.constant.enums.CommonEnum;
import com.fzm.mall.constant.enums.FileEnum;
import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.requestobject.back.MApplyStatusRO;
import com.fzm.mall.entity.requestobject.back.MUserAdminRO;
import com.fzm.mall.entity.viewobject.back.MAuthPermissionTreeVO;
import com.fzm.mall.entity.viewobject.back.MUserAdminVO;
import com.fzm.mall.service.AuthService;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.third.component.FileComponent;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name = "当前登录的账号")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/account", produces = MediaType.APPLICATION_JSON_VALUE)
public class MAccountController {
    private final AuthService authService;
    private final UserAdminService userAdminService;
    private final AuthRoleComponent authRoleComponent;
    private final FileComponent fileComponent;

    @UnPermission
    @Operation(summary = "信息")
    @GetMapping("/info")
    public ResponseVO<MUserAdminVO> info() {
        UserAdminDO userAdminDO = userAdminService.getByAddress(ThreadInfo.getInfo().getAddress());
        MUserAdminVO userAdminVO = BeanCopierUtils.copy(userAdminDO, MUserAdminVO.class);
        authRoleComponent.setRoleName(userAdminVO);
        return ResponseUtils.success(userAdminVO);
    }

    @Operation(summary = "信息更新")
    @PostMapping("/info-update")
    public ResponseVO<Object> infoUpdate(@RequestBody MUserAdminRO adminRO) {
        String nickname = adminRO.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            AssertUtils.isTrue(nickname.length() <= 16, "nickname length must <= 16");
        }

        String headUrl = adminRO.getHeadUrl();
        if (StringUtils.isNotBlank(headUrl)) {
            fileComponent.verifyFileUrl(headUrl, FileEnum.image, "headUrl只能使用图片");
        }

        if (StringUtils.isAllBlank(nickname, headUrl)) {
            return ResponseUtils.success();
        }

        UserAdminDO updateAdminDO = new UserAdminDO();
        updateAdminDO.setAddress(ThreadInfo.getInfo().getAddress());
        updateAdminDO.setNickname(nickname);
        updateAdminDO.setHeadUrl(headUrl);
        userAdminService.updateByAddress(updateAdminDO);

        return ResponseUtils.success();
    }

    @UnPermission
    @Operation(summary = "权限")
    @GetMapping("/permission")
    public ResponseVO<List<MAuthPermissionTreeVO>> permission() {
        if (!ThreadInfo.getInfo()._isActiveRole()) {
            return ResponseUtils.success(Collections.emptyList());
        }

        List<MAuthPermissionTreeVO> tree = authService.getPermissionTree(ThreadInfo.getInfo(), null);
        return ResponseUtils.success(tree);
    }

    @UnPermission
    @Operation(summary = "子账号请求处理")
    @PostMapping("/child-audit")
    public ResponseVO<Object> childAudit(@RequestBody MApplyStatusRO statusRO) {
        CommonEnum.BoolEnum boolEnum = CommonEnum.BoolEnum.exist(statusRO.getStatus());
        AssertUtils.isNotNull(boolEnum, ResponseEnum.invalid_parameter);
        AssertUtils.isFalse(ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.none.getRole(), ResponseEnum.permission_denied);
        AssertUtils.isFalse(ThreadInfo.getInfo().getChildRole() == UserEnum.ChildRoleEnum.none.getRole(), ResponseEnum.permission_denied);
        AssertUtils.isTrue(ThreadInfo.getInfo().getApplyStatus() == UserEnum.ApplyStatusEnum.wait.getStatus(), ResponseEnum.permission_denied);

        userAdminService.childAudit(ThreadInfo.getInfo().getAddress(), boolEnum);

        return ResponseUtils.success();
    }
}
