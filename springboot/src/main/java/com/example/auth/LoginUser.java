package com.example.auth;

/**
 * 当前登录用户的轻量载体（由 JWT 解析而来）。
 *
 * <p>只承载鉴权所需的最小字段，不包含密码等敏感信息。</p>
 */
public class LoginUser {

    private Integer id;
    /** 账号 */
    private String username;
    /** 姓名 */
    private String name;
    /** 角色：ADMIN / TEACHER / STUDENT */
    private String role;

    public LoginUser() {
    }

    public LoginUser(Integer id, String username, String name, String role) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
