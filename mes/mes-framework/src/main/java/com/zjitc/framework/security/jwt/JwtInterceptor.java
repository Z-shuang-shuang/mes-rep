package com.zjitc.framework.security.jwt;

import com.zjitc.common.result.Result;
import com.zjitc.framework.security.context.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
// 修改后的 JwtInterceptor.java

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private TokenBlacklistService blacklistService;

    @Autowired
    private UserTokenService userTokenService;

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response,
                             Object handler) throws Exception {

        String method = request.getMethod();
        if (method.equals("OPTIONS")) {
            return true;
        }

        // 白名单路径（不需要认证的接口）
        String path = request.getRequestURI();
        if (path.contains("/login") || path.contains("/logout")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "认证失败");
            return false;
        }

        try {
            String token = authorization.substring(7);

            // 1. 检查token是否在黑名单中
            if (blacklistService.isBlacklisted(token)) {
                writeUnauthorized(response, "token已失效，请重新登录");
                return false;
            }

            // 2. 检查token是否过期
            boolean exp = jwtUtil.isExp(token);
            if (exp) {
                writeUnauthorized(response, "token已过期，请重新登录");
                return false;
            }

            // 3. 解析token，获取userId
            String userId = jwtUtil.getUserId(token);

            // 4. 检查该token是否是用户当前有效的token
            if (!userTokenService.isTokenValid(userId, token)) {
                writeUnauthorized(response, "账号已在其他地方登录，请重新登录");
                return false;
            }

            // 5. 设置用户信息到上下文
            request.setAttribute("userId", userId);
            UserContext.setUserId(userId);

            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "认证失败");
            return false;
        }
    }

    @Override
    public void afterCompletion(jakarta.servlet.http.HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        Result<String> result = Result.fail(401, msg);
        String res = objectMapper.writeValueAsString(result);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(res);
    }
}