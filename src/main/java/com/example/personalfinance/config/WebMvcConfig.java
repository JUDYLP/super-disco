package com.example.personalfinance.config;

import com.example.personalfinance.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 【新增】Web MVC 配置 —— 注册拦截器，告诉 Spring 哪些路径需要认证。
 *
 * 这个配置类的作用：
 *   把我们写的 AuthInterceptor 注册到 Spring 的拦截器链中，
 *   并指定哪些 URL 需要被拦截（需要登录），哪些不需要（公开接口）。
 */
@Configuration  // 声明这是一个配置类，Spring 启动时会自动执行
public class WebMvcConfig implements WebMvcConfigurer {

    // 注入我们写的认证拦截器
    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * 注册拦截器并配置拦截规则。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)   // 添加我们的拦截器
                // ========== 需要拦截的路径（需要登录才能访问）==========
                .addPathPatterns("/api/**")         // 拦截所有 /api/ 开头的请求

                // ========== 放行的路径（不需要登录也能访问）==========
                .excludePathPatterns(
                        "/api/auth/login",          // 登录接口 —— 没登录的人当然要能访问
                        "/api/auth/register"         // 注册接口 —— 没注册的人当然要能访问
                );
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
