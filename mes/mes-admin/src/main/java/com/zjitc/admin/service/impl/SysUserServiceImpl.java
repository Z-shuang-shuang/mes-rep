package com.zjitc.admin.service.impl;

import com.zjitc.admin.entity.SysUser;
import com.zjitc.admin.mapper.SysUserMapper;
import com.zjitc.admin.service.SysUserService;
import com.zjitc.framework.security.jwt.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public SysUser findSysUserByName(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public SysUser findSysUserById(String id) {
        // 修复 UserContextHolder 的使用方式
        // 注意：这里需要先从 ThreadLocal 获取，但如果当前线程没有设置 UserContextHolder，会报空指针
        // 建议改为从参数传入或使用其他方式
        if (id == null) {
            return null;
        }
        return sysUserMapper.selectById(Integer.parseInt(id));
    }
}