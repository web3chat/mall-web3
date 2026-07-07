package com.fzm.mall.redis.cache;

import com.fzm.mall.constant.enums.ChainEnum;
import com.fzm.mall.redis.RedisCacheComponent;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.key.RedisCacheKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PriceCacheComponent {
    private final RedisCacheComponent redisCacheComponent;

    // =============================================
    public void setPrice(ChainEnum.CoinEnum coinEnum, BigDecimal price, RedisCacheExpireEnum expireEnum) {
        String key = String.format(RedisCacheKey.CoinPrice.price, coinEnum.name(), ChainEnum.CoinEnum.CNY.name());
        redisCacheComponent.setObj(key, price, expireEnum);
    }

    public BigDecimal getPrice(ChainEnum.CoinEnum coinEnum) {
        String key = String.format(RedisCacheKey.CoinPrice.price, coinEnum.name(), ChainEnum.CoinEnum.CNY.name());
        try {
            return (BigDecimal) redisCacheComponent.getObj(key);
        } catch (Exception e) {
            return null;
        }
    }

    // =============================================
    public void setPriceOfAddress(ChainEnum.CoinEnum fromCoin, String address, BigDecimal price) {
        if (StringUtils.isNotBlank(address)) {
            String key = String.format(RedisCacheKey.CoinPrice.price_by_address, fromCoin.name(), ChainEnum.CoinEnum.CNY.name(), address);
            redisCacheComponent.setObj(key, price, RedisCacheExpireEnum.expire_minutes_10);
        }
    }

    public BigDecimal getPriceOfAddress(ChainEnum.CoinEnum fromCoin, String address) {
        String key = String.format(RedisCacheKey.CoinPrice.price_by_address, fromCoin.name(), ChainEnum.CoinEnum.CNY.name(), address);
        try {
            return (BigDecimal) redisCacheComponent.getObj(key);
        } catch (Exception ignored) {
            return getPrice(fromCoin);
        }
    }

}
