package com.fzm.mall.crontab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fzm.mall.constant.enums.ChainEnum;
import com.fzm.mall.entity.properties.ChainPaymentProperties;
import com.fzm.mall.redis.RedisCacheExpireEnum;
import com.fzm.mall.redis.cache.PriceCacheComponent;
import com.fzm.mall.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CoinPriceCrontab {
    private final static BigDecimal min_usd_cny_price = new BigDecimal("6.8");
    private final static BigDecimal max_usd_cny_price = new BigDecimal("7.4");
    private static final ObjectMapper mapper = new ObjectMapper();
    private final PriceCacheComponent priceCacheComponent;
    private final ChainPaymentProperties chainPaymentProperties;

    public static BigDecimal getPrice(String url, String path) {
        String execute = HttpUtils.url(url).setMethod(HttpUtils.MethodEnum.GET).execute();

        if (StringUtils.isBlank(execute)) {
            return null;
        }

        try {
            JsonNode rootNode = mapper.readTree(execute);
            JsonNode jsonNode = rootNode.at(path);

            return new BigDecimal(jsonNode.asText());
        } catch (Exception e) {
            log.error("获取行情失败：url：{}", url, e);
        }

        return null;

    }

    // U的行情变化不大
    @Scheduled(cron = "0 5 8 * * ?")
    public void usdPrice() {
        // 这个接口免费套餐，北京时间8点，每天更新一次
        BigDecimal usd_cny_price = getPrice("https://v6.exchangerate-api.com/v6/4913882a755f9d52a7d5e103/latest/USD", "/conversion_rates/CNY");
        if (usd_cny_price == null || usd_cny_price.compareTo(min_usd_cny_price) < 0 || usd_cny_price.compareTo(max_usd_cny_price) > 0) {
            usd_cny_price = new BigDecimal("7.1");
        }
        priceCacheComponent.setPrice(ChainEnum.CoinEnum.USD, usd_cny_price, RedisCacheExpireEnum.expire_days_7);
    }

    // 币的行情变化比较快
    @Scheduled(cron = "0 0/1 * * * ?")
    public void coinPrice() {
        BigDecimal usd_cny_price = priceCacheComponent.getPrice(ChainEnum.CoinEnum.USD);
        if (usd_cny_price == null) {
            usdPrice();
            usd_cny_price = priceCacheComponent.getPrice(ChainEnum.CoinEnum.USD);
            if (usd_cny_price == null) {
                return;
            }
        }

        // BTY兑CNY
        if (chainPaymentProperties.getBty().getEnable()) {
            BigDecimal bty_usd_price = getPrice(chainPaymentProperties.getBty().getUsdtUrl(), "/data/data/USDT/BTY/last");

            if (bty_usd_price != null && bty_usd_price.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal bty_cny_price = bty_usd_price.multiply(usd_cny_price);
                priceCacheComponent.setPrice(ChainEnum.CoinEnum.BTY, bty_cny_price, RedisCacheExpireEnum.expire_minutes_10);
            }
        }
    }
}
