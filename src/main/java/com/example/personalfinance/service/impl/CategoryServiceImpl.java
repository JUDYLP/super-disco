package com.example.personalfinance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.personalfinance.entity.Category;
import com.example.personalfinance.mapper.CategoryMapper;
import com.example.personalfinance.service.ICategoryService;
import org.springframework.stereotype.Service;

/**
 * Default category service implementation.
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
}
