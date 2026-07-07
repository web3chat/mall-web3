package com.fzm.mall.configuration.interceptor;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebMvcInterceptorConfig implements WebMvcConfigurer {
    private final Error404Interceptor error404Interceptor;
    private final OptionsInterceptor optionsInterceptor;
    private final AuthenticationInterceptor authenticationInterceptor;
    private final PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(optionsInterceptor).addPathPatterns("/**");
        registry.addInterceptor(error404Interceptor).addPathPatterns("/error");
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/v/**", "/m/**");
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/m/**");
    }

}
