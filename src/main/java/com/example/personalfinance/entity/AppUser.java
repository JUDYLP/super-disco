package com.example.personalfinance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

/**
 * 【修改】用户实体 —— 增加 role 字段，支持多角色。
 * 角色值：ADMIN（管理员）、USER（普通用户）、VIEWER（只读成员）
 */
@TableName("app_user")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AppUser {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String email;

    @JsonIgnore
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 【新增字段】用户角色。
     */
    private String role;

    @TableField("create_time")
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
