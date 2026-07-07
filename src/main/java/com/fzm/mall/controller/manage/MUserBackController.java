package com.fzm.mall.controller.manage;

import com.fzm.mall.component.AuthRoleComponent;
import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.queryobject.back.MUserAdminPageQO;
import com.fzm.mall.entity.requestobject.AddressRO;
import com.fzm.mall.entity.requestobject.back.MChildAddRO;
import com.fzm.mall.entity.viewobject.back.MUserAdminVO;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.BeanCopierUtils;
import com.fzm.mall.util.PageVOUtils;
import com.fzm.mall.util.ParamsUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户-后台用户")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/user/back", produces = MediaType.APPLICATION_JSON_VALUE)
public class MUserBackController {
    private final UserAdminService userAdminService;
    private final AuthRoleComponent authRoleComponent;

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public ResponseVO<PageVO<MUserAdminVO>> page(@RequestBody MUserAdminPageQO pageQO) {
        if (ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            pageQO.setParentAddress(ThreadInfo.getInfo().getParentAddress());
        }

        PageInfo<UserAdminDO> pageInfo = userAdminService.page(pageQO);
        ResponseVO<PageVO<MUserAdminVO>> responseVO = PageVOUtils.pageVO(pageInfo, MUserAdminVO.class);
        authRoleComponent.setRoleName(responseVO.getData().getData());

        return responseVO;
    }

    @Operation(summary = "指定地址的信息")
    @Parameter(name = "address", description = "地址", in = ParameterIn.QUERY)
    @GetMapping("/info")
    public ResponseVO<MUserAdminVO> info(@RequestParam("address") String address) {
        address = ParamsUtils.address(address);
        UserAdminDO userAdminDO = userAdminService.getByAddress(address);

        if (userAdminDO != null && ThreadInfo.getInfo().getRole() == UserEnum.RoleEnum.merchant.getRole()) {
            AssertUtils.isTrue(userAdminDO.getParentAddress().equals(ThreadInfo.getInfo().getParentAddress()), ResponseEnum.permission_denied);
        }

        MUserAdminVO userAdminVO = BeanCopierUtils.copy(userAdminDO, MUserAdminVO.class);
        authRoleComponent.setRoleName(userAdminVO);
        return ResponseUtils.success(userAdminVO);
    }

    @Operation(summary = "添加/编辑子账号")
    @PostMapping("/child/add-update")
    public ResponseVO<Object> addUpdateChild(@RequestBody MChildAddRO childAddRO) {
        String address = ParamsUtils.address(childAddRO.getAddress());

        AssertUtils.isNotNull(childAddRO.getChildRole(), "角色编号为空");
        AssertUtils.isTrue(childAddRO.getChildRole() > 0, "角色编号错误");

        userAdminService.addChild(address, ThreadInfo.getInfo().getParentAddress(), ThreadInfo.getInfo().getRole(), childAddRO.getChildRole());

        return ResponseUtils.success();
    }

    @Operation(summary = "删除子账号")
    @PostMapping("/child/delete")
    public ResponseVO<Object> delChild(@RequestBody AddressRO addressRO) {
        String address = ParamsUtils.address(addressRO.getAddress());
        UserAdminDO selectAdminDO = userAdminService.getByAddress(address);
        AssertUtils.isNotNull(selectAdminDO, ResponseEnum.invalid_address_format);
        // 不能删除其他人的子账号
        AssertUtils.isTrue(selectAdminDO.getParentAddress().equals(ThreadInfo.getInfo().getParentAddress()), ResponseEnum.permission_denied);
        // 不能删除顶级账号
        AssertUtils.isFalse(selectAdminDO._isTopRole(), ResponseEnum.permission_denied);

        userAdminService.delChild(address);
        return ResponseUtils.success();
    }
}
