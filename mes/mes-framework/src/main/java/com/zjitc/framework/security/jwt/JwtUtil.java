package com.zjitc.framework.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

/**
 * JWT工具类 - 负责token的生成、解析和验证
 * 作用：提供完整的JWT操作功能，包括生成带唯一ID的token、解析token、验证过期时间等
 * @author admin
 */
@Component
public class JwtUtil {

    private long expiration = 24 * 60 * 60 * 1000; // token有效期24小时
    private String secret = "11111111111111111111111111111111"; // 签名密钥

    /**
     * 生成token（带tokenId和权限信息）
     * @param userid 用户ID
     * @param username 用户名
     * @param roles 用户角色列表
     * @param permissions 用户权限列表
     */
    public String generateToken(String userid, String username, List<String> roles, List<String> permissions){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        // 生成唯一的tokenId
        String tokenId = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("tokenId", tokenId);
        claims.put("roles", roles);      // 添加角色信息
        claims.put("permissions", permissions); // 添加权限信息
        claims.put("loginTime", now.getTime()); // 登录时间

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .setSubject(userid)
                .addClaims(claims)
                .setId(tokenId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 重载方法 - 兼容旧代码
     */
    public String generateToken(String userid, String username){
        return generateToken(userid, username, new ArrayList<>(), new ArrayList<>());
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
            String tokenId = claims.get("tokenId", String.class);
            if (tokenId == null || tokenId.isEmpty()) {
                tokenId = claims.getId();
            }
            return tokenId;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取用户角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("roles", List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取用户权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissions(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("permissions", List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
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