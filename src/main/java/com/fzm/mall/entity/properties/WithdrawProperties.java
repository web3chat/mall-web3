package com.fzm.mall.entity.properties;

import com.fzm.mall.constant.enums.ChainEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "local.chain.withdraw")
public class WithdrawProperties {
    private Withdraw bty;
    private Withdraw usd;

    public Withdraw getByCoinEnum(ChainEnum.CoinEnum coinEnum) {
        if (coinEnum == null) {
            return null;
        }
        if (coinEnum == ChainEnum.CoinEnum.BTY) {
            return bty;
        }
        if (coinEnum == ChainEnum.CoinEnum.USD) {
            return usd;
        }
        return null;
    }

    @Data
    public static class Withdraw {
        // 是否支持提币
        private Boolean enable;
        // 链Url
        private String chainUrl;
        // 链ID
        private Long chainId;
        // 合约地址
        private String contractAddress;
        // 小数点位数
        private Integer decimals;
        // 最小提币数量
        private BigDecimal minNumber;
    }
}
