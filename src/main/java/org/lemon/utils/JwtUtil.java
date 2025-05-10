package org.lemon.utils;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * description: add a description
 *
 * @author Lemon
 * @version 1.0.0
 * @date 2025/05/10 14:39:01
 */
public class JwtUtil {

    /**
     * 使用更安全的密钥生成方式（必须至少 256 bits）
     */
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    /**
     * 过期时间：24小时
     */
    private static final long EXPIRATION = 24 * 60 * 60 * 1000;

    public static String generateToken(String userInfo) {
        return Jwts.builder()
                .setSubject(userInfo)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                // 使用 Key + 指定算法
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    public static String extractUserInfo(String token) {
        return parseClaimsJws(token).getBody().getSubject();
    }

    public static boolean validateToken(String token) {
        try {
            parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 提取全部 Claims（可扩展用于权限校验）
     *
     * @param token
     * @return
     */
    public static Claims getAllClaimsFromToken(String token) {
        return parseClaimsJws(token).getBody();
    }

    /**
     * 解析逻辑
     */
    private static Jws<Claims> parseClaimsJws(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
    }
}
