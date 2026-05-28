// SysUserService.java - 修改
package com.zjitc.admin.service;

import com.zjitc.admin.entity.SysUser;
import com.zjitc.framework.security.jwt.LoginUser;

public interface SysUserService {
    SysUser findSysUserByName(String username);
    SysUser findSysUserById(String id);

    // 新增：加载用户的角色和权限
    LoginUser loadUserAuthorities(String userId);
}