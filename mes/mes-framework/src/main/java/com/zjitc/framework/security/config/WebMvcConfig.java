package com.zjitc.framework.security.config; // 建议放在 config 包下

import com.zjitc.framework.security.jwt.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 1. 指定拦截的路径：通常拦截所有需要鉴权的接口
                .addPathPatterns("/api/**")
                // 2. 指定放行的路径：登录、注册、静态资源等不需要 Token 的接口
                .excludePathPatterns(
                        "/api/**/login",      // 登录接口
                        "/api/**/register"  // 注册接口
                                              // 如果有 swagger/knife4j 文档
                                              // 静态资源
                                              // 图标
                );
    }
}
