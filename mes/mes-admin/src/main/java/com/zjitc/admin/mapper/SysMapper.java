package com.zjitc.admin.mapper;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zjitc.admin.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysMapper {

    @Select("select * from user where username=#{username};")
    SysUser getUserByName(@Param("username") String username);

    @Select("select * from user where id=#{id};")
    SysUser getUserById(@Param("id") Integer id);
}
