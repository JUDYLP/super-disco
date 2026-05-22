package com.example.personalfinance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.personalfinance.annotation.RequireRole;
import com.example.personalfinance.common.ApiResponse;
import com.example.personalfinance.common.UserContext;
import com.example.personalfinance.dto.CategoryCreateDTO;
import com.example.personalfinance.dto.CategoryQueryDTO;
import com.example.personalfinance.entity.Category;
import com.example.personalfinance.service.ICategoryService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【修改】分类控制器 —— 增加用户隔离和权限控制。
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @RequireRole({"ADMIN", "USER"})
    public ApiResponse<Category> add(@Valid @RequestBody CategoryCreateDTO request) {
        if (!"income".equals(request.getType()) && !"expense".equals(request.getType())) {
            throw new IllegalArgumentException("type must be income or expense");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setSortOrder(100);
        category.setCreateTime(LocalDateTime.now());

        String currentRole = UserContext.getRole();
        if ("ADMIN".equals(currentRole)) {
            category.setUserId(null);
        } else {
            category.setUserId(UserContext.getUserId());
        }

        // 检查同名同类型分类是否已存在
        LambdaQueryWrapper<Category> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Category::getName, request.getName())
                .eq(Category::getType, request.getType());
        if (category.getUserId() == null) {
            dupWrapper.isNull(Category::getUserId);
        } else {
            dupWrapper.eq(Category::getUserId, category.getUserId());
        }
        if (categoryService.count(dupWrapper) > 0) {
            throw new IllegalArgumentException("category with this name and type already exists");
        }

        boolean saved = categoryService.save(category);
        if (!saved || category.getId() == null) {
            return ApiResponse.failure("failed to add category");
        }
        return ApiResponse.success("category added successfully", category);
    }

    @GetMapping
    public ApiResponse<Page<Category>> list(
            @ModelAttribute CategoryQueryDTO query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {

        size = Math.min(size, 100);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();

        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();

        if ("ADMIN".equals(currentRole)) {
            // 管理员：可以看到所有分类
        } else {
            wrapper.and(w -> w
                    .isNull(Category::getUserId)
                    .or()
                    .eq(Category::getUserId, currentUserId)
            );
        }

        wrapper.eq(StringUtils.hasText(query.getType()), Category::getType, query.getType());
        wrapper.orderByAsc(Category::getSortOrder).orderByAsc(Category::getId);

        Page<Category> result = categoryService.page(new Page<>(page, size), wrapper);
        return ApiResponse.success("category list loaded successfully", result);
    }
}
