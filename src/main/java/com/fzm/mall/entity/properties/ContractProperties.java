package com.fzm.mall.entity.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "local.chain.contract")
public class ContractProperties {
    // 链Url
    private String chainUrl;
    // 链ID
    private Long chainId;
    // 小数点位数
    private Integer decimals;
    // 钱包密码
    private String walletPassword;
    // 助记词
    private String mnemonic;
    private String deployTempPrivateKey;
    private String deployAdminAddress;
}
