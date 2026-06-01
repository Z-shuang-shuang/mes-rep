// SysUserRoleMapper.java
package com.zjitc.sys.auth.mapper;

import com.zjitc.sys.user.entity.SysPermission;
import com.zjitc.sys.user.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysUserRoleMapper {

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<SysRole> getRolesByUserId(@Param("userId") Integer userId);//查询用户拥有哪些角色

    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<SysPermission> getPermissionsByUserId(@Param("userId") Integer userId);//查询用户拥有哪些权限（通过角色间接获得）
}