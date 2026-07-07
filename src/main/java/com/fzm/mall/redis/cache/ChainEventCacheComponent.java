package com.fzm.mall.redis.cache;

import com.fzm.mall.redis.RedisCacheComponent;
import com.fzm.mall.redis.key.RedisCacheKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChainEventCacheComponent {
    private final RedisCacheComponent redisCacheComponent;

    public BigInteger getConfirmHeight() {
        try {
            Object obj = redisCacheComponent.getObj(RedisCacheKey.ChainEvent.confirm_height);
            if (obj == null) {
                return BigInteger.ZERO;
            }
            return (BigInteger) obj;
        } catch (Exception e) {
            return null;
        }
    }

    // =============================================
    public void setConfirmHeight(BigInteger height) {
        redisCacheComponent.setObj(RedisCacheKey.ChainEvent.confirm_height, height);
    }

}
