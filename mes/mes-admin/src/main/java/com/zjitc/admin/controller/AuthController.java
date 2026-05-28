package com.zjitc.admin.controller;

import com.zjitc.admin.dto.request.UserRequest;
import com.zjitc.admin.dto.response.LoginResponse;
import com.zjitc.admin.entity.SysUser;
import com.zjitc.admin.service.SysUserService;
import com.zjitc.common.result.Result;
import com.zjitc.framework.security.annotation.RequirePermission;
import com.zjitc.framework.security.annotation.RequireRole;
import com.zjitc.framework.security.jwt.JwtUtil;
import com.zjitc.framework.security.jwt.LoginUser;
import com.zjitc.framework.security.jwt.TokenService;
import com.zjitc.framework.security.jwt.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody UserRequest userRequest) {
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();

        // 1. 从数据库查询用户
        SysUser sysUser = sysUserService.findSysUserByName(username);

        // 2. 验证用户是否存在
        if (sysUser == null) {
            return Result.fail("用户名不存在");
        }

        // 3. 验证密码
        if (!password.equals(sysUser.getPassword())) {
            return Result.fail("密码错误");
        }

        String tokenId = UUID.randomUUID().toString();
        String userId = String.valueOf(sysUser.getId());

        // 4. 生成token
        String token = jwtUtil.generateTokenByTokenId(userId, tokenId);

        // 5. 加载用户的角色和权限（重要！）
        LoginUser loginUser = sysUserService.loadUserAuthorities(userId);
        loginUser.setUsername(sysUser.getUsername());
        loginUser.setUserid(userId);
        loginUser.setTokenId(tokenId);

        System.out.println("========== 用户登录 ==========");
        System.out.println("用户名: " + sysUser.getUsername());
        System.out.println("角色: " + loginUser.getRoles());
        System.out.println("权限: " + loginUser.getPermissions());

        // 6. 把用户信息存入redis
        tokenService.setLoginUser(userId, loginUser, tokenId);

        // 7. 返回登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUsername(sysUser.getUsername());
        loginResponse.setToken(token);
        loginResponse.setUserid(userId);

        return Result.success(loginResponse);
    }

    // 退出登录 - 所有用户都可以
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            try {
                String tokenId = jwtUtil.getTokenId(token);
                String userId = jwtUtil.getUserId(token);
                tokenService.removeToken(userId, tokenId);
            } catch (Exception e) {
                // token解析失败，忽略
            }
        }
        return Result.success("退出成功");
    }

    // 踢自己下线 - 所有用户都可以
    @DeleteMapping("/kick-self")
    public Result<String> kickSelf(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            try {
                String tokenId = jwtUtil.getTokenId(token);
                String userId = jwtUtil.getUserId(token);
                tokenService.removeAllUserTokens(userId);
            } catch (Exception e) {
                // token解析失败，忽略
            }
        }
        return Result.success("已强制下线");
    }

    // 获取当前用户信息 - 所有用户都可以
    @GetMapping("/current-user")
    public Result<Map<String, Object>> getCurrentUser() {
        LoginUser loginUser = UserContextHolder.getUser();
        if (loginUser == null) {
            return Result.fail(401, "未登录");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", loginUser.getUserid());
        userInfo.put("username", loginUser.getUsername());
        userInfo.put("roles", loginUser.getRoles());      // 返回角色
        userInfo.put("permissions", loginUser.getPermissions());  // 返回权限

        return Result.success(userInfo);
    }

    // 获取所有在线用户 - 只有管理员可以访问
    @RequireRole("ADMIN")
    @GetMapping("/online-users")
    public Result<Map<String, Set<String>>> getOnlineUsers() {
        Map<String, Set<String>> allOnlineUsers = tokenService.getAllOnlineUsers();
        return Result.success(allOnlineUsers);
    }

    // 踢用户下线 - 只有管理员可以访问
    @RequireRole("ADMIN")
    @DeleteMapping("/kick/{userId}")
    public Result<String> kickUser(@PathVariable String userId, HttpServletRequest request) {
        // 不能踢自己
        String currentUserId = null;
        try {
            String token = extractToken(request);
            if (token != null) {
                currentUserId = jwtUtil.getUserId(token);
            }
        } catch (Exception e) {
            // 忽略
        }

        if (userId.equals(currentUserId)) {
            return Result.fail("不能踢自己，请使用强制下线功能");
        }

        tokenService.removeAllUserTokens(userId);
        return Result.success("用户 " + userId + " 已被踢下线");
    }

    // 从请求头中提取token
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}