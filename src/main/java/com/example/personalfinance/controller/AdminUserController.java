package com.example.personalfinance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.personalfinance.annotation.RequireRole;
import com.example.personalfinance.common.ApiResponse;
import com.example.personalfinance.dto.UserRoleUpdateDTO;
import com.example.personalfinance.entity.AppUser;
import com.example.personalfinance.service.IAppUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【新增】管理员用户管理控制器。
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final IAppUserService appUserService;

    public AdminUserController(IAppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    @RequireRole("ADMIN")
    public ApiResponse<Page<AppUser>> list(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);

        Page<AppUser> result = appUserService.listUsers(role, page, size);
        return ApiResponse.success("user list loaded successfully", result);
    }

    @PutMapping("/role")
    @RequireRole("ADMIN")
    public ApiResponse<Void> updateRole(@Valid @RequestBody UserRoleUpdateDTO request) {
        appUserService.updateUserRole(request.getUserId(), request.getRole());
        return ApiResponse.success("user role updated successfully", null);
    }
}
