package com.fzm.mall.redis.cache;

import com.fzm.mall.redis.RedisCacheComponent;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.key.RedisCacheKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SiweCacheComponent {
    private final RedisCacheComponent redisCacheComponent;

    // =============================================
    public void setNonce(String nonce, String address) {
        String key = String.format(RedisCacheKey.Siwe.nonce, nonce);
        redisCacheComponent.setStr(key, address, RedisCacheExpireEnum.expire_minutes_30);
    }

    public String getNonce(String nonce) {
        String key = String.format(RedisCacheKey.Siwe.nonce, nonce);
        return redisCacheComponent.getStr(key);
    }

    public void delNonce(String nonce) {
        String key = String.format(RedisCacheKey.Siwe.nonce, nonce);
        redisCacheComponent.del(key);
    }
}
