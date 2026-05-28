package com.zjitc.framework.security.config;

import com.zjitc.framework.security.interceptor.PermissionInterceptor;
import com.zjitc.framework.security.jwt.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截器执行顺序：先添加的先执行
        // 1. JWT拦截器（先验证token是否有效）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/**/login",      // 登录接口
                        "/api/**/register"    // 注册接口
                )
                .order(1);  // 优先级1，先执行

        // 2. 权限拦截器（验证token后，检查角色/权限）
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/**/login",
                        "/api/**/register"
                )
                .order(2);  // 优先级2，后执行
    }
}