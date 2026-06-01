package com.zjitc.sys.user.controller;

import com.zjitc.common.result.Result;
import com.zjitc.sys.auth.entity.SysUser;
import com.zjitc.sys.auth.service.SysUserService;
import com.zjitc.sys.user.service.SysAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SysUserController {
    @Autowired
    private SysAuthService sysAuthService;

    @GetMapping("/getAll")
    public List<SysUser> getAllSysUser() {
        return sysAuthService.getAllSysUser();
    }


}
