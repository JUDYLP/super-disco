package com.example.personalfinance.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 【修改】JWT 工具类 —— 增加 role 的生成和解析。
 *
 * 原有功能不变，新增：
 *   1. generateToken 方法增加 role 参数
 *   2. 新增 getRole 方法从 Token 中提取角色
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * 【修改】生成 JWT Token —— 增加 role 参数。
     *
     * 原方法签名：generateToken(Long userId, String email)
     * 新方法签名：generateToken(Long userId, String email, String role)
     *
     * 在 Token 中多存一个 claim（声明）叫 "role"，
     * 这样拦截器解析 Token 时就能直接拿到用户角色，不需要查数据库。
     */
    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))  // subject 存用户 ID
                .claim("email", email)             // 自定义声明：邮箱
                .claim("role", role)               // 【新增】自定义声明：角色
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 从 Token 中提取用户 ID。
     * （未修改，保持原样）
     */
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 【新增】从 Token 中提取用户角色。
     *
     * 拦截器中调用：String role = jwtUtil.getRole(token);
     * 返回值示例："ADMIN"、"USER"、"VIEWER"
     */
    public String getRole(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);  // 取出 "role" 这个 claim，转为 String
    }

    /**
     * 从 Token 中提取签发时间（issued at），用于判断 token 是否在角色变更之前签发。
     */
    public Date getIssuedAt(String token) {
        Claims claims = parseClaims(token);
        return claims.getIssuedAt();
    }

    /**
     * 验证 Token 是否有效。
     * （未修改，保持原样）
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
