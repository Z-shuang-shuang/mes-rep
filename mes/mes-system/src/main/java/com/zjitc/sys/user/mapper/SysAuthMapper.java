package com.zjitc.sys.user.mapper;

import com.zjitc.sys.auth.entity.SysUser;
import com.zjitc.sys.user.entity.SysRole;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysAuthMapper {
    @Select("select * from sys_user;")
    public List<SysUser> getSysUsers();
}
