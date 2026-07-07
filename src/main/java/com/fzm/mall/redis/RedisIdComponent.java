package com.fzm.mall.redis;

import com.fzm.mall.redis.key.RedisCacheKey;
import com.fzm.mall.third.chain.util.TokenUtils;
import com.fzm.mall.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisIdComponent {
    private final RedisAtomicComponent redisAtomicComponent;

    private static String fillingZero(long serial, int length) {
        String format = "%0" + length + "d";
        return String.format(format, serial);
    }

    public long nextTokenPrefix() {
        return redisAtomicComponent.incrementAndGet(RedisCacheKey.Token.serial_prefix);
    }

    public List<Long> getTokenIds(String skuId, long prefix, int num) {
        List<Long> tokenIds = new ArrayList<>();

        String key = String.format(RedisCacheKey.Token.serial_sku_id, skuId);
        for (int i = 0; i < num; i++) {
            long serial = redisAtomicComponent.incrementAndGet(key);
            long tokenId = TokenUtils.tokenId(prefix, serial);
            tokenIds.add(tokenId);
        }

        return tokenIds;
    }

    /**
     * 获取商品编号 16位
     *
     * @return 商品编号
     */
    public String nextSpuId() {
        // 8
        String dateYmd = TimeUtils.nowDateTime().format(TimeUtils.yyyyMMdd);
        // 6
        String redisIdKey = String.format(RedisCacheKey.Id.spu_id, dateYmd);
        long serial = redisAtomicComponent.incrementAndGetAndExpire(redisIdKey, RedisCacheExpireEnum.expire_days_7);
        String seqFormat = fillingZero(serial, 6);
        // 2
        String randomNumeric = RandomStringUtils.secure().nextNumeric(2);

        return dateYmd + seqFormat + randomNumeric;
    }

    public String nextSkuId() {
        // 8
        String dateYmd = TimeUtils.nowDateTime().format(TimeUtils.yyyyMMdd);
        // 8
        String redisIdKey = String.format(RedisCacheKey.Id.sku_id, dateYmd);
        long serial = redisAtomicComponent.incrementAndGetAndExpire(redisIdKey, RedisCacheExpireEnum.expire_days_7);
        String seqFormat = fillingZero(serial, 8);

        return dateYmd + seqFormat;
    }

    /**
     * 获取订单编号 18位
     *
     * @return 订单编号
     */
    public String nextOrderId() {
        // 8
        String dateYmd = TimeUtils.nowDateTime().format(TimeUtils.yyyyMMdd);
        // 8
        String redisIdKey = String.format(RedisCacheKey.Id.order_id, dateYmd);
        long serial = redisAtomicComponent.incrementAndGetAndExpire(redisIdKey, RedisCacheExpireEnum.expire_days_7);
        String seqFormat = fillingZero(serial, 8);
        // 2
        String randomNumeric = RandomStringUtils.secure().nextNumeric(2);

        return dateYmd + seqFormat + randomNumeric;
    }
}
