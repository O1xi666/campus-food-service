package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    // 🔥【只加这2行！固定秘钥+过期时间，永远不null！】
    private static final String SECRET_KEY = "my_secret_key_123456"; // 固定秘钥
    private static final long TTL_MILLIS = 7200000L; // 2小时过期

    /**
     * 生成jwt（🔥【改这里：去掉参数，用内部常量】）
     */
    public static String createJWT(Map<String, Object> claims) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long expMillis = System.currentTimeMillis() + TTL_MILLIS;
        Date exp = new Date(expMillis);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                // 🔥【用内部常量，永远不null！】
                .signWith(signatureAlgorithm, SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * Token解密（🔥【改这里：去掉秘钥参数，用内部常量】）
     */
    public static Claims parseJWT(String token) {
        Claims claims = Jwts.parser()
                // 🔥【用内部常量，不用外部传！】
                .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token).getBody();
        return claims;
    }
}