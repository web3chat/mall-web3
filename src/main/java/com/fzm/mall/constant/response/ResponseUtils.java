package com.fzm.mall.constant.response;

import com.fzm.mall.entity.common.ThreadInfo;
import org.springframework.context.support.ResourceBundleMessageSource;

public class ResponseUtils {
    private static final ResourceBundleMessageSource messageSource;

    static {
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        // 禁用系统语言回退
        messageSource.setFallbackToSystemLocale(false);
        // 若键不存在，直接返回键名
        messageSource.setUseCodeAsDefaultMessage(true);
    }

    public static <T> ResponseVO<T> success(T data) {
        String message = messageSource.getMessage(ResponseEnum.success.name(), null, ThreadInfo.getInfo().getLangEnum().getLocale());

        return new ResponseVO<>(ResponseEnum.success.getCode(), message, data);
    }

    public static ResponseVO<Object> success() {
        return success(null);
    }

    public static ResponseVO<Object> error(ResponseEnum responseEnum, Object... args) {
        String message = messageSource.getMessage(responseEnum.name(), args, ThreadInfo.getInfo().getLangEnum().getLocale());

        return new ResponseVO<>(responseEnum.getCode(), message);
    }

    public static ResponseVO<Object> error(String message) {
        return new ResponseVO<>(ResponseEnum.failure.getCode(), message);
    }
}
