package com.blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 AccessToken（短期，默认 24h）。
     */
    public String generateAccessToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成 RefreshToken（长期，默认 7d）。
     * 仅用于刷新 AccessToken，不携带业务信息。
     */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 保留旧方法兼容。
     */
    public String generateToken(Long userId, String username) {
        return generateAccessToken(userId, username);
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");
    }

    /**
     * 判断 token 是否为 RefreshToken。
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验 RefreshToken 合法性并返回用户 ID。
     */
    public Long validateRefreshToken(String token) {
        Claims claims = parseToken(token);
        if (!"refresh".equals(claims.get("type"))) {
            throw new RuntimeException("非法的 RefreshToken 类型");
        }
        if (claims.getExpiration().before(new Date())) {
            throw new RuntimeException("RefreshToken 已过期");
        }
        return Long.parseLong(claims.getSubject());
    }
}
