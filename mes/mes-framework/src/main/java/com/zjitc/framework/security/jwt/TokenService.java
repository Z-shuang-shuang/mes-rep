package com.zjitc.framework.security.jwt;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final static String LOGIN_TOKEN_PREFIX = "login:tokenid:";
    private final static String LOGIN_TOKEN_SET_PREFIX = "login:userid:{userid}:tokens";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // 存储登录用户信息
    public void setLoginUser(String userid, LoginUser loginUser, String tokenId) {
        try {
            String jsonLoginUser = objectMapper.writeValueAsString(loginUser);

            // 1. 存储token对应的用户信息
            String tokenKey = LOGIN_TOKEN_PREFIX + tokenId;
            stringRedisTemplate.opsForValue().set(
                    tokenKey,
                    jsonLoginUser,
                    jwtUtil.getExpiration(),
                    TimeUnit.MILLISECONDS
            );

            // 2. 将tokenId添加到用户token集合中（支持多设备登录）
            String setKey = LOGIN_TOKEN_SET_PREFIX.replace("{userid}", userid);
            stringRedisTemplate.opsForSet().add(setKey, tokenId);
            stringRedisTemplate.expire(setKey, jwtUtil.getExpiration(), TimeUnit.MILLISECONDS);

            System.out.println("✅ 用户 " + userid + " 登录成功，tokenId: " + tokenId);
            System.out.println("   Redis存储: " + tokenKey);
            System.out.println("   用户token集合: " + setKey);
        } catch (Exception e) {
            System.err.println("❌ 存储用户信息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 获取登录用户信息
    public LoginUser getLoginUser(String tokenId) {
        try {
            String tokenKey = LOGIN_TOKEN_PREFIX + tokenId;
            String jsonLoginUser = stringRedisTemplate.opsForValue().get(tokenKey);

            System.out.println("🔍 查询token: " + tokenKey);
            System.out.println("   结果: " + (jsonLoginUser != null ? "存在" : "不存在"));

            if (StringUtils.isEmpty(jsonLoginUser)) {
                return null;
            }
            return objectMapper.readValue(jsonLoginUser, LoginUser.class);
        } catch (Exception e) {
            System.err.println("❌ 获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 获取所有在线用户
    public Map<String, Set<String>> getAllOnlineUsers() {
        Map<String, Set<String>> result = new HashMap<>();

        String pattern = "login:userid:*:tokens";
        Set<String> keys = stringRedisTemplate.keys(pattern);

        System.out.println("📋 查找在线用户keys: " + keys);

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                // 从 key "login:userid:1:tokens" 中提取 userId
                String[] parts = key.split(":");
                if (parts.length >= 3) {
                    String userId = parts[2];
                    Set<String> tokens = stringRedisTemplate.opsForSet().members(key);

                    System.out.println("   用户 " + userId + " 的token集合: " + tokens);

                    if (tokens != null && !tokens.isEmpty()) {
                        result.put(userId, tokens);
                    }
                }
            }
        }

        System.out.println("📊 最终返回在线用户: " + result);
        return result;
    }

    // 删除用户的所有token（踢人下线）
    public void removeAllUserTokens(String userId) {
        System.out.println("🔨 开始踢掉用户: " + userId);

        String setKey = LOGIN_TOKEN_SET_PREFIX.replace("{userid}", userId);
        Set<String> tokens = stringRedisTemplate.opsForSet().members(setKey);

        System.out.println("   找到token集合: " + tokens);

        if (tokens != null && !tokens.isEmpty()) {
            for (String tokenId : tokens) {
                String tokenKey = LOGIN_TOKEN_PREFIX + tokenId;
                Boolean deleted = stringRedisTemplate.delete(tokenKey);
                System.out.println("   删除token: " + tokenKey + " " + (deleted ? "✅成功" : "❌失败"));
            }
        }

        Boolean deletedSet = stringRedisTemplate.delete(setKey);
        System.out.println("   删除用户token集合: " + setKey + " " + (deletedSet ? "✅成功" : "❌失败"));
        System.out.println("✅ 用户 " + userId + " 已被踢下线");
    }

    // 删除单个token（退出登录）
    public void removeToken(String userId, String tokenId) {
        System.out.println("🔨 用户 " + userId + " 退出登录，删除token: " + tokenId);

        String setKey = LOGIN_TOKEN_SET_PREFIX.replace("{userid}", userId);
        stringRedisTemplate.opsForSet().remove(setKey, tokenId);

        String tokenKey = LOGIN_TOKEN_PREFIX + tokenId;
        stringRedisTemplate.delete(tokenKey);

        System.out.println("✅ token已删除");
    }

    // 检查token是否有效
    public boolean isTokenValid(String tokenId) {
        String tokenKey = LOGIN_TOKEN_PREFIX + tokenId;
        Boolean hasKey = stringRedisTemplate.hasKey(tokenKey);
        return hasKey != null && hasKey;
    }
}