// PermissionChecker.java
package com.zjitc.framework.security.util;

import com.zjitc.framework.security.jwt.LoginUser;
import com.zjitc.framework.security.jwt.UserContextHolder;

import java.util.Set;

public class PermissionChecker {

    /**
     * 检查当前用户是否有指定角色
     */
    public static boolean hasRole(String roleCode) {
        LoginUser user = UserContextHolder.getUser();
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().contains(roleCode);
    }

    /**
     * 检查当前用户是否有任意一个角色
     */
    public static boolean hasAnyRole(String... roleCodes) {
        LoginUser user = UserContextHolder.getUser();
        if (user == null || user.getRoles() == null) {
            return false;
        }
        for (String roleCode : roleCodes) {
            if (user.getRoles().contains(roleCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查当前用户是否有指定权限
     */
    public static boolean hasPermission(String permissionCode) {
        LoginUser user = UserContextHolder.getUser();
        if (user == null || user.getPermissions() == null) {
            return false;
        }
        return user.getPermissions().contains(permissionCode);
    }

    /**
     * 检查当前用户是否有任意一个权限
     */
    public static boolean hasAnyPermission(String... permissionCodes) {
        LoginUser user = UserContextHolder.getUser();
        if (user == null || user.getPermissions() == null) {
            return false;
        }
        Set<String> userPermissions = user.getPermissions();
        for (String permissionCode : permissionCodes) {
            if (userPermissions.contains(permissionCode)) {
                return true;
            }
        }
        return false;
    }
}