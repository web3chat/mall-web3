package com.fzm.mall.redis.cache;

import com.fzm.mall.redis.RedisCacheComponent;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.key.RedisCacheKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthenticationCacheComponent {
    private final RedisCacheComponent redisCacheComponent;


    // =============================================
    public void setAuthorizationToAddress(String authorization, String address) {
        String key = String.format(RedisCacheKey.Authorization.to_address, authorization);
        redisCacheComponent.setStr(key, address, RedisCacheExpireEnum.expire_days_7);
    }

    public String getAuthorizationToAddress(String authorization) {
        String key = String.format(RedisCacheKey.Authorization.to_address, authorization);
        return redisCacheComponent.getStr(key);
    }

    public void delAuthorizationToAddress(String authorization) {
        String key = String.format(RedisCacheKey.Authorization.to_address, authorization);
        redisCacheComponent.del(key);
    }

}
