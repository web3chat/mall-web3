package com.fzm.mall.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisCacheComponent {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public void setStr(String key, String val) {
        stringRedisTemplate.opsForValue().set(key, val);
    }

    public void setObj(String key, Object val) {
        redisTemplate.opsForValue().set(key, val);
    }

    public void setStr(String key, String val, RedisCacheExpireEnum expireEnum) {
        stringRedisTemplate.opsForValue().set(key, val, expireEnum.getTimeout(), expireEnum.getTimeUnit());
    }

    public void setObj(String key, Object val, RedisCacheExpireEnum expireEnum) {
        redisTemplate.opsForValue().set(key, val, expireEnum.getTimeout(), expireEnum.getTimeUnit());
    }

    public String getStr(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public Object getObj(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void del(String key) {
        redisTemplate.delete(key);
    }

    public void setExpire(String key, RedisCacheExpireEnum expireEnum) {
        redisTemplate.expire(key, expireEnum.getTimeout(), expireEnum.getTimeUnit());
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 执行 Lua 脚本（返回 Long）
     */
    public <T> T executeLua(org.springframework.data.redis.core.script.DefaultRedisScript<T> script,
                            java.util.List<String> keys, Object... args) {
        return stringRedisTemplate.execute(script, keys, args);
    }
}
