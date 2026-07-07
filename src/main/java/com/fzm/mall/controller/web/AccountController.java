package com.fzm.mall.controller.web;

import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.common.PageQO;
import com.fzm.mall.entity.common.PageVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.UserDO;
import com.fzm.mall.entity.queryobject.back.MUserPageQO;
import com.fzm.mall.entity.requestobject.UserRO;
import com.fzm.mall.entity.viewobject.UserVO;
import com.fzm.mall.service.UserService;
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

@Tag(name = "当前登录的账号")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/v/account", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {
    private final UserService userService;

    @Operation(summary = "信息")
    @GetMapping("/info")
    public ResponseVO<UserVO> info() {
        UserDO userDO = userService.getByAddress(ThreadInfo.getInfo().getAddress());
        UserVO userVO = BeanCopierUtils.copy(userDO, UserVO.class);
        return ResponseUtils.success(userVO);
    }


    @Operation(summary = "信息更新")
    @PostMapping("/update-info")
    public ResponseVO<Object> updateInfo(@RequestBody UserRO userRO) {
        String nickname = userRO.getNickname();
        AssertUtils.isNotBlank(nickname, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(nickname.length() <= 16, ResponseEnum.invalid_parameter);

        String headUrl = userRO.getHeadUrl();

        UserDO updateUserDO = new UserDO();
        updateUserDO.setAddress(ThreadInfo.getInfo().getAddress());
        updateUserDO.setNickname(nickname);
        updateUserDO.setHeadUrl("");
        userService.updateByAddress(updateUserDO);

        return ResponseUtils.success();
    }


    @Operation(summary = "指定用户信息")
    @Parameter(name = "address", description = "UID或者地址", in = ParameterIn.QUERY)
    @GetMapping("/user-info")
    public ResponseVO<UserVO> userInfo(@RequestParam("address") String address) {
        UserDO userDO = null;

        try {
            address = ParamsUtils.address(address);
            userDO = userService.getByAddress(address);
        } catch (ValidateException e) {
            try {
                long uid = Long.parseLong(address);
                userDO = userService.getByUid(uid);
            } catch (Exception ignored) {
            }
        }
        UserVO userVO = BeanCopierUtils.copy(userDO, UserVO.class);

        return ResponseUtils.success(userVO);
    }


    @Operation(summary = "邀请的用户列表")
    @PostMapping("/invite/page")
    public ResponseVO<PageVO<UserVO>> invitePage(@RequestBody PageQO pageQO) {
        MUserPageQO userPageQO = BeanCopierUtils.copy(pageQO, MUserPageQO.class);
        userPageQO.setParentAddress(ThreadInfo.getInfo().getAddress());

        PageInfo<UserDO> pageInfo = userService.page(userPageQO);
        return PageVOUtils.pageVO(pageInfo, UserVO.class);
    }
}
