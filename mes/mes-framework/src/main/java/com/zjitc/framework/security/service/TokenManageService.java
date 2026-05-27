//package com.zjitc.framework.security.service;
//
//import com.zjitc.framework.security.jwt.JwtUtil;
//import com.zjitc.framework.security.jwt.LoginUser;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class TokenManageService {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    /**
//     * 存储 LoginUser 到 Redis
//     */
//    public void storeLoginUser(LoginUser loginUser, long ttlHours) {
//        String userId = loginUser.getUserId();
//        String tokenId = loginUser.getTokenId();
//
//        // 存储 LoginUser 对象
//        String loginUserKey = buildLoginUserKey(userId, tokenId);
//        redisTemplate.opsForValue().set(loginUserKey, loginUser, ttlHours, TimeUnit.HOURS);
//
//        // 存储 token 字符串
//        String tokenKey = buildTokenKey(userId, tokenId);
//        redisTemplate.opsForValue().set(tokenKey, loginUser.getToken(), ttlHours, TimeUnit.HOURS);
//
//        // 维护用户的 tokenId 列表
//        String userTokenListKey = buildUserTokenListKey(userId);
//        redisTemplate.opsForSet().add(userTokenListKey, tokenId);
//        redisTemplate.expire(userTokenListKey, ttlHours, TimeUnit.HOURS);
//
//        System.out.println("存储LoginUser成功 - userId: " + userId + ", tokenId: " + tokenId);
//    }
//
//    /**
//     * 兼容旧方法
//     */
//    public void storeToken(String userId, String tokenId, String token, long ttlHours) {
//        LoginUser loginUser = jwtUtil.parseTokenToLoginUser(token);
//        if (loginUser != null) {
//            storeLoginUser(loginUser, ttlHours);
//        } else {
//            // 降级方案
//            String tokenKey = buildTokenKey(userId, tokenId);
//            redisTemplate.opsForValue().set(tokenKey, token, ttlHours, TimeUnit.HOURS);
//
//            String userTokenListKey = buildUserTokenListKey(userId);
//            redisTemplate.opsForSet().add(userTokenListKey, tokenId);
//            redisTemplate.expire(userTokenListKey, ttlHours, TimeUnit.HOURS);
//        }
//    }
//
//    /**
//     * 获取 LoginUser
//     */
//    public LoginUser getLoginUser(String userId, String tokenId) {
//        String loginUserKey = buildLoginUserKey(userId, tokenId);
//        Object obj = redisTemplate.opsForValue().get(loginUserKey);
//        if (obj instanceof LoginUser) {
//            return (LoginUser) obj;
//        }
//        return null;
//    }
//
//    /**
//     * 获取 LoginUser (通过 token)
//     */
//    public LoginUser getLoginUserByToken(String token) {
//        try {
//            String userId = jwtUtil.getUserId(token);
//            String tokenId = jwtUtil.getTokenIdFromToken(token);
//            if (userId != null && tokenId != null) {
//                return getLoginUser(userId, tokenId);
//            }
//        } catch (Exception e) {
//            System.out.println("获取LoginUser失败: " + e.getMessage());
//        }
//        return null;
//    }
//
//    /**
//     * 验证 token 是否有效
//     */
//    public boolean validateToken(String userId, String tokenId) {
//        String tokenKey = buildTokenKey(userId, tokenId);
//        Boolean exists = redisTemplate.hasKey(tokenKey);
//        return Boolean.TRUE.equals(exists);
//    }
//
//    /**
//     * 获取 token
//     */
//    public String getToken(String userId, String tokenId) {
//        String tokenKey = buildTokenKey(userId, tokenId);
//        Object token = redisTemplate.opsForValue().get(tokenKey);
//        return token != null ? token.toString() : null;
//    }
//
//    /**
//     * 删除单个 token
//     */
//    public void deleteToken(String userId, String tokenId) {
//        String loginUserKey = buildLoginUserKey(userId, tokenId);
//        String tokenKey = buildTokenKey(userId, tokenId);
//        redisTemplate.delete(loginUserKey);
//        redisTemplate.delete(tokenKey);
//
//        String userTokenListKey = buildUserTokenListKey(userId);
//        redisTemplate.opsForSet().remove(userTokenListKey, tokenId);
//
//        System.out.println("删除token成功 - userId: " + userId + ", tokenId: " + tokenId);
//    }
//
//    /**
//     * 删除用户所有 token
//     */
//    public void deleteAllUserTokens(String userId) {
//        String userTokenListKey = buildUserTokenListKey(userId);
//        Set<Object> tokenIds = redisTemplate.opsForSet().members(userTokenListKey);
//
//        if (tokenIds != null && !tokenIds.isEmpty()) {
//            for (Object tokenIdObj : tokenIds) {
//                String tokenId = tokenIdObj.toString();
//                deleteToken(userId, tokenId);
//            }
//        }
//        redisTemplate.delete(userTokenListKey);
//
//        System.out.println("删除用户所有token成功 - userId: " + userId);
//    }
//
//    /**
//     * 获取用户所有 tokenId
//     */
//    public Set<Object> getUserTokenIds(String userId) {
//        String userTokenListKey = buildUserTokenListKey(userId);
//        Set<Object> tokenIds = redisTemplate.opsForSet().members(userTokenListKey);
//        return tokenIds != null ? tokenIds : new HashSet<>();
//    }
//
//    /**
//     * 获取所有在线用户
//     */
//    public Map<String, Set<Object>> getAllOnlineUsers() {
//        Map<String, Set<Object>> onlineUsers = new HashMap<>();
//
//        Set<String> keys = redisTemplate.keys("user:token:list:*");
//        if (keys != null && !keys.isEmpty()) {
//            for (String key : keys) {
//                String userId = key.substring(key.lastIndexOf(":") + 1);
//                Set<Object> tokenIds = redisTemplate.opsForSet().members(key);
//                if (tokenIds != null && !tokenIds.isEmpty()) {
//                    onlineUsers.put(userId, tokenIds);
//                }
//            }
//        }
//        return onlineUsers;
//    }
//
//    /**
//     * 获取 token 剩余有效时间（秒）
//     */
//    public long getTokenRemainingTTL(String userId, String tokenId) {
//        String tokenKey = buildTokenKey(userId, tokenId);
//        Long ttl = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
//        return ttl != null ? ttl : 0;
//    }
//
//    private String buildTokenKey(String userId, String tokenId) {
//        return String.format("user:token:%s:%s", userId, tokenId);
//    }
//
//    private String buildLoginUserKey(String userId, String tokenId) {
//        return String.format("user:login:%s:%s", userId, tokenId);
//    }
//
//    private String buildUserTokenListKey(String userId) {
//        return String.format("user:token:list:%s", userId);
//    }
//}