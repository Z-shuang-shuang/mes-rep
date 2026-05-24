package com.zjitc.framework.security.jwt;

import com.zjitc.framework.security.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService blacklistService;

    @Autowired
    private TokenManageService tokenManageService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if (method.equals("OPTIONS")) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.contains("/login")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "认证失败");
            return false;
        }

        try {
            String token = authorization.substring(7);
            request.setAttribute("token", token); // 保存token供后续使用

            // 1. 检查token是否在黑名单中
            if (blacklistService.isBlacklisted(token)) {
                writeUnauthorized(response, "token已失效，请重新登录");
                return false;
            }

            // 2. 检查token是否过期
            if (jwtUtil.isExp(token)) {
                writeUnauthorized(response, "token已过期，请重新登录");
                return false;
            }

            // 3. 解析token，获取userId和tokenId
            String userId = jwtUtil.getUserId(token);
            String tokenId = jwtUtil.getTokenIdFromToken(token);

            // 4. 从Redis验证token是否存在
            if (!tokenManageService.validateToken(userId, tokenId)) {
                writeUnauthorized(response, "token已失效，请重新登录");
                return false;
            }

            // 5. 设置用户信息到上下文
            UserContext.setUserId(userId);
            UserContext.setToken(token);

            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "认证失败");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        String json = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", msg);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(json);
    }
}