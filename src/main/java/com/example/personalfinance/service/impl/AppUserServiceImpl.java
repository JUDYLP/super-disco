package com.example.personalfinance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.personalfinance.dto.LoginDTO;
import com.example.personalfinance.dto.RegisterDTO;
import com.example.personalfinance.entity.AppUser;
import com.example.personalfinance.mapper.AppUserMapper;
import com.example.personalfinance.service.IAppUserService;
import com.example.personalfinance.util.JwtUtil;
import com.example.personalfinance.vo.LoginVO;
import java.time.LocalDateTime;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements IAppUserService {

    private static final Logger log = LoggerFactory.getLogger(AppUserServiceImpl.class);

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    public AppUserServiceImpl(JwtUtil jwtUtil,
                              BCryptPasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "USER", "VIEWER");

    @Override
    public LoginVO register(RegisterDTO request) {
        validatePassword(request.getPassword());

        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getEmail, request.getEmail());
        if (this.count(wrapper) > 0) {
            throw new IllegalArgumentException("email is already registered");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setRole("USER");

        user.setCreateTime(LocalDateTime.now());

        boolean saved = this.save(user);
        if (!saved || user.getId() == null) {
            throw new IllegalStateException("failed to register user");
        }

        log.info("User registered: id={}, role={}", user.getId(), user.getRole());

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new LoginVO(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), token);
    }

    @Override
    public LoginVO login(LoginDTO request) {
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppUser::getEmail, request.getEmail());
        AppUser user = this.getOne(wrapper, false);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("email or password is incorrect");
        }

        log.info("User logged in: id={}, role={}", user.getId(), user.getRole());

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new LoginVO(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), token);
    }

    @Override
    public Page<AppUser> listUsers(String role, int page, int size) {
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(role != null && !role.isBlank(), AppUser::getRole, role);
        wrapper.orderByDesc(AppUser::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public void updateUserRole(Long userId, String newRole) {
        if (!VALID_ROLES.contains(newRole)) {
            throw new IllegalArgumentException("role must be ADMIN, USER, or VIEWER");
        }

        AppUser user = this.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }

        String oldRole = user.getRole();
        user.setRole(newRole);
        this.updateById(user);

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(
                        "token_invalid:" + userId, "1",
                        java.time.Duration.ofMillis(jwtExpirationMs));
            } catch (Exception e) {
                log.warn("Redis unavailable, token invalidation skipped: {}", e.getMessage());
            }
        }

        log.info("User role updated: id={}, oldRole={}, newRole={}", userId, oldRole, newRole);
    }

    private void validatePassword(String password) {
        if (password.length() < 6) {
            throw new IllegalArgumentException("password must be at least 6 characters");
        }
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasDigit) {
            throw new IllegalArgumentException("password must contain at least one number");
        }
    }
}
