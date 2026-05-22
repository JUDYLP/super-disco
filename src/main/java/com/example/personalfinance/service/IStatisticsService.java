package com.example.personalfinance.service;

import com.example.personalfinance.vo.CategoryStatVO;
import com.example.personalfinance.vo.DashboardVO;
import java.util.List;

/**
 * 【修改】统计服务接口 —— 方法签名增加 userId 参数。
 *
 * userId 的含义：
 *   - 有值 → 只统计这个用户的数据
 *   - null → 统计所有用户的数据（仅 ADMIN 使用）
 */
public interface IStatisticsService {
    /** 【修改】增加 userId 参数 */
    DashboardVO getDashboard(Long userId);

    /** 【修改】增加 userId 参数 */
    List<CategoryStatVO> getExpenseByCategory(Long userId);
}
