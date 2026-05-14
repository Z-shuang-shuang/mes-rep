package com.zjitc.admin.controller;

import com.zjitc.admin.dto.request.UserRequest;
import com.zjitc.admin.dto.response.LoginResponse;
import com.zjitc.common.result.Result;
import com.zjitc.framework.security.context.UserContext;
import com.zjitc.framework.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @RequestMapping("/login")
    public Result<LoginResponse> login(@RequestBody UserRequest userRequest) {

        String username = userRequest.getUsername();
        String password = userRequest.getPassword();

        String token = jwtUtil.generateToken("1000", username);

        // 方式一：从 request 中获取（需要在拦截器里先 setAttribute）
        // String userIdFromRequest = (String) request.getAttribute("userId");

        // 方式二：从 ThreadLocal 中获取（更推荐，无需传递 request 参数）
        String userIdFromThreadLocal = UserContext.getUserId();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUsername(username);
        loginResponse.setToken(token);
        loginResponse.setUserid(userIdFromThreadLocal);  // 设置用户ID

        return Result.success(loginResponse);
    }

    // 示例：需要获取当前登录用户ID的接口
    @GetMapping("/current-user")
    public Result<String> getCurrentUser() {
        String userId = UserContext.getUserId();
        return Result.success("当前用户ID: " + userId);
    }
}