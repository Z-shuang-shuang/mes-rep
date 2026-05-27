package com.zjitc.admin.mapper;

import com.zjitc.admin.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper {

    @Select("select * from sys_user where username=#{username}")
    SysUser selectByUsername(@Param("username") String username);

    @Select("select * from sys_user where id=#{id}")
    SysUser selectById(@Param("id") int id);
}
