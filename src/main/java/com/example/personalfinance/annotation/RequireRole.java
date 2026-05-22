package com.example.personalfinance.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 【新增】自定义角色权限注解。
 *
 * 用法：标注在 Controller 的方法上，声明"访问这个接口需要什么角色"。
 *
 * 示例：
 *   @RequireRole("ADMIN")           → 只有管理员能访问
 *   @RequireRole({"ADMIN", "USER"})  → 管理员和普通用户都能访问
 *
 * 拦截器（AuthInterceptor）会读取这个注解，对比当前用户的角色是否满足要求。
 * 如果不满足，拦截器直接返回 403，不会执行到 Controller 方法。
 *
 * 如果方法上没有加这个注解，表示"只要登录就行，不限制角色"。
 */
@Target(ElementType.METHOD)           // 这个注解只能加在方法上（不能加在类上）
@Retention(RetentionPolicy.RUNTIME)   // 运行时保留（拦截器需要在运行时读取它）
public @interface RequireRole {
    /**
     * 允许访问的角色列表。
     * 例如 @RequireRole({"ADMIN", "USER"}) 表示 ADMIN 和 USER 都可以访问。
     */
    String[] value();
}
