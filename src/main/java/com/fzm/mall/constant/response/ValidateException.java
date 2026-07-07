package com.fzm.mall.constant.response;

import lombok.Getter;

/**
 * 验证异常
 */
@Getter
public class ValidateException extends RuntimeException {
    private final ResponseEnum source;
    private final Object[] args;

    public ValidateException(ResponseEnum source, Object... args) {
        super(source.name());
        this.source = source;
        this.args = args;
    }

    public ValidateException(Object... args) {
        this(ResponseEnum.invalid_parameter_with_args, args);
    }

}
