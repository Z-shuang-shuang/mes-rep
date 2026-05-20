package com.zjitc.framework.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserTokenService {

    // 可选保留，用于其他场景
    private final ConcurrentHashMap<String, String> userCurrentToken = new ConcurrentHashMap<>();

    @Autowired
    private TokenBlacklistService blacklistService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 【已废弃】支持多端登录后不再使用
     */
    // public void registerNewToken(String userId, String newToken) {
    //     // 已废弃
    // }

    /**
     * 【已废弃】支持多端登录后不再使用
     */
    public boolean isTokenValid(String userId, String token) {
        return true; // 始终返回true，验证交给Redis
    }

    /**
     * 用户登出时清除token记录（保留用于兼容）
     */
    public void logout(String userId) {
        String token = userCurrentToken.remove(userId);
        if (token != null) {
            try {
                long remainingExpiration = jwtUtil.getRemainingExpiration(token);
                if (remainingExpiration > 0) {
                    blacklistService.addToBlacklist(token, remainingExpiration);
                }
            } catch (Exception e) {
                // 忽略
            }
        }
    }
}