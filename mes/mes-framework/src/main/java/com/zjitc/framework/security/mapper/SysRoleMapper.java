//package com.zjitc.framework.security.mapper;
//
//import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import com.zjitc.framework.security.entity.SysRole;
//import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Param;
//import org.apache.ibatis.annotations.Select;
//import java.util.List;
//
//@Mapper
//public interface SysRoleMapper extends BaseMapper<SysRole> {
//
//    /**
//     * 根据用户ID查询角色代码列表
//     */
//    @Select("SELECT r.role_code FROM sys_role r " +
//            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
//            "WHERE ur.user_id = #{userId}")
//    List<String> selectRoleCodesByUserId(@Param("userId") String userId);
//
//    /**
//     * 根据用户ID查询角色实体列表（如果需要完整角色信息）
//     */
//    @Select("SELECT r.* FROM sys_role r " +
//            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
//            "WHERE ur.user_id = #{userId}")
//    List<SysRole> selectRolesByUserId(@Param("userId") String userId);
//}