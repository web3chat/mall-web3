package com.fzm.mall.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisAtomicComponent {
    private final RedisTemplate<String, Object> redisTemplate;

    public long incrementAndGetAndExpire(String key, RedisCacheExpireEnum expireEnum) {
        return atomicLong(key, expireEnum).incrementAndGet();
    }

    public long incrementAndGet(String key) {
        return atomicLong(key, null).incrementAndGet();
    }

    private RedisAtomicLong atomicLong(String key, RedisCacheExpireEnum expireEnum) {
        RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, Objects.requireNonNull(redisTemplate.getConnectionFactory()));
        if (expireEnum != null) {
            Long expire = redisAtomicLong.getExpire();
            if (expire == null || expire < 0) {
                redisAtomicLong.expire(expireEnum.getTimeout(), expireEnum.getTimeUnit());
            }
        }
        return redisAtomicLong;
    }
}
