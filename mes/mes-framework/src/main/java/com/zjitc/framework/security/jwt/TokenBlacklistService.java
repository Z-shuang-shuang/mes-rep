package com.zjitc.framework.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TokenBlacklistService {

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    // 如果没有Redis，使用内存存储（简单场景）
    private final java.util.concurrent.ConcurrentHashMap<String, Long> localBlacklist = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 将token加入黑名单
     * @param token 需要失效的token
     * @param expiration 原token的剩余有效期（毫秒）
     */
    public void addToBlacklist(String token, long expiration) {
        if (redisTemplate != null) {
            // 使用Redis，设置过期时间等于token剩余有效期
            redisTemplate.opsForValue().set(
                    "blacklist:token:" + token,
                    "expired",
                    expiration,
                    TimeUnit.MILLISECONDS
            );
        } else {
            // 使用本地缓存
            localBlacklist.put(token, System.currentTimeMillis() + expiration);
        }
    }

    /**
     * 检查token是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        if (redisTemplate != null) {
            return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:token:" + token));
        } else {
            Long expireTime = localBlacklist.get(token);
            if (expireTime != null && expireTime > System.currentTimeMillis()) {
                return true;
            } else if (expireTime != null) {
                localBlacklist.remove(token); // 清理过期的
            }
            return false;
        }
    }
}