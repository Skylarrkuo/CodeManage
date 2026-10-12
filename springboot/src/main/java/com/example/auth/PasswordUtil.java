package com.example.auth;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码加密与校验工具（BCrypt）。
 */
public class PasswordUtil {

    /** BCrypt 哈希的统一前缀，用于判断库中存的究竟是不是哈希 */
    private static final String BCRYPT_PREFIX = "$2";

    private PasswordUtil() {
    }

    /** 生成 BCrypt 哈希 */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 校验明文密码是否匹配。
     *
     * <p>兼容历史数据：若库中尚未迁移为 BCrypt 哈希（不以 {@code $2} 开头），
     * 则退化为明文比对，避免种子数据未更新时全员无法登录。待数据全部迁移后可移除此分支。</p>
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (StrUtil.isBlank(rawPassword) || StrUtil.isBlank(storedPassword)) {
            return false;
        }
        if (storedPassword.startsWith(BCRYPT_PREFIX)) {
            try {
                return BCrypt.checkpw(rawPassword, storedPassword);
            } catch (Exception e) {
                // 哈希串格式非法时视为不匹配
                return false;
            }
        }
        return rawPassword.equals(storedPassword);
    }
}
