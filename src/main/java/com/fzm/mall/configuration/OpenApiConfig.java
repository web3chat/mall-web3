package com.fzm.mall.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

/**
 * swagger3配置文件
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("API文档").version("1.0"));
    }

    @Bean
    public GroupedOpenApi frontApi() {
        return GroupedOpenApi.builder()
                .group("前台接口文档")
                .packagesToScan("com.fzm.mall.controller.web")
                .addOperationCustomizer((Operation operation, HandlerMethod handlerMethod) -> {
                    operation.addParametersItem(new Parameter().in("header").name("authorization").description("认证Token").schema(new StringSchema()));
                    operation.addParametersItem(new Parameter().in("header").name("lang").description("语言").schema(new StringSchema()));
                    return operation;
                })
                .build();
    }

    @Bean
    public GroupedOpenApi backApi() {

        return GroupedOpenApi.builder()
                .group("后台接口文档")
                .packagesToScan("com.fzm.mall.controller.manage")
                .addOperationCustomizer((Operation operation, HandlerMethod handlerMethod) -> {
                    operation.addParametersItem(new Parameter().in("header").name("authorization").description("认证Token").schema(new StringSchema()));
                    operation.addParametersItem(new Parameter().in("header").name("lang").description("语言").schema(new StringSchema()));
                    return operation;
                })
                .build();
    }

}
