package com.fzm.mall.configuration.interceptor;

import com.fzm.mall.annotation.UnAuthorization;
import com.fzm.mall.annotation.UnPermission;
import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.entity.common.ThreadInfo;
import com.fzm.mall.entity.dataobject.AuthPermissionDO;
import com.fzm.mall.mapper.AuthPermissionMapper;
import com.fzm.mall.mapper.AuthRolePermissionMapper;
import com.fzm.mall.util.AssertUtils;
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
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PermissionInterceptor implements HandlerInterceptor {
    private final AuthPermissionMapper authPermissionMapper;
    private final AuthRolePermissionMapper authRolePermissionMapper;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        Class<?> cls = method.getDeclaringClass();
        // 免登陆
        if (cls.isAnnotationPresent(UnAuthorization.class) || method.isAnnotationPresent(UnAuthorization.class)) {
            return true;
        }
        // 免权限
        if (cls.isAnnotationPresent(UnPermission.class) || method.isAnnotationPresent(UnPermission.class)) {
            return true;
        }

        AssertUtils.isTrue(ThreadInfo.getInfo()._isActiveRole(), ResponseEnum.permission_denied);

        // 权限
        List<AuthPermissionDO> permissionDOS;
        // 父类角色
        if (ThreadInfo.getInfo()._isTopRole()) {
            List<AuthPermissionDO> allPermissionDOS = authPermissionMapper.listAll();
            permissionDOS = allPermissionDOS.stream().filter(o -> o.getRole() == 0 || o.getRole().equals(ThreadInfo.getInfo().getRole())).toList();
        } else {
            // 子角色
            permissionDOS = authRolePermissionMapper.listByChildRole(ThreadInfo.getInfo().getChildRole());
        }

        String requestURI = request.getRequestURI().replaceAll("//", "/");
        boolean anyMatch = permissionDOS.stream().map(AuthPermissionDO::getUri).filter(StringUtils::isNotBlank).anyMatch(requestURI::equals);
        AssertUtils.isTrue(anyMatch, ResponseEnum.permission_denied);

        return true;
    }

}
