package com.fzm.mall.entity.properties;

import com.fzm.mall.constant.enums.ChainEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "local.chain.pay")
public class ChainPaymentProperties {
    private Detail bty;
    private Detail bep20Usd;

    public Detail getByChainEnum(ChainEnum.TypeEnum typeEnum) {
        if (typeEnum == ChainEnum.TypeEnum.BTY) {
            if (typeEnum.getCoinEnum() == ChainEnum.CoinEnum.BTY) {
                return bty;
            }
        }
        if (typeEnum == ChainEnum.TypeEnum.BSC) {
            if (typeEnum.getCoinEnum() == ChainEnum.CoinEnum.USD) {
                return bep20Usd;
            }
        }
        return null;
    }

    public Detail getByCoinEnum(ChainEnum.CoinEnum coinEnum) {
        if (coinEnum == ChainEnum.CoinEnum.BTY) {
            return bty;
        }
        if (coinEnum == ChainEnum.CoinEnum.USD) {
            return bep20Usd;
        }
        return null;
    }

    @Data
    public static class Detail {
        // 是否开启支付
        private Boolean enable;
        // 链Url
        private String chainUrl;
        // 链ID
        private Long chainId;
        // 合约地址
        private String contractAddress;
        // 小数点位数
        private Integer decimals;
        // 计算时保留的小数点位数
        private Integer calcDecimals;
        // 行情
        private String usdtUrl;
    }
}
