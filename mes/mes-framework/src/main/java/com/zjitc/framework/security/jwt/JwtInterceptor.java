package com.zjitc.framework.security.jwt;

import com.zjitc.framework.security.context.UserContext;
import com.zjitc.framework.security.service.TokenManageService;
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
    private TokenManageService tokenManageService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if (method.equals("OPTIONS")) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.contains("/login") || path.contains("/register")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "认证失败");
            return false;
        }

        try {
            String token = authorization.substring(7);
            request.setAttribute("token", token);

            // 1. 检查 token 是否过期
            if (jwtUtil.isExp(token)) {
                writeUnauthorized(response, "token已过期，请重新登录");
                return false;
            }

            // 2. 解析 token 获取 userId 和 tokenId
            String userId = jwtUtil.getUserId(token);
            String tokenId = jwtUtil.getTokenIdFromToken(token);

            // 3. 从 Redis 验证 token 是否存在
            if (!tokenManageService.validateToken(userId, tokenId)) {
                writeUnauthorized(response, "token已失效，请重新登录");
                return false;
            }

            // 4. 获取 LoginUser 并存储到上下文
            LoginUser loginUser = tokenManageService.getLoginUser(userId, tokenId);
            if (loginUser == null) {
                // 降级方案：从 token 解析
                loginUser = jwtUtil.parseTokenToLoginUser(token);
            }

            if (loginUser != null) {
                loginUser.setToken(token);
                UserContext.setLoginUser(loginUser);
            } else {
                UserContext.setUserId(userId);
                UserContext.setToken(token);
            }

            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "认证失败: " + e.getMessage());
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