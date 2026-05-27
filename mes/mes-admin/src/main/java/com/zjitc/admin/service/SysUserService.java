package com.zjitc.admin.service;

import com.zjitc.admin.entity.SysUser;

public interface SysUserService {

    SysUser findSysUserByName(String username);

    SysUser findSysUserById(String id);  // 参数类型改为 String，更灵活
}