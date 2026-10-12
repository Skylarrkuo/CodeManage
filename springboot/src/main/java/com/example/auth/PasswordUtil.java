package com.example.auth;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

/**
 * 密码加密与校验工具（BCrypt）。
 */
public class PasswordUtil {

    private static final Log log = LogFactory.get();

    /** BCrypt 哈希的统一前缀 */
    private static final String BCRYPT_PREFIX = "$2";

    private PasswordUtil() {
    }

    /** 生成 BCrypt 哈希 */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 校验明文密码是否与库中的 BCrypt 哈希匹配。
     *
     * <p><b>只接受 BCrypt 哈希。</b>若库中存的仍是明文（例如导入了早期的种子数据），
     * 这里会返回 false 并打印告警日志——此时所有账号都登不进去，需要重新导入
     * {@code bsoj.sql}（其中的种子密码已是 BCrypt 哈希）。</p>
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(storedPassword)) {
            return false;
        }
        if (!storedPassword.startsWith(BCRYPT_PREFIX)) {
            // 曾经的明文兼容分支已移除，这里显式告警，避免把「库没迁移」误判成「密码记错了」
            log.warn("数据库中的密码不是 BCrypt 哈希格式，登录将一律失败。"
                    + "请重新导入 bsoj.sql。");
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, storedPassword);
        } catch (Exception e) {
            // 哈希串格式非法时视为不匹配
            return false;
        }
    }
}
