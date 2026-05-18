// 修改后的 AuthController.java

package com.zjitc.admin.controller;

import com.zjitc.admin.dto.request.UserRequest;
import com.zjitc.admin.dto.response.LoginResponse;
import com.zjitc.common.result.Result;
import com.zjitc.framework.security.context.UserContext;
import com.zjitc.framework.security.jwt.JwtUtil;
import com.zjitc.framework.security.jwt.UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserTokenService userTokenService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody UserRequest userRequest) {
        String username = userRequest.getUsername();
        String password = userRequest.getPassword();

        // TODO: 实际项目中需要验证用户名密码
        // 这里模拟验证通过，用户ID为1000
        String userId = "1000";

        // 生成新token
        String token = jwtUtil.generateToken(userId, username);

        // 【关键】注册新token，使旧token失效
        userTokenService.registerNewToken(userId, token);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUsername(username);
        loginResponse.setToken(token);
        loginResponse.setUserid(userId);

        return Result.success(loginResponse);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        String userId = UserContext.getUserId();
        if (userId != null) {
            userTokenService.logout(userId);
            UserContext.remove();
        }
        System.out.printf("退出登录成功");
        return Result.success("退出登录成功");
    }

    @GetMapping("/current-user")
    public Result<String> getCurrentUser() {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success("当前用户ID: " + userId);
    }
}