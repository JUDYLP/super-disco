package com.example.personalfinance.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;

/**
 * 【修改】查询参数 —— 增加 userId 字段供管理员按用户筛选。
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BillQueryDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long categoryId;
    private String type;

    /** 【新增】管理员按用户筛选时使用 */
    private Long userId;

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
