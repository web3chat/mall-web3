package com.fzm.mall.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public enum RedisCacheExpireEnum {
    expire_minutes_10(10, TimeUnit.MINUTES),
    expire_minutes_30(30, TimeUnit.MINUTES),
    expire_days_7(7, TimeUnit.DAYS),
    ;


    private final long timeout;
    private final TimeUnit timeUnit;
}
