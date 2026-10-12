package com.example.auth;

/**
 * 当前请求的登录用户上下文（ThreadLocal）。
 *
 * <p>由 {@link AuthInterceptor} 在 preHandle 中写入、afterCompletion 中清理。
 * 由于 Tomcat 会复用线程，务必保证异常路径下也能清理，避免身份串号。</p>
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 当前登录用户 ID，未登录返回 null */
    public static Integer currentId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getId();
    }

    /** 当前登录用户角色，未登录返回 null */
    public static String currentRole() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getRole();
    }

    /** 当前登录用户姓名，未登录返回 null */
    public static String currentName() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getName();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
