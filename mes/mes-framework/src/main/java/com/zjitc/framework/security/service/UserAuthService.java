//package com.zjitc.framework.security.service;
//
//import com.zjitc.framework.security.entity.SysUser;
//import com.zjitc.framework.security.mapper.SysPermissionMapper;
//import com.zjitc.framework.security.mapper.SysRoleMapper;
//import com.zjitc.framework.security.mapper.SysUserMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//import java.util.List;
//
//@Service
//public class UserAuthService {
//
//    @Autowired
//    private SysUserMapper userMapper;
//
//    @Autowired
//    private SysRoleMapper roleMapper;
//
//    @Autowired
//    private SysPermissionMapper permissionMapper;
//
//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//    /**
//     * 根据用户名查询用户
//     */
//    public SysUser findByUsername(String username) {
//        return userMapper.selectByUsername(username);
//    }
//
//    /**
//     * 验证密码
//     */
//    public boolean verifyPassword(String rawPassword, String encodedPassword) {
//        return passwordEncoder.matches(rawPassword, encodedPassword);
//    }
//
//    /**
//     * 获取用户角色列表（返回角色代码字符串列表）
//     */
//    public List<String> getUserRoles(String userId) {
//        return roleMapper.selectRoleCodesByUserId(userId);
//    }
//
//    /**
//     * 获取用户权限列表（返回权限代码字符串列表）
//     */
//    public List<String> getUserPermissions(String userId) {
//        return permissionMapper.selectPermissionCodesByUserId(userId);
//    }
//
//    /**
//     * 检查用户状态
//     */
//    public boolean isUserEnabled(String userId) {
//        SysUser user = userMapper.selectById(userId);
//        return user != null && user.getStatus() == 1;
//    }
//}