
// SysUserServiceImpl.java - 修改
package com.zjitc.sys.auth.service.impl;

import com.zjitc.sys.user.entity.SysPermission;
import com.zjitc.sys.user.entity.SysRole;
import com.zjitc.sys.auth.entity.SysUser;
import com.zjitc.sys.auth.mapper.SysUserMapper;
import com.zjitc.sys.auth.mapper.SysUserRoleMapper;
import com.zjitc.sys.auth.service.SysUserService;
import com.zjitc.framework.security.jwt.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public SysUser findSysUserByName(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public SysUser findSysUserById(String id) {
        if (id == null) {
            return null;
        }
        return sysUserMapper.selectById(Integer.parseInt(id));
    }

    // 新增方法：加载用户的角色和权限
    @Override
    public LoginUser loadUserAuthorities(String userId) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(userId);

        try {
            Integer userIdInt = Integer.parseInt(userId);

            // 获取用户角色
            Set<String> roles = sysUserRoleMapper.getRolesByUserId(userIdInt)
                    .stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toSet());
            loginUser.setRoles(roles);

            // 获取用户权限
            Set<String> permissions = sysUserRoleMapper.getPermissionsByUserId(userIdInt)
                    .stream()
                    .map(SysPermission::getPermissionCode)
                    .collect(Collectors.toSet());
            loginUser.setPermissions(permissions);

            System.out.println("📋 加载用户权限 - userId: " + userId);
            System.out.println("   角色: " + roles);
            System.out.println("   权限: " + permissions);

        } catch (Exception e) {
            System.err.println("❌ 加载用户权限失败: " + e.getMessage());
        }

        return loginUser;
    }
}