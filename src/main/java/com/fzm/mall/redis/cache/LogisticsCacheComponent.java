package com.fzm.mall.redis.cache;

import com.fzm.mall.entity.viewobject.LogisticsVO;
import com.fzm.mall.redis.RedisCacheComponent;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.key.RedisCacheKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LogisticsCacheComponent {
    private final RedisCacheComponent redisCacheComponent;

    // =============================================
    public void setOrderIdToLogistics(String orderId, Integer type, List<LogisticsVO> vos) {
        String key = String.format(RedisCacheKey.Order.logistics_by_order_id, orderId, type);
        redisCacheComponent.setObj(key, vos, RedisCacheExpireEnum.expire_minutes_10);
    }

    @SuppressWarnings("unchecked")
    public List<LogisticsVO> getOrderIdToLogistics(String orderId, Integer type) {
        String key = String.format(RedisCacheKey.Order.logistics_by_order_id, orderId, type);
        try {
            return (List<LogisticsVO>) redisCacheComponent.getObj(key);
        } catch (Exception e) {
            return null;
        }
    }
}
