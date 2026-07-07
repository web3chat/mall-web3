package com.fzm.mall.configuration.interceptor;

import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.constant.SystemConstant;
import com.fzm.mall.constant.enums.LangEnum;
import com.fzm.mall.constant.enums.UserEnum;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.common.ReqInfo;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.redis.cache.AuthenticationCacheComponent;
import com.fzm.mall.service.UserAdminService;
import com.fzm.mall.service.UserService;
import com.fzm.mall.util.AssertUtils;
import com.fzm.mall.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final static String front_uri_prefix = "/v/";
    private final UserService userService;
    private final UserAdminService userAdminService;
    private final AuthenticationCacheComponent authenticationCacheComponent;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 所有请求都有
        setBaseInfo(new ReqInfo(), request);

        //
        Method method = handlerMethod.getMethod();
        Class<?> cls = method.getDeclaringClass();

        // 免登陆
        boolean isUnAuthorization = cls.isAnnotationPresent(UnAuthorization.class) || method.isAnnotationPresent(UnAuthorization.class);

        String authorization = ServletUtils.getHeader(request, SystemConstant.Header.authorization_name);
        if (StringUtils.isBlank(authorization)) {
            if (isUnAuthorization) {
                return true;
            }
            throw new ValidateException(ResponseEnum.not_logged_in);
        }

        String address = authenticationCacheComponent.getAuthorizationToAddress(authorization);
        if (StringUtils.isBlank(address)) {
            if (isUnAuthorization) {
                return true;
            }
            throw new ValidateException(ResponseEnum.login_expired);
        }

        ReqInfo reqInfo = request.getRequestURI().contains(front_uri_prefix) ? userService.getByAddress(address) : userAdminService.getByAddress(address);
        if (reqInfo == null) {
            authenticationCacheComponent.delAuthorizationToAddress(authorization);
            if (isUnAuthorization) {
                return true;
            }
            throw new ValidateException(ResponseEnum.login_expired);
        }

        // 是否冻结
        AssertUtils.isTrue(reqInfo.getStatus() == UserEnum.StatusEnum.normal.getStatus(), ResponseEnum.account_frozen);

        setBaseInfo(reqInfo, request);

        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, @NotNull Exception ex) throws Exception {
        ThreadInfo.delInfo();
    }

    private void setBaseInfo(ReqInfo reqInfo, HttpServletRequest request) {
        reqInfo.setClientIp(ServletUtils.getClientIP(request));
        reqInfo.setUserAgent(ServletUtils.getUserAgent(request));
        reqInfo.setLangEnum(LangEnum.getByRequest(request));

        ThreadInfo.setInfo(reqInfo);
    }
}
