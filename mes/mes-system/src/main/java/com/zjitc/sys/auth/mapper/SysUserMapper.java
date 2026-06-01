package com.zjitc.sys.auth.mapper;

import com.zjitc.sys.auth.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper {

    @Select("select * from sys_user where username=#{username}")
    SysUser selectByUsername(@Param("username") String username);

    @Select("select * from sys_user where id=#{id}")
    SysUser selectById(@Param("id") int id);
}
