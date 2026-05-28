// LoginUser.java - 修改
package com.zjitc.framework.security.jwt;

import lombok.Data;
import java.util.Set;

@Data
public class LoginUser {
    private String tokenId;
    private String userid;
    private String username;
    private String password;

    // 新增：角色和权限
    private Set<String> roles;        // 角色代码集合
    private Set<String> permissions;  // 权限代码集合
}