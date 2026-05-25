package com.zjitc.framework.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.*;

@Component
public class JwtUtil {

    private long expiration = 24 * 60 * 60 * 1000;
    private String secret = "11111111111111111111111111111111";

    /**
     * 从 LoginUser 生成 token
     */
    public String generateToken(LoginUser loginUser) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", loginUser.getUsername());
        claims.put("tokenId", loginUser.getTokenId());
        claims.put("roles", loginUser.getRoles());
        claims.put("permissions", loginUser.getPermissions());
        claims.put("loginTime", loginUser.getLoginTime());

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .setSubject(loginUser.getUserId())
                .addClaims(claims)
                .setId(loginUser.getTokenId())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 兼容旧方法
     */
    public String generateToken(String userId, String username, List<String> roles, List<String> permissions) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        loginUser.setRoles(roles);
        loginUser.setPermissions(permissions);
        loginUser.setTokenId(UUID.randomUUID().toString().replace("-", ""));
        loginUser.setLoginTime(System.currentTimeMillis());
        return generateToken(loginUser);
    }

    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    public String getTokenIdFromToken(String token) {
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

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("roles", List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermissions(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("permissions", List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public boolean isExp(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    /**
     * 获取 token 剩余有效时间（毫秒）
     */
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

    /**
     * 获取 token 剩余有效时间（秒）
     */
    public long getRemainingExpirationSeconds(String token) {
        return getRemainingExpiration(token) / 1000;
    }

    /**
     * 获取 token 过期时间
     */
    public Date getExpirationDate(String token) {
        try {
            return parseToken(token).getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 token 为 LoginUser
     */
    public LoginUser parseTokenToLoginUser(String token) {
        try {
            Claims claims = parseToken(token);
            LoginUser loginUser = new LoginUser();
            loginUser.setToken(token);
            loginUser.setUserId(claims.getSubject());
            loginUser.setUsername(claims.get("username", String.class));
            loginUser.setTokenId(claims.get("tokenId", String.class));
            loginUser.setRoles(claims.get("roles", List.class));
            loginUser.setPermissions(claims.get("permissions", List.class));
            loginUser.setLoginTime(claims.get("loginTime", Long.class));
            return loginUser;
        } catch (Exception e) {
            return null;
        }
    }
}