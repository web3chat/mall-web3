package com.fzm.mall.entity.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "local.chain.siwe")
public class SiweProperties {
    private String domain;
}
