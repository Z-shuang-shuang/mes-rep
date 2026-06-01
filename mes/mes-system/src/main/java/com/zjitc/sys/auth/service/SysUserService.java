// SysUserService.java - 修改
package com.zjitc.sys.auth.service;

import com.zjitc.sys.auth.entity.SysUser;
import com.zjitc.framework.security.jwt.LoginUser;

public interface SysUserService {
    SysUser findSysUserByName(String username);
    SysUser findSysUserById(String id);

    // 新增：加载用户的角色和权限
    LoginUser loadUserAuthorities(String userId);
}