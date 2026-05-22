package com.example.personalfinance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.personalfinance.dto.LoginDTO;
import com.example.personalfinance.dto.RegisterDTO;
import com.example.personalfinance.entity.AppUser;
import com.example.personalfinance.vo.LoginVO;

/**
 * 【修改】用户服务接口 —— 新增用户管理方法供 ADMIN 使用。
 */
public interface IAppUserService extends IService<AppUser> {
    LoginVO register(RegisterDTO request);

    LoginVO login(LoginDTO request);

    /**
     * 【新增】获取用户列表（分页），供管理员使用。
     * 可按角色筛选。
     */
    Page<AppUser> listUsers(String role, int page, int size);

    /**
     * 【新增】修改用户角色，供管理员使用。
     */
    void updateUserRole(Long userId, String newRole);
}
