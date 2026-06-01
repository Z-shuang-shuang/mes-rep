package com.zjitc.sys.user.service.impl;

import com.zjitc.sys.auth.entity.SysUser;
import com.zjitc.sys.user.service.SysAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class SysAuthServiceImpl implements SysAuthService {
    @Autowired
    private SysAuthService sysAutoService;
    @Override
    public List<SysUser> getAllSysUser() {
        return sysAutoService.getAllSysUser();
    }
}
