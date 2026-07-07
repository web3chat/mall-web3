package com.fzm.mall.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisLockComponent {
    private static final String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private final StringRedisTemplate stringRedisTemplate;

    public boolean lock(String key, String value, RedisCacheExpireEnum expireEnum) {
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, value, expireEnum.getTimeout(), expireEnum.getTimeUnit());
        return ok != null && ok;
    }

    public void unlock(String key, String value) {
        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Boolean.class);
        redisScript.setScriptText(luaScript);
        List<String> keys = new ArrayList<>();
        keys.add(key);

        stringRedisTemplate.execute(redisScript, keys, value);
    }
}
