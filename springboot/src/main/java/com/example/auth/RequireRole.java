package com.example.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明访问接口所需的角色。
 *
 * <p>可打在方法或类上；<b>方法级优先于类级</b>。未标注的接口表示「任意已登录角色均可访问」。
 * 角色取值：ADMIN / TEACHER / STUDENT。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value();
}
