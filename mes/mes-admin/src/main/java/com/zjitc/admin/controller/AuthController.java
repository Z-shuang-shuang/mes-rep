package com.zjitc.admin.controller;

import com.zjitc.admin.dto.request.UserRequest;
import com.zjitc.admin.dto.response.LoginResponse;
import com.zjitc.common.result.Result;
import com.zjitc.framework.security.context.UserContext;
import com.zjitc.framework.security.jwt.JwtUtil;
import com.zjitc.framework.security.jwt.UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
@CrossOrigin
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserTokenService userTokenService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody UserRequest userRequest) {
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();

        String userId = "1000";

        // 生成token
        String token = jwtUtil.generateToken(userId, username);
        String tokenId = jwtUtil.getTokenIdFromToken(token);

        System.out.println("========== 登录开始 ==========");
        System.out.println("用户名: " + username);
        System.out.println("用户ID: " + userId);
        System.out.println("tokenId: " + tokenId);

        // 存储到Redis
        String redisKey = "user:token:" + userId + ":" + tokenId;
        redisTemplate.opsForValue().set(redisKey, token, 24, TimeUnit.HOURS);
        System.out.println("存储token: " + redisKey);

        // 记录用户的tokenId到列表中
        String userTokenListKey = "user:token:list:" + userId;
        Long added = redisTemplate.opsForSet().add(userTokenListKey, tokenId);
        redisTemplate.expire(userTokenListKey, 24, TimeUnit.HOURS);
        System.out.println("添加到列表: " + userTokenListKey + ", 添加结果: " + added);

        // 验证是否真的存进去了
        Boolean hasKey = redisTemplate.hasKey(userTokenListKey);
        System.out.println("列表是否存在: " + hasKey);

        Set<Object> members = redisTemplate.opsForSet().members(userTokenListKey);
        System.out.println("列表中的tokenIds: " + members);
        System.out.println("========== 登录结束 ==========");

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUsername(username);
        loginResponse.setToken(token);
        loginResponse.setUserid(userId);

        return Result.success(loginResponse);
    }
    @PostMapping("/logout")
    public Result<String> logout() {
        String userId = UserContext.getUserId();
        String token = UserContext.getToken();
        if (userId != null && token != null) {
            try {
                String tokenId = jwtUtil.getTokenIdFromToken(token);
                if (tokenId != null) {
                    String redisKey = "user:token:" + userId + ":" + tokenId;
                    redisTemplate.delete(redisKey);

                    String userTokenListKey = "user:token:list:" + userId;
                    redisTemplate.opsForSet().remove(userTokenListKey, tokenId);
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
            userTokenService.logout(userId);
            UserContext.remove();
        }
        return Result.success("退出登录成功");
    }

    // 管理员踢用户下线（踢掉该用户所有设备）
    @DeleteMapping("/kick/{userId}")
    public Result<String> kickUser(@PathVariable String userId) {
        String userTokenListKey = "user:token:list:" + userId;
        Set<Object> tokenIds = redisTemplate.opsForSet().members(userTokenListKey);

        if (tokenIds != null && !tokenIds.isEmpty()) {
            // 删除每个tokenId对应的token
            for (Object tokenIdObj : tokenIds) {
                String tokenId = tokenIdObj.toString();
                String redisKey = "user:token:" + userId + ":" + tokenId;
                redisTemplate.delete(redisKey);
                System.out.println("删除token: " + redisKey);
            }
            // 删除用户的token列表
            redisTemplate.delete(userTokenListKey);
            System.out.println("管理员踢下线用户: " + userId + ", 共 " + tokenIds.size() + " 个设备");
        }

        return Result.success("用户已踢下线");
    }

    // 用户强制下线自己（只踢当前设备）
    @DeleteMapping("/kick-self")
    public Result<String> kickSelf() {
        String userId = UserContext.getUserId();
        String token = UserContext.getToken();

        if (userId != null && token != null) {
            try {
                String tokenId = jwtUtil.getTokenIdFromToken(token);
                if (tokenId != null) {
                    String redisKey = "user:token:" + userId + ":" + tokenId;
                    redisTemplate.delete(redisKey);

                    String userTokenListKey = "user:token:list:" + userId;
                    redisTemplate.opsForSet().remove(userTokenListKey, tokenId);

                    System.out.println("用户 " + userId + " 强制下线当前设备");
                }
            } catch (Exception e) {
                System.out.println("强制下线失败: " + e.getMessage());
            }
            UserContext.remove();
        }

        return Result.success("已强制下线");
    }

    @GetMapping("/current-user")
    public Result<String> getCurrentUser() {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success("当前用户ID: " + userId);
    }

    // 获取所有在线用户
// 获取所有在线用户
// 获取所有在线用户
    @GetMapping("/online-users")
    public Result<Map<String, Set<Object>>> getOnlineUsers() {
        Map<String, Set<Object>> onlineUsers = new HashMap<>();

        // 直接查询用户1000（因为你的系统目前只有一个用户）
        String userKey = "user:token:list:1000";
        Boolean exists = redisTemplate.hasKey(userKey);

        if (Boolean.TRUE.equals(exists)) {
            Set<Object> tokens = redisTemplate.opsForSet().members(userKey);
            onlineUsers.put("1000", tokens);
            System.out.println("找到在线用户 1000，设备数: " + tokens.size());
        }

        return Result.success(onlineUsers);
    }
}