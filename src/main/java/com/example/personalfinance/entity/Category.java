package com.example.personalfinance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

/**
 * 【修改】分类实体 —— 增加 userId 字段，支持用户私有分类。
 * userId = null → 全局分类（所有用户可见）
 * userId = 某值  → 用户私有分类（只有该用户可见）
 */
@TableName("category")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 【新增字段】所属用户 ID，null 表示全局分类 */
    @TableField("user_id")
    private Long userId;

    private String name;

    private String type;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("create_time")
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
