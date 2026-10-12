package com.example.auth.support;

import com.example.auth.JwtUtil;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 测试中初始化 {@link JwtUtil} 静态状态的工具类。
 *
 * <p>{@code JwtUtil} 把密钥与有效期放在 static 字段里，由 {@code @PostConstruct} 注入。
 * 测试里不走 Spring 上下文，而是反射填入配置后<b>调用真实的 {@code init()}</b>，
 * 这样做有两个好处：</p>
 * <ul>
 *   <li>不必依赖 Spring 的占位符解析（{@code @ContextConfiguration} + {@code @TestPropertySource}
 *       并不会自动注册 {@code PropertySourcesPlaceholderConfigurer}）；</li>
 *   <li>避开上下文缓存——Spring 缓存复用上下文时 {@code @PostConstruct} 不会重跑，
 *       静态字段会残留上一个测试类设置的密钥。</li>
 * </ul>
 *
 * <p>因此每个用到 {@code JwtUtil} 的测试类都应在 {@code @BeforeEach} / {@code @BeforeAll}
 * 里调用本工具重新初始化一次（幂等，重复调用无副作用）。</p>
 */
public final class JwtTestSupport {

    /** 测试统一使用的密钥 */
    public static final String SECRET = "unit-test-secret-0123456789abcdef";

    private JwtTestSupport() {
    }

    /** 用默认密钥与 12 小时有效期初始化 */
    public static void init() {
        init(SECRET, 12);
    }

    /** 用指定密钥与有效期重新初始化 JwtUtil 的静态状态 */
    public static void init(String secret, long expireHours) {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret", secret);
        ReflectionTestUtils.setField(util, "expireHours", expireHours);
        util.init();
    }
}
