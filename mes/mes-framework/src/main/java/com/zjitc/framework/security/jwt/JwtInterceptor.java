package com.zjitc.framework.security.jwt;

import com.zjitc.common.result.Result;
import com.zjitc.framework.security.context.UserContext;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService blacklistService;

    @Autowired
    private UserTokenService userTokenService;

    // 使用Spring Boot自带的RedisTemplate
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response,
                             Object handler) throws Exception {

        String method = request.getMethod();
        if (method.equals("OPTIONS")) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.contains("/login")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        System.out.println("========== 拦截器开始 ==========");
        System.out.println("请求路径: " + path);
        System.out.println("Authorization: " + authorization);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("❌ Authorization为空或格式错误");
            writeUnauthorized(response, "认证失败");
            return false;
        }

        try {
            String token = authorization.substring(7);
            System.out.println("Token: " + token);

            // 1. 检查token是否在黑名单中
            if (blacklistService.isBlacklisted(token)) {
                System.out.println("❌ token在黑名单中");
                writeUnauthorized(response, "token已失效，请重新登录");
                return false;
            }

            // 2. 检查token是否过期
            boolean exp = jwtUtil.isExp(token);
            System.out.println("token是否过期: " + exp);
            if (exp) {
                System.out.println("❌ token已过期");
                writeUnauthorized(response, "token已过期，请重新登录");
                return false;
            }

            // 3. 解析token，获取userId和tokenId
            String userId = jwtUtil.getUserId(token);
            String tokenId = jwtUtil.getTokenIdFromToken(token);
            System.out.println("解析结果 - userId: " + userId + ", tokenId: " + tokenId);

            // 4. 从Redis验证token是否存在
            String redisKey = "user:token:" + userId + ":" + tokenId;
            System.out.println("Redis Key: " + redisKey);

            if (redisTemplate != null) {
                Boolean exists = redisTemplate.hasKey(redisKey);
                System.out.println("Redis中是否存在: " + exists);

                if (!Boolean.TRUE.equals(exists)) {
                    System.out.println("❌ Redis中不存在该token");
                    writeUnauthorized(response, "token已失效，请重新登录");
                    return false;
                }
            } else {
                System.out.println("⚠️ RedisTemplate为null");
            }

            // 5. 设置用户信息到上下文
            request.setAttribute("userId", userId);
            UserContext.setUserId(userId);
            UserContext.setToken(token);

            System.out.println("✅ token验证通过");
            System.out.println("========== 拦截器结束 ==========");

            return true;
        } catch (Exception e) {
            System.out.println("❌ 验证异常: " + e.getMessage());
            e.printStackTrace();
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
        String json = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", msg);
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(json);
    }
}