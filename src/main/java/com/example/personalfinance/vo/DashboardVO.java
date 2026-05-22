package com.example.personalfinance.vo;

import com.example.personalfinance.entity.Bill;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DashboardVO {
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private BigDecimal monthBalance;
    private List<Bill> recentBills;

    public BigDecimal getMonthIncome() { return monthIncome; }
    public void setMonthIncome(BigDecimal monthIncome) { this.monthIncome = monthIncome; }
    public BigDecimal getMonthExpense() { return monthExpense; }
    public void setMonthExpense(BigDecimal monthExpense) { this.monthExpense = monthExpense; }
    public BigDecimal getMonthBalance() { return monthBalance; }
    public void setMonthBalance(BigDecimal monthBalance) { this.monthBalance = monthBalance; }
    public List<Bill> getRecentBills() { return recentBills; }
    public void setRecentBills(List<Bill> recentBills) { this.recentBills = recentBills; }
}
