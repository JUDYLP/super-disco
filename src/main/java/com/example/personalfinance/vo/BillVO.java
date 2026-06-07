package com.example.personalfinance.vo;

import com.example.personalfinance.entity.Bill;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单 VO —— 返回给前端的账单数据。
 *
 * 为什么不用 Bill Entity 直接返回？
 *   1. Entity 和数据库表强绑定，表结构变了 API 就变了
 *   2. VO 可以只暴露前端需要的字段，隐藏内部字段
 *   3. VO 可以加额外计算字段（如 categoryName）而不污染 Entity
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BillVO {
    private Long id;
    private Long userId;
    private String name;
    private BigDecimal amount;
    private String type;
    private Long categoryId;
    private LocalDate consumeDate;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public BillVO() {}

    /**
     * 从 Entity 构建 VO（静态工厂方法）。
     * Controller 层调用此方法做转换，Service 层仍然操作 Entity。
     */
    public static BillVO from(Bill bill) {
        if (bill == null) return null;
        BillVO vo = new BillVO();
        vo.setId(bill.getId());
        vo.setUserId(bill.getUserId());
        vo.setName(bill.getName());
        vo.setAmount(bill.getAmount());
        vo.setType(bill.getType());
        vo.setCategoryId(bill.getCategoryId());
        vo.setConsumeDate(bill.getConsumeDate());
        vo.setRemark(bill.getRemark());
        vo.setCreateTime(bill.getCreateTime());
        vo.setUpdateTime(bill.getUpdateTime());
        return vo;
    }

    // ========== Getters / Setters ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public LocalDate getConsumeDate() { return consumeDate; }
    public void setConsumeDate(LocalDate consumeDate) { this.consumeDate = consumeDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
