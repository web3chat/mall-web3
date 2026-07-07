package com.fzm.mall.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域过滤器
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    /**
     * 跨域
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern(CorsConfiguration.ALL);
        config.addAllowedHeader(CorsConfiguration.ALL);
        config.addAllowedMethod(CorsConfiguration.ALL);
        config.setMaxAge(7 * 24 * 60 * 60L);

        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            // 生产环境：仅允许配置的白名单域名
            for (String origin : allowedOrigins.split(",")) {
                config.addAllowedOrigin(origin.trim());
            }
        } else {
            // 开发/测试环境：允许所有来源
            config.addAllowedOriginPattern(CorsConfiguration.ALL);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
