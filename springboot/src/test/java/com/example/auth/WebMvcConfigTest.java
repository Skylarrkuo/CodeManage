package com.example.auth;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 守着 {@link WebMvcConfig} 的拦截器接线。
 *
 * <p>之所以要测：{@code /error} 若被误删出排除列表，后端异常会被拦截器改写成 401，
 * 真实错误被掩盖，且症状极具迷惑性。</p>
 *
 * <p>断言全部走 {@link MappedInterceptor} 的公开 getter。唯一用到反射的是
 * {@code InterceptorRegistry.getInterceptors()}——它是 protected，Spring 未提供公开读回入口。</p>
 */
class WebMvcConfigTest {

    @Test
    @SuppressWarnings("unchecked")
    void registersAuthInterceptorForAllPathsAndExcludesErrorPath() {
        InterceptorRegistry registry = new InterceptorRegistry();
        new WebMvcConfig().addInterceptors(registry);

        List<Object> registrations =
                (List<Object>) ReflectionTestUtils.invokeMethod(registry, "getInterceptors");
        assertThat(registrations).as("应恰好注册一个拦截器").hasSize(1);

        MappedInterceptor mapped = (MappedInterceptor) registrations.get(0);
        assertThat(mapped.getInterceptor()).isInstanceOf(AuthInterceptor.class);
        assertThat(mapped.getIncludePathPatterns()).containsExactly("/**");
        assertThat(mapped.getExcludePathPatterns())
                .as("必须排除 /error，否则后端异常会被改写成 401")
                .containsExactly("/error");
    }
}
