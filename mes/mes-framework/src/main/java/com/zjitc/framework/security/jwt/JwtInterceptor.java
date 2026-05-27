package com.zjitc.framework.security.jwt;

import com.zjitc.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 处理OPTIONS预检请求
        String method = request.getMethod();
        if (method.equals("OPTIONS")) {
            return true;
        }

        // 从请求头中获取token
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "认证失败：未提供token");
            return false;
        }

        try {
            String token = authorization.substring(7);

            // 1. 检查token是否过期
            boolean isExpired = jwtUtil.isExp(token);
            if (isExpired) {
                writeUnauthorized(response, "认证失败：token已过期");
                return false;
            }

            // 2. 解析token，获取用户ID和tokenId
            String userId = jwtUtil.getUserId(token);
            String tokenId = jwtUtil.getTokenId(token);

            System.out.println("🔍 拦截器验证token - userId: " + userId + ", tokenId: " + tokenId);

            // 3. 从Redis中检查token是否存在（关键！）
            LoginUser loginUser = tokenService.getLoginUser(tokenId);
            if (loginUser == null) {
                System.out.println("❌ token在Redis中不存在，用户已被踢下线");
                writeUnauthorized(response, "认证失败：用户已被踢下线或会话已过期");
                return false;
            }

            // 4. 验证token中的userId是否与Redis中的一致
            if (!userId.equals(loginUser.getUserid())) {
                System.out.println("❌ userId不匹配: token中=" + userId + ", Redis中=" + loginUser.getUserid());
                writeUnauthorized(response, "认证失败：用户信息不匹配");
                return false;
            }

            System.out.println("✅ token验证通过，用户: " + loginUser.getUsername());

            // 5. 将用户信息存储到ThreadLocal中
            UserContextHolder.setUser(loginUser);
            request.setAttribute("userId", userId);
            request.setAttribute("username", loginUser.getUsername());

            return true;

        } catch (Exception e) {
            System.err.println("❌ token验证异常: " + e.getMessage());
            e.printStackTrace();
            writeUnauthorized(response, "认证失败：" + e.getMessage());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理ThreadLocal，防止内存泄漏
        UserContextHolder.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Result<String> result = Result.fail(401, msg);
        String jsonResult = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResult);
        response.getWriter().flush();

        System.out.println("🔒 返回401未授权: " + msg);
    }
}