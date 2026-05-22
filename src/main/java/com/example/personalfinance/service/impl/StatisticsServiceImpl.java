package com.example.personalfinance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.personalfinance.entity.Bill;
import com.example.personalfinance.entity.Category;
import com.example.personalfinance.service.IBillService;
import com.example.personalfinance.service.ICategoryService;
import com.example.personalfinance.service.IStatisticsService;
import com.example.personalfinance.vo.CategoryStatVO;
import com.example.personalfinance.vo.DashboardVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 【修改】统计服务实现 —— 所有查询增加 userId 过滤。
 *
 * 改动说明：
 *   1. ★ getDashboard(userId) → 查询时加上 WHERE user_id = userId（userId 不为 null 时）
 *   2. ★ getExpenseByCategory(userId) → 同上
 *   3. 缓存 key 也要加上 userId，避免不同用户看到相同的缓存数据
 */
@Service
public class StatisticsServiceImpl implements IStatisticsService {

    private final IBillService billService;
    private final ICategoryService categoryService;

    public StatisticsServiceImpl(IBillService billService, ICategoryService categoryService) {
        this.billService = billService;
        this.categoryService = categoryService;
    }

    @Override
    // ★ 缓存 key 改为包含 userId，不同用户的数据分开缓存
    // 当 userId 为 null（ADMIN 查全部）时，key 为 "all"；否则为具体用户ID
    @Cacheable(value = "dashboard", key = "#userId != null ? #userId : 'all'")
    public DashboardVO getDashboard(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LambdaQueryWrapper<Bill> monthWrapper = new LambdaQueryWrapper<>();

        // ========== ★ 数据隔离：按用户过滤 ==========
        if (userId != null) {
            // userId 有值 → 只查这个用户的账单
            monthWrapper.eq(Bill::getUserId, userId);
        }
        // userId 为 null → 不加 user_id 过滤，查所有用户（ADMIN 查全部）

        monthWrapper.ge(Bill::getConsumeDate, startOfMonth);
        monthWrapper.le(Bill::getConsumeDate, endOfMonth);
        List<Bill> monthBills = billService.list(monthWrapper);

        BigDecimal income = sumByType(monthBills, "income");
        BigDecimal expense = sumByType(monthBills, "expense");

        LambdaQueryWrapper<Bill> recentWrapper = new LambdaQueryWrapper<>();

        // ★ 同样按用户过滤最近账单
        if (userId != null) {
            recentWrapper.eq(Bill::getUserId, userId);
        }

        recentWrapper.orderByDesc(Bill::getConsumeDate).orderByDesc(Bill::getId);
        List<Bill> recentBills = billService.page(new Page<>(1, 5), recentWrapper).getRecords();

        DashboardVO dashboard = new DashboardVO();
        dashboard.setMonthIncome(income);
        dashboard.setMonthExpense(expense);
        dashboard.setMonthBalance(income.subtract(expense));
        dashboard.setRecentBills(recentBills);

        return dashboard;
    }

    @Override
    @Cacheable(value = "expenseByCategory", key = "#userId != null ? #userId : 'all'")
    public List<CategoryStatVO> getExpenseByCategory(Long userId) {
        Map<Long, Category> categoryMap = categoryService.list().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity(), (a, b) -> a));

        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();

        // ========== ★ 数据隔离：按用户过滤 ==========
        if (userId != null) {
            wrapper.eq(Bill::getUserId, userId);
        }

        wrapper.eq(Bill::getType, "expense");
        wrapper.select(Bill::getCategoryId, Bill::getAmount);
        List<Bill> expenseBills = billService.list(wrapper);

        Map<Long, BigDecimal> amountMap = new LinkedHashMap<>();
        for (Bill bill : expenseBills) {
            amountMap.merge(bill.getCategoryId(), bill.getAmount(), BigDecimal::add);
        }

        List<CategoryStatVO> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : amountMap.entrySet()) {
            Category category = categoryMap.get(entry.getKey());
            String categoryName = category == null ? "Unknown" : category.getName();
            result.add(new CategoryStatVO(entry.getKey(), categoryName, entry.getValue()));
        }

        return result;
    }

    private BigDecimal sumByType(List<Bill> bills, String type) {
        return bills.stream()
                .filter(bill -> type.equals(bill.getType()))
                .filter(bill -> bill.getAmount() != null)
                .map(Bill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
