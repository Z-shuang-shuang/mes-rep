package com.zjitc.framework.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class TokenManageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;  // 使用Object类型

    /**
     * 存储token
     */
    public void storeToken(String userId, String tokenId, String token, long ttl) {
        String tokenKey = buildTokenKey(userId, tokenId);
        redisTemplate.opsForValue().set(tokenKey, token, ttl, TimeUnit.HOURS);

        String userTokenListKey = buildUserTokenListKey(userId);
        redisTemplate.opsForSet().add(userTokenListKey, tokenId);
        redisTemplate.expire(userTokenListKey, ttl, TimeUnit.HOURS);

        System.out.println("存储token成功 - userId: " + userId + ", tokenId: " + tokenId);
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String userId, String tokenId) {
        String tokenKey = buildTokenKey(userId, tokenId);
        Boolean exists = redisTemplate.hasKey(tokenKey);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 获取token
     */
    public String getToken(String userId, String tokenId) {
        String tokenKey = buildTokenKey(userId, tokenId);
        Object token = redisTemplate.opsForValue().get(tokenKey);
        return token != null ? token.toString() : null;
    }

    /**
     * 删除单个token
     */
    public void deleteToken(String userId, String tokenId) {
        String tokenKey = buildTokenKey(userId, tokenId);
        redisTemplate.delete(tokenKey);

        String userTokenListKey = buildUserTokenListKey(userId);
        redisTemplate.opsForSet().remove(userTokenListKey, tokenId);

        System.out.println("删除token成功 - userId: " + userId + ", tokenId: " + tokenId);
    }

    /**
     * 删除用户所有token
     */
    public void deleteAllUserTokens(String userId) {
        String userTokenListKey = buildUserTokenListKey(userId);
        Set<Object> tokenIds = redisTemplate.opsForSet().members(userTokenListKey);

        if (tokenIds != null && !tokenIds.isEmpty()) {
            for (Object tokenIdObj : tokenIds) {
                String tokenId = tokenIdObj.toString();
                deleteToken(userId, tokenId);
            }
        }
        redisTemplate.delete(userTokenListKey);

        System.out.println("删除用户所有token成功 - userId: " + userId);
    }

    /**
     * 获取用户所有tokenId
     */
    public Set<Object> getUserTokenIds(String userId) {
        String userTokenListKey = buildUserTokenListKey(userId);
        Set<Object> tokenIds = redisTemplate.opsForSet().members(userTokenListKey);
        return tokenIds != null ? tokenIds : new HashSet<>();
    }

    /**
     * 获取所有在线用户
     */
    public Map<String, Set<Object>> getAllOnlineUsers() {
        Map<String, Set<Object>> onlineUsers = new HashMap<>();

        Set<String> keys = redisTemplate.keys("user:token:list:*");
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                String userId = key.substring(key.lastIndexOf(":") + 1);
                Set<Object> tokenIds = redisTemplate.opsForSet().members(key);
                if (tokenIds != null && !tokenIds.isEmpty()) {
                    onlineUsers.put(userId, tokenIds);
                }
            }
        }
        return onlineUsers;
    }

    /**
     * 获取token剩余有效时间（秒）
     */
    public long getTokenRemainingTTL(String userId, String tokenId) {
        String tokenKey = buildTokenKey(userId, tokenId);
        Long ttl = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : 0;
    }

    private String buildTokenKey(String userId, String tokenId) {
        return String.format("user:token:%s:%s", userId, tokenId);
    }

    private String buildUserTokenListKey(String userId) {
        return String.format("user:token:list:%s", userId);
    }
}