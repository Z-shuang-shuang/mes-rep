package com.zjitc.admin.controller;

import com.zjitc.admin.dto.request.UserRequest;
import com.zjitc.admin.dto.response.LoginResponse;
import com.zjitc.common.result.Result;
import com.zjitc.framework.security.context.UserContext;
import com.zjitc.framework.security.jwt.JwtUtil;
import com.zjitc.framework.security.jwt.RequirePermission;
import com.zjitc.framework.security.jwt.RequireRole;
import com.zjitc.framework.security.jwt.TokenManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenManageService tokenManageService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody UserRequest userRequest) {
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();

        // TODO: 实际应该从数据库查询用户信息和权限
        String userId = "1000";
        List<String> roles = Arrays.asList("admin", "user");
        List<String> permissions = Arrays.asList("user:read", "user:write", "user:delete", "token:manage");

        // 生成token（带角色和权限）
        String token = jwtUtil.generateToken(userId, username, roles, permissions);
        String tokenId = jwtUtil.getTokenIdFromToken(token);

        // 存储到Redis（使用新的管理服务）
        tokenManageService.storeToken(userId, tokenId, token, 24);

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
                    // 只删除当前设备的token
                    tokenManageService.deleteToken(userId, tokenId);
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
            UserContext.remove();
        }
        return Result.success("退出登录成功");
    }

    /**
     * 踢用户下线 - 需要管理员权限
     * @RequirePermission 注解表示只有拥有 user:delete 权限的用户才能访问
     */
    @DeleteMapping("/kick/{userId}")
    @RequirePermission("user:delete")
    public Result<String> kickUser(@PathVariable String userId) {
        // 获取当前用户ID，防止踢自己
        String currentUserId = UserContext.getUserId();
        if (currentUserId.equals(userId)) {
            return Result.fail(400, "不能踢自己");
        }

        tokenManageService.deleteAllUserTokens(userId);
        return Result.success("用户已踢下线");
    }

    /**
     * 踢用户指定设备下线 - 需要管理员权限
     */
    @DeleteMapping("/kick/{userId}/{tokenId}")
    @RequirePermission("token:manage")
    public Result<String> kickUserDevice(@PathVariable String userId, @PathVariable String tokenId) {
        tokenManageService.deleteToken(userId, tokenId);
        return Result.success("用户设备已踢下线");
    }

    /**
     * 用户强制下线自己（只踢当前设备）
     */
    @DeleteMapping("/kick-self")
    public Result<String> kickSelf() {
        String userId = UserContext.getUserId();
        String token = UserContext.getToken();

        if (userId != null && token != null) {
            try {
                String tokenId = jwtUtil.getTokenIdFromToken(token);
                if (tokenId != null) {
                    tokenManageService.deleteToken(userId, tokenId);
                }
            } catch (Exception e) {
                System.out.println("强制下线失败: " + e.getMessage());
            }
            UserContext.remove();
        }

        return Result.success("已强制下线");
    }

    /**
     * 获取当前用户信息 - 需要登录
     */
    @GetMapping("/current-user")
    public Result<Map<String, Object>> getCurrentUser() {
        String userId = UserContext.getUserId();
        String token = UserContext.getToken();

        if (userId == null) {
            return Result.fail(401, "未登录");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", jwtUtil.getUsername(token));
        userInfo.put("roles", jwtUtil.getRoles(token));
        userInfo.put("permissions", jwtUtil.getPermissions(token));

        return Result.success(userInfo);
    }

    /**
     * 获取所有在线用户 - 需要管理员权限
     */
    @GetMapping("/online-users")
    @RequireRole("admin")
    public Result<Map<String, Set<Object>>> getOnlineUsers() {
        Map<String, Set<Object>> onlineUsers = tokenManageService.getAllOnlineUsers();
        return Result.success(onlineUsers);
    }

    /**
     * 获取用户的所有在线设备 - 用户可以查看自己的设备
     */
    @GetMapping("/my-devices")
    public Result<Map<String, Object>> getMyDevices() {
        String userId = UserContext.getUserId();

        Set<Object> tokenIds = tokenManageService.getUserTokenIds(userId);
        List<Map<String, Object>> devices = new ArrayList<>();

        for (Object tokenIdObj : tokenIds) {
            String tokenId = tokenIdObj.toString();
            Map<String, Object> device = new HashMap<>();
            device.put("tokenId", tokenId);
            device.put("remainingTTL", tokenManageService.getTokenRemainingTTL(userId, tokenId));
            devices.add(device);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("devices", devices);
        result.put("count", devices.size());

        return Result.success(result);
    }

    /**
     * 删除指定设备（用户自己操作）
     */
    @DeleteMapping("/delete-device/{tokenId}")
    public Result<String> deleteDevice(@PathVariable String tokenId) {
        String userId = UserContext.getUserId();

        // 验证tokenId是否属于当前用户
        Set<Object> userTokenIds = tokenManageService.getUserTokenIds(userId);
        if (userTokenIds.contains(tokenId)) {
            tokenManageService.deleteToken(userId, tokenId);
            return Result.success("设备已下线");
        }

        return Result.fail(400, "设备不存在或不属于当前用户");
    }
}