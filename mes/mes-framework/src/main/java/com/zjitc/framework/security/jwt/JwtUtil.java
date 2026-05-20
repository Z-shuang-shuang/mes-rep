package com.zjitc.framework.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtil {

    private long expiration = 24 * 60 * 60 * 1000;
    private String secret = "11111111111111111111111111111111";

    /**
     * 生成token（带tokenId）
     */
    public String generateToken(String userid, String username){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // 生成唯一的tokenId
        String tokenId = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("tokenId", tokenId);  // 添加tokenId到payload

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .setSubject(userid)
                .addClaims(claims)
                .setId(tokenId)  // 也可以设置JWT ID
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token){
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserId(String token){
        return parseToken(token).getSubject();
    }

    public String getUsername(String token){
        return parseToken(token).get("username", String.class);
    }

    /**
     * 从token中获取tokenId
     */
    public String getTokenIdFromToken(String token){
        try {
            Claims claims = parseToken(token);
            System.out.println("Claims内容: " + claims);

            String tokenId = claims.get("tokenId", String.class);
            System.out.println("从claims获取tokenId: " + tokenId);

            if (tokenId == null || tokenId.isEmpty()) {
                tokenId = claims.getId();
                System.out.println("从claims.getId获取tokenId: " + tokenId);
            }
            return tokenId;
        } catch (Exception e) {
            System.out.println("解析tokenId失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public String getData(String token, String name){
        return parseToken(token).get(name, String.class);
    }

    public boolean isExp(String token){
        return parseToken(token).getExpiration().before(new Date());
    }

    public long getRemainingExpiration(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long remaining = expiration.getTime() - now.getTime();
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }

    public Date getExpirationDate(String token) {
        return parseToken(token).getExpiration();
    }
}