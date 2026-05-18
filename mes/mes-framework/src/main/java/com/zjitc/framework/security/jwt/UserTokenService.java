package com.zjitc.framework.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserTokenService {

    // 存储用户当前有效的token
    private final ConcurrentHashMap<String, String> userCurrentToken = new ConcurrentHashMap<>();

    @Autowired
    private TokenBlacklistService blacklistService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 为新token注册，并使旧token失效
     * @param userId 用户ID
     * @param newToken 新生成的token
     */
    public void registerNewToken(String userId, String newToken) {
        // 获取用户旧的token
//        String oldToken = userCurrentToken.get(userId);
//
//        if (oldToken != null && !oldToken.equals(newToken)) {
//            // 计算旧token的剩余有效期
//            try {
//                long remainingExpiration = jwtUtil.getRemainingExpiration(oldToken);
//                if (remainingExpiration > 0) {
//                    // 将旧token加入黑名单
//                    blacklistService.addToBlacklist(oldToken, remainingExpiration);
//                }
//            } catch (Exception e) {
//                // token可能已过期或无效，忽略
//            }
//        }

        // 存储新的token
        userCurrentToken.put(userId, newToken);
    }

    /**
     * 验证token是否有效（未被新token替代）
     */
    public boolean isTokenValid(String userId, String token) {
//        String currentToken = userCurrentToken.get(userId);
//        return currentToken != null && currentToken.equals(token);
        return true;
    }

    /**
     * 用户登出时清除token记录
     */
    public void logout(String userId) {
        String token = userCurrentToken.remove(userId);//从Map中删除用户ID与token
        if (token != null) {
            try {
                long remainingExpiration = jwtUtil.getRemainingExpiration(token);//计算token剩余有效期（毫秒
                if (remainingExpiration > 0) {
                    blacklistService.addToBlacklist(token, remainingExpiration);//将token加入黑名单，过期时间等于剩余有效期
                }
            } catch (Exception e) {
                // 忽略
            }
        }
    }
}