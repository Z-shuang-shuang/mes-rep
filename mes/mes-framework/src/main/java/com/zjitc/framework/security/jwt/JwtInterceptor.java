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

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) throws Exception {

        String method = request.getMethod();
        if (method.equals("OPTIONS")) {//跨域预检请求，放行
            return true;
        }
        //1、从请求头中拿token

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {//Bearer eeeerrttt4545566667777785444443333333
            //给前端响应
            writeUnauthorized(response, "认证失败");
            //拦截下来，不放行
             return false;
        }
        try {
            String token = authorization.substring(7);
            boolean exp = jwtUtil.isExp(token);
            if (exp) {
                //给前端响应
                writeUnauthorized(response, "认证失败");
                //拦截下来，不放行
                return false;
            }
            //2、解析token，拿出userid
            String userId = jwtUtil.getUserId(token);//隐藏的有对token的解析
            //3、设置userid，方便后续拿到userid,有以下两种方式：
            //3.1 方式一：放在reqest
            request.setAttribute("userId", userId);
            //3.2 方式二：放在ThreadLocal（当前请求的线程的上下文）
            //todo:
            UserContext.setUserId(userId);  // 设置到 ThreadLocal
            return  true;
        }catch (Exception e){
            //给前端响应
            writeUnauthorized(response, "认证失败");
            //拦截下来，不放行
            return false;
        }
    }

    @Override
    public void afterCompletion(jakarta.servlet.http.HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        UserContext.remove();  // 清理 ThreadLocal
    }

    private void writeUnauthorized( HttpServletResponse response, String msg) throws IOException {
        Result<String> result = Result.fail(401, msg);//构造出来失败的结果对象
        String res = objectMapper.writeValueAsString(result);//把对象转json字符串
        response.getWriter().write(res);//把json字符串返回给前端
    }
}
