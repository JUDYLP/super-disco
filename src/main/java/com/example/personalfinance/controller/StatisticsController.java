package com.example.personalfinance.controller;

import com.example.personalfinance.annotation.RequireRole;
import com.example.personalfinance.common.ApiResponse;
import com.example.personalfinance.common.UserContext;
import com.example.personalfinance.service.IStatisticsService;
import com.example.personalfinance.vo.CategoryStatVO;
import com.example.personalfinance.vo.DashboardVO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【修改】统计控制器 —— 统计数据按用户隔离。
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final IStatisticsService statisticsService;

    public StatisticsController(IStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequireRole({"ADMIN", "USER"})
    @GetMapping("/dashboard")
    public ApiResponse<DashboardVO> dashboard(@RequestParam(required = false) Long userId) {
        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        Long targetUserId;

        if ("ADMIN".equals(currentRole) && userId != null) {
            targetUserId = userId;
        } else if ("ADMIN".equals(currentRole)) {
            targetUserId = null;
        } else {
            targetUserId = currentUserId;
        }

        DashboardVO result = statisticsService.getDashboard(targetUserId);
        return ApiResponse.success("dashboard loaded successfully", result);
    }

    @RequireRole({"ADMIN", "USER"})
    @GetMapping("/expense-by-category")
    public ApiResponse<List<CategoryStatVO>> expenseByCategory(@RequestParam(required = false) Long userId) {
        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        Long targetUserId;

        if ("ADMIN".equals(currentRole) && userId != null) {
            targetUserId = userId;
        } else if ("ADMIN".equals(currentRole)) {
            targetUserId = null;
        } else {
            targetUserId = currentUserId;
        }

        List<CategoryStatVO> result = statisticsService.getExpenseByCategory(targetUserId);
        return ApiResponse.success("category statistics loaded successfully", result);
    }
}
