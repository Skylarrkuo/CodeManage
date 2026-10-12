package com.example.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册认证拦截器。
 *
 * <p>默认拦截全部请求（fail-closed）。免登录接口由 {@link Public} 注解在 Controller 方法上声明，
 * 这里只排除框架层的 {@code /error} —— 若不排除，Spring 的错误转发会被拦截并改写成 401，
 * 从而掩盖真实的异常信息。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/error");
    }
}
