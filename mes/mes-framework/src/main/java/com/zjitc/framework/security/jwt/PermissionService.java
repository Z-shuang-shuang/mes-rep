package com.zjitc.framework.security.jwt;

import com.zjitc.framework.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限验证服务 - 负责检查用户是否有权限访问资源
 * 作用：集中处理权限验证逻辑
 */
@Component
public class PermissionService {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 验证用户是否有指定权限
     * @param token JWT token
     * @param requiredPermissions 需要的权限
     * @param logical 逻辑（AND/OR）
     */
    public boolean hasPermission(String token, String[] requiredPermissions, Logical logical) {
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return true;
        }

        List<String> userPermissions = jwtUtil.getPermissions(token);

        if (logical == Logical.AND) {
            // 需要所有权限
            for (String permission : requiredPermissions) {
                if (!userPermissions.contains(permission)) {
                    return false;
                }
            }
            return true;
        } else {
            // 只需要任一权限
            for (String permission : requiredPermissions) {
                if (userPermissions.contains(permission)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 验证用户是否有指定角色
     */
    public boolean hasRole(String token, String[] requiredRoles, Logical logical) {
        if (requiredRoles == null || requiredRoles.length == 0) {
            return true;
        }

        List<String> userRoles = jwtUtil.getRoles(token);

        if (logical == Logical.AND) {
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
}