package org.lemon.utils;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.lemon.entity.resp.UserTokenVO;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    /**
     * 生成 JWT Token
     *
     * @param userInfo
     * @return
     */
    public static UserTokenVO generateToken(String userInfo) {
        UserTokenVO result = new UserTokenVO();
        LocalDateTime expireTime = LocalDateTimeUtil.offset(LocalDateTime.now(), EXPIRATION, ChronoUnit.MILLIS);
        String accessToken = Jwts.builder()
                .setSubject(userInfo)
                .setExpiration(DateUtil.date(expireTime))
                // 使用 Key + 指定算法
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
        result.setAccessToken(accessToken);
        result.setExpireTime(expireTime);
        return result;
    }

    /**
     * 获取refreshToken
     *
     * @return
     */
    public static String generateRefreshToken(String userInfo, String deviceId, String tokenKey, Date expireDay) {
        return Jwts.builder()
                .setSubject(userInfo)
                .setId(userInfo + "-" + deviceId)
                .setExpiration(expireDay)
                .signWith(Keys.hmacShaKeyFor(tokenKey.getBytes()), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 解析用户信息
     *
     * @param token
     * @return
     */
    public static String extractUserInfo(String token) {
        try {
            return parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            return "";
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
