package com.zjitc.framework.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjitc.framework.security.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据用户ID查询权限代码列表（去重）
     */
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectPermissionCodesByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID查询权限实体列表（如果需要完整权限信息）
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<SysPermission> selectPermissionsByUserId(@Param("userId") String userId);
}