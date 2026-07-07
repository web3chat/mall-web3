package com.fzm.mall.entity.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "local.logistics")
public class LogisticsProperties {
    private Boolean enable;

    private String appCode;
}
