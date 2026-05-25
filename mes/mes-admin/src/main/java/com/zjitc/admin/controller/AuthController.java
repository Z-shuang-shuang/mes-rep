package com.zjitc.admin.controller;

import com.zjitc.admin.dto.request.UserRequest;
import com.zjitc.admin.dto.response.LoginResponse;
import com.zjitc.common.result.Result;
import com.zjitc.framework.security.context.UserContext;
import com.zjitc.framework.security.entity.SysUser;
import com.zjitc.framework.security.jwt.*;
import com.zjitc.framework.security.service.TokenManageService;
import com.zjitc.framework.security.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private UserAuthService userAuthService;

    /**
     * 登录接口 - 从数据库查询用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody UserRequest userRequest, HttpServletRequest request) {
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();

        // 1. 从数据库查询用户
        SysUser user = userAuthService.findByUsername(username);
        if (user == null) {
            return Result.fail(401, "用户名不存在");
        }

        // 2. 验证密码
        if (!userAuthService.verifyPassword(password, user.getPassword())) {
            return Result.fail(401, "密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() != 1) {
            return Result.fail(403, "账号已被禁用，请联系管理员");
        }

        // 4. 从数据库查询角色和权限
        List<String> roles = userAuthService.getUserRoles(user.getId());
        List<String> permissions = userAuthService.getUserPermissions(user.getId());

        // 5. 构建 LoginUser 对象
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(username);
        loginUser.setRoles(roles);
        loginUser.setPermissions(permissions);
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setTokenId(UUID.randomUUID().toString().replace("-", ""));
        loginUser.setIpAddress(getClientIp(request));
        loginUser.setDeviceInfo(request.getHeader("User-Agent"));

        // 6. 生成 token
        String token = jwtUtil.generateToken(loginUser);
        loginUser.setToken(token);

        // 7. 存储到 Redis
        tokenManageService.storeLoginUser(loginUser, 24);

        // 8. 返回响应（使用你现有的 LoginResponse）
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUsername(username);
        loginResponse.setToken(token);
        loginResponse.setUserid(user.getId());

        return Result.success(loginResponse);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        LoginUser loginUser = UserContext.getLoginUser();

        if (loginUser != null) {
            tokenManageService.deleteToken(loginUser.getUserId(), loginUser.getTokenId());
            UserContext.remove();
        }
        return Result.success("退出登录成功");
    }

    /**
     * 踢用户下线 - 需要管理员权限
     */
    @DeleteMapping("/kick/{userId}")
    @RequirePermission("user:delete")
    public Result<String> kickUser(@PathVariable String userId) {
        LoginUser currentUser = UserContext.getLoginUser();

        if (currentUser.getUserId().equals(userId)) {
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
        LoginUser loginUser = UserContext.getLoginUser();

        if (loginUser != null) {
            tokenManageService.deleteToken(loginUser.getUserId(), loginUser.getTokenId());
            UserContext.remove();
        }

        return Result.success("已强制下线");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    public Result<Map<String, Object>> getCurrentUser() {
        LoginUser loginUser = UserContext.getLoginUser();

        if (loginUser == null) {
            return Result.fail(401, "未登录");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", loginUser.getUserId());
        userInfo.put("username", loginUser.getUsername());
        userInfo.put("roles", loginUser.getRoles());
        userInfo.put("permissions", loginUser.getPermissions());

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
     * 获取用户的所有在线设备
     */
    @GetMapping("/my-devices")
    public Result<Map<String, Object>> getMyDevices() {
        LoginUser loginUser = UserContext.getLoginUser();

        Set<Object> tokenIds = tokenManageService.getUserTokenIds(loginUser.getUserId());
        List<Map<String, Object>> devices = new ArrayList<>();

        for (Object tokenIdObj : tokenIds) {
            String tokenId = tokenIdObj.toString();
            Map<String, Object> device = new HashMap<>();
            device.put("tokenId", tokenId);
            device.put("remainingTTL", tokenManageService.getTokenRemainingTTL(loginUser.getUserId(), tokenId));

            if (tokenId.equals(loginUser.getTokenId())) {
                device.put("isCurrent", true);
            }
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
        LoginUser loginUser = UserContext.getLoginUser();

        if (tokenId.equals(loginUser.getTokenId())) {
            return Result.fail(400, "不能删除当前登录的设备");
        }

        Set<Object> userTokenIds = tokenManageService.getUserTokenIds(loginUser.getUserId());
        if (userTokenIds.contains(tokenId)) {
            tokenManageService.deleteToken(loginUser.getUserId(), tokenId);
            return Result.success("设备已下线");
        }

        return Result.fail(400, "设备不存在或不属于当前用户");
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}