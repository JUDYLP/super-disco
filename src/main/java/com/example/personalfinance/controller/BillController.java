package com.example.personalfinance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.personalfinance.annotation.RequireRole;
import com.example.personalfinance.common.ApiResponse;
import com.example.personalfinance.common.UserContext;
import com.example.personalfinance.dto.BillCreateDTO;
import com.example.personalfinance.dto.BillQueryDTO;
import com.example.personalfinance.dto.BillUpdateDTO;
import com.example.personalfinance.entity.Bill;
import com.example.personalfinance.entity.Category;
import com.example.personalfinance.service.IBillService;
import com.example.personalfinance.service.ICategoryService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【修改】账单控制器 —— 所有接口增加数据隔离。
 */
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final IBillService billService;
    private final ICategoryService categoryService;

    public BillController(IBillService billService, ICategoryService categoryService) {
        this.billService = billService;
        this.categoryService = categoryService;
    }

    @PostMapping
    @RequireRole({"ADMIN", "USER"})
    public ApiResponse<Bill> add(@Valid @RequestBody BillCreateDTO request) {
        validateCategory(request.getCategoryId(), request.getType());

        Bill bill = new Bill();
        bill.setUserId(UserContext.getUserId());
        bill.setName(request.getName());
        bill.setAmount(request.getAmount());
        bill.setType(request.getType());
        bill.setCategoryId(request.getCategoryId());
        bill.setConsumeDate(request.getConsumeDate());
        bill.setRemark(request.getRemark());
        bill.setCreateTime(LocalDateTime.now());
        bill.setUpdateTime(LocalDateTime.now());

        boolean saved = billService.save(bill);
        if (!saved || bill.getId() == null) {
            return ApiResponse.failure("failed to add bill");
        }
        return ApiResponse.success("bill added successfully", bill);
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN", "USER"})
    public ApiResponse<Bill> update(@PathVariable Long id, @Valid @RequestBody BillUpdateDTO request) {
        if (!id.equals(request.getId())) {
            throw new IllegalArgumentException("path id and request id do not match");
        }

        Bill existingBill = billService.getById(id);
        if (existingBill == null) {
            throw new IllegalArgumentException("bill not found");
        }

        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        if (!"ADMIN".equals(currentRole) && !currentUserId.equals(existingBill.getUserId())) {
            throw new IllegalArgumentException("bill not found");
        }

        validateCategory(request.getCategoryId(), request.getType());

        existingBill.setName(request.getName());
        existingBill.setAmount(request.getAmount());
        existingBill.setType(request.getType());
        existingBill.setCategoryId(request.getCategoryId());
        existingBill.setConsumeDate(request.getConsumeDate());
        existingBill.setRemark(request.getRemark());
        existingBill.setUpdateTime(LocalDateTime.now());

        boolean updated = billService.updateById(existingBill);
        if (!updated) {
            return ApiResponse.failure("failed to update bill");
        }
        return ApiResponse.success("bill updated successfully", existingBill);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "USER"})
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        Bill existingBill = billService.getById(id);
        if (existingBill == null) {
            throw new IllegalArgumentException("bill not found or already deleted");
        }

        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        if (!"ADMIN".equals(currentRole) && !currentUserId.equals(existingBill.getUserId())) {
            throw new IllegalArgumentException("bill not found or already deleted");
        }

        boolean removed = billService.removeById(id);
        if (!removed) {
            throw new IllegalArgumentException("bill not found or already deleted");
        }
        return ApiResponse.success("bill deleted successfully", true);
    }

    @GetMapping
    public ApiResponse<Page<Bill>> list(
            @ModelAttribute BillQueryDTO query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);

        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();

        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();

        if ("ADMIN".equals(currentRole)) {
            if (query.getUserId() != null) {
                wrapper.eq(Bill::getUserId, query.getUserId());
            }
        } else {
            wrapper.eq(Bill::getUserId, currentUserId);
        }

        wrapper.ge(query.getStartDate() != null, Bill::getConsumeDate, query.getStartDate());
        wrapper.le(query.getEndDate() != null, Bill::getConsumeDate, query.getEndDate());
        wrapper.eq(query.getCategoryId() != null, Bill::getCategoryId, query.getCategoryId());
        wrapper.eq(StringUtils.hasText(query.getType()), Bill::getType, query.getType());
        wrapper.orderByDesc(Bill::getConsumeDate).orderByDesc(Bill::getId);

        Page<Bill> result = billService.page(new Page<>(page, size), wrapper);
        return ApiResponse.success("bill list loaded successfully", result);
    }

    private void validateCategory(Long categoryId, String type) {
        if (!"income".equals(type) && !"expense".equals(type)) {
            throw new IllegalArgumentException("type must be income or expense");
        }
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("category not found");
        }
        if (!type.equals(category.getType())) {
            throw new IllegalArgumentException("category type does not match bill type");
        }
        Long currentUserId = UserContext.getUserId();
        String currentRole = UserContext.getRole();
        if (!"ADMIN".equals(currentRole)
                && category.getUserId() != null
                && !currentUserId.equals(category.getUserId())) {
            throw new IllegalArgumentException("category not found");
        }
    }
}
