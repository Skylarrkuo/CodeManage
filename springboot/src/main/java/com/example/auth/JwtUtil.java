package com.example.auth;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 签发与解析工具（基于 Hutool，无需额外依赖）。
 *
 * <p>密钥与有效期通过 {@code application.yml} 的 {@code jwt.*} 配置注入。
 * 由于 {@code @Value} 无法直接作用于静态字段，这里用 {@link PostConstruct} 把配置灌入静态字段，
 * 使 {@link AuthInterceptor} 等外部调用方能以静态方法使用。</p>
 */
@Component
public class JwtUtil {

    /** 签名密钥的最小长度（HS256 建议至少 256 bit） */
    private static final int MIN_SECRET_BYTES = 32;

    /** 签名密钥（由配置注入，切勿退化为纯静态工具类，否则此处为 null） */
    private static byte[] KEY;

    /** token 有效期（秒） */
    private static long EXPIRE_SECONDS;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours:12}")
    private long expireHours;

    @PostConstruct
    public void init() {
        // 快速失败：密钥缺失或过短时直接让应用启动失败，
        // 避免用空密钥签发 token（那样任何人都能伪造任意身份的 token）。
        if (StrUtil.isBlank(secret) || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret 未配置或长度不足 " + MIN_SECRET_BYTES + " 字节。"
                            + "请在 application-local.yml 中设置 jwt.secret，"
                            + "或通过环境变量 JWT_SECRET 注入（可用 `openssl rand -base64 48` 生成）。");
        }
        KEY = secret.getBytes(StandardCharsets.UTF_8);
        EXPIRE_SECONDS = expireHours * 3600L;
    }

    /**
     * 签发 token。
     *
     * <p>注意：role 必须传数据库中的真实角色，不能用登录请求体里的值。</p>
     */
    public static String createToken(LoginUser user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", user.getId());
        payload.put("username", user.getUsername());
        payload.put("name", user.getName());
        payload.put("role", user.getRole());
        payload.put("exp", System.currentTimeMillis() / 1000 + EXPIRE_SECONDS);
        return JWTUtil.createToken(payload, KEY);
    }

    /**
     * 解析并校验 token。
     *
     * <p>签名不合法、已过期、字段缺失或格式错误一律返回 {@code null}（fail-closed）。</p>
     */
    public static LoginUser parse(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            JWT jwt = JWTUtil.parseToken(token);
            jwt.setKey(KEY);
            // 先校验签名
            if (!jwt.verify()) {
                return null;
            }
            // 再显式校验过期时间，不依赖 Hutool 各小版本的过期判断差异
            Object exp = jwt.getPayload("exp");
            if (exp == null) {
                return null;
            }
            if (System.currentTimeMillis() / 1000 > Convert.toLong(exp)) {
                return null;
            }

            LoginUser user = new LoginUser();
            user.setId(Convert.toInt(jwt.getPayload("id")));
            user.setUsername(Convert.toStr(jwt.getPayload("username")));
            user.setName(Convert.toStr(jwt.getPayload("name")));
            user.setRole(Convert.toStr(jwt.getPayload("role")));
            return user;
        } catch (Exception e) {
            return null;
        }
    }
}
