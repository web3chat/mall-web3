package com.fzm.mall.redis.cache;

import com.fzm.mall.redis.RedisCacheComponent;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.key.RedisCacheKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SIWE 待签名消息缓存：以规范化地址 + 设备标识为键存储/读取/消费原文，实现设备隔离。
 */
@Component
@RequiredArgsConstructor
public class SiweMessageCache {

    private static final String LUA_GET_AND_DEL = """
            local val = redis.call('GET', KEYS[1])
            if val then
              redis.call('DEL', KEYS[1])
            end
            return val
            """;

    private static final DefaultRedisScript<String> GET_AND_DEL_SCRIPT;

    static {
        GET_AND_DEL_SCRIPT = new DefaultRedisScript<>();
        GET_AND_DEL_SCRIPT.setScriptText(LUA_GET_AND_DEL);
        GET_AND_DEL_SCRIPT.setResultType(String.class);
    }

    private final RedisCacheComponent redisCacheComponent;

    /**
     * 写入 address + deviceId → message，TTL 与 SIWE 有效期一致。
     *
     * @param normalizedAddress 规范化后的钱包地址
     * @param message           SIWE 原文
     */
    public void save(String normalizedAddress, String message) {
        String key = RedisCacheKey.Siwe.contentByAddressAndDevice(normalizedAddress);
        redisCacheComponent.setStr(key, message, RedisCacheExpireEnum.expire_minutes_30);
    }

    /**
     * 原子读取并删除 address + deviceId 对应的 SIWE 原文（Lua 保证 GET + DEL 不可分割）。
     *
     * @param normalizedAddress 规范化后的钱包地址
     * @return 缓存的 SIWE 原文
     */
    public String getAndConsume(String normalizedAddress) {
        String key = RedisCacheKey.Siwe.contentByAddressAndDevice(normalizedAddress);
        return redisCacheComponent.executeLua(GET_AND_DEL_SCRIPT, List.of(key));
    }

}
