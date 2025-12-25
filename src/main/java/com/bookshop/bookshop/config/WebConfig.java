package com.bookshop.bookshop.config;

import com.bookshop.bookshop.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/**") // 拦截所有
                .excludePathPatterns(
                        // 1. API 接口白名单
                        "/user/login",
                        "/user/register",
                        "/book/**",
                        "/user/sendMsg",

                        // 2. 静态资源白名单
                        "/**/*.html",
                        "/js/**",
                        "/css/**",
                        "/images/**",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/favicon.ico"
                );
    }
}