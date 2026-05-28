// PermissionInterceptor.java
package com.zjitc.framework.security.interceptor;

import com.zjitc.common.result.Result;
import com.zjitc.framework.security.annotation.RequirePermission;
import com.zjitc.framework.security.annotation.RequireRole;
import com.zjitc.framework.security.jwt.LoginUser;
import com.zjitc.framework.security.jwt.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("PermissionInterceptor preHandle");
        // 如果不是方法映射，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取当前登录用户
        LoginUser loginUser = UserContextHolder.getUser();
        if (loginUser == null) {
            writeForbidden(response, "未登录");
            return false;
        }

        // 检查角色
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && !checkRole(loginUser, requireRole)) {
            writeForbidden(response, "权限不足：缺少所需角色");
            return false;
        }

        // 检查权限
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (requirePermission != null && !checkPermission(loginUser, requirePermission)) {
            writeForbidden(response, "权限不足：缺少所需权限");
            return false;
        }

        return true;
    }

    private boolean checkRole(LoginUser user, RequireRole requireRole) {
        Set<String> userRoles = user.getRoles();
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        String[] requiredRoles = requireRole.value();
        if (requireRole.logical() == RequireRole.Logical.AND) {
            for (String role : requiredRoles) {
                if (!userRoles.contains(role)) {
                    return false;
                }
            }
            return true;
        } else {
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean checkPermission(LoginUser user, RequirePermission requirePermission) {
        Set<String> userPermissions = user.getPermissions();
        if (userPermissions == null || userPermissions.isEmpty()) {
            return false;
        }

        String[] requiredPermissions = requirePermission.value();
        if (requirePermission.logical() == RequirePermission.Logical.AND) {
            for (String perm : requiredPermissions) {
                if (!userPermissions.contains(perm)) {
                    return false;
                }
            }
            return true;
        } else {
            for (String perm : requiredPermissions) {
                if (userPermissions.contains(perm)) {
                    return true;
                }
            }
            return false;
        }
    }

    private void writeForbidden(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Result<String> result = Result.fail(403, msg);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}