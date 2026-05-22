package com.example.personalfinance.common;

/**
 * 【新增】用户上下文 —— 用 ThreadLocal 存储当前请求的用户信息。
 *
 * 为什么需要这个类？
 *   拦截器从 JWT 中解析出 userId 和 role 后，需要一个地方"暂存"，
 *   让 Controller / Service 层能随时取到。ThreadLocal 保证每个线程
 *   独立存储，互不干扰（即使同时有 100 个请求也不会串数据）。
 *
 * 使用方式：
 *   拦截器里：UserContext.set(userId, role);
 *   Controller里：Long userId = UserContext.getUserId();
 *   拦截器afterCompletion里：UserContext.clear(); // 必须清理！
 */
public class UserContext {

    // ========== 第1步：定义两个 ThreadLocal 变量 ==========
    // ThreadLocal<Long> 表示这个变量只存 Long 类型
    // 每个线程都有自己独立的一份，线程A设的值线程B看不到
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    // ========== 第2步：提供设置值的方法 ==========
    // 拦截器解析完 JWT 后调用这个方法，把用户信息"放进去"
    public static void set(Long userId, String role) {
        USER_ID.set(userId);  // 把 userId 存入当前线程的 ThreadLocal
        ROLE.set(role);       // 把 role 存入当前线程的 ThreadLocal
    }

    // ========== 第3步：提供获取值的方法 ==========
    // Controller / Service 里随时可以调用，拿到当前登录用户的 ID
    public static Long getUserId() {
        return USER_ID.get(); // 从当前线程的 ThreadLocal 中取 userId
    }

    // Controller / Service 里随时可以调用，拿到当前登录用户的角色
    public static String getRole() {
        return ROLE.get();    // 从当前线程的 ThreadLocal 中取 role
    }

    // ========== 第4步：提供清理方法 ==========
    // 【非常重要】请求处理完毕后必须清理，否则线程复用时会残留上一个请求的数据
    // 拦截器的 afterCompletion 方法里会调用这个
    public static void clear() {
        USER_ID.remove(); // 清除当前线程的 userId
        ROLE.remove();    // 清除当前线程的 role
    }
}
