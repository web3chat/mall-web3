package com.fzm.mall.controller.manage;

import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.annotation.UnPermission;
import com.fzm.mall.component.SignatureComponent;
import com.fzm.mall.constant.SystemConstant;
import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ResponseUtils;
import com.fzm.mall.constant.response.ResponseVO;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.UserAdminDO;
import com.fzm.mall.entity.requestobject.LoginRO;
import com.fzm.mall.entity.viewobject.AuthorizationVO;
import com.fzm.mall.entity.viewobject.PresignContentVO;
import com.fzm.mall.redis.cache.AuthenticationCacheComponent;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.TimeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "登录登出")
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(value = "/m/access", produces = MediaType.APPLICATION_JSON_VALUE)
public class MAccessController {
    private final UserAdminService userAdminService;
    private final SignatureComponent signatureComponent;
    private final AuthenticationCacheComponent authenticationCacheComponent;

    @UnAuthorization
    @Operation(summary = "登录 - 签名原文")
    @Parameter(name = "address", description = "地址", in = ParameterIn.QUERY)
    @GetMapping("/content")
    public ResponseVO<PresignContentVO> presignContent(@RequestParam("address") String address) {
        String presignContent = signatureComponent.getPresignContent(address, "");
        return ResponseUtils.success(new PresignContentVO(presignContent));
    }

    @UnAuthorization
    @Operation(summary = "登录")
    @PostMapping("/login")
    public ResponseVO<AuthorizationVO> login(@RequestBody LoginRO loginRO) {
        signatureComponent.valid(loginRO);
        String address = loginRO.getAddress();

        UserAdminDO selectUser = userAdminService.getByAddress(address);
        // 用户不存在时，则注册
        if (selectUser == null) {
            userAdminService.register(address);
            selectUser = userAdminService.getByAddress(address);
        }

        // 登录时校验一次用户是否被冻结
        AssertUtils.isTrue(selectUser.getStatus() == UserEnum.StatusEnum.normal.getStatus(), ResponseEnum.account_frozen);

        // 更新最近登录时间
        UserAdminDO updateUser = new UserAdminDO();
        updateUser.setAddress(address);
        updateUser.setLatestLoginIp(ThreadInfo.getInfo().getClientIp());
        updateUser.setLatestLoginTime(TimeUtils.nowTimestamp());
        // 其他方式添加的用户，第一次登录时的ip为注册ip
        if (StringUtils.isBlank(selectUser.getRegisterIp())) {
            updateUser.setRegisterIp(ThreadInfo.getInfo().getClientIp());
        }
        userAdminService.updateByAddress(updateUser);

        // 获取新的authorization，并且缓存
        String authorization = DigestUtils.md5Hex(UUID.randomUUID().toString());
        authenticationCacheComponent.setAuthorizationToAddress(authorization, address);
        // 返回信息
        AuthorizationVO authorizationVO = new AuthorizationVO();
        authorizationVO.setAuthorization(authorization);

        return ResponseUtils.success(authorizationVO);
    }

    @UnPermission
    @Operation(summary = "登出")
    @GetMapping("/logout")
    public ResponseVO<Object> logout(@Parameter(hidden = true) @RequestHeader(value = SystemConstant.Header.authorization_name, required = false) String authentication) {
        // 删除缓存中authorization对应的信息
        authenticationCacheComponent.delAuthorizationToAddress(authentication);
        return ResponseUtils.success();
    }
}
