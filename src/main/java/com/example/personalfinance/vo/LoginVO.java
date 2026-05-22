package com.example.personalfinance.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 【修改】登录/注册返回的用户信息 —— 增加 role 字段。
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LoginVO {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String token;

    public LoginVO() {}

    public LoginVO(Long userId, String username, String email, String role, String token) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
