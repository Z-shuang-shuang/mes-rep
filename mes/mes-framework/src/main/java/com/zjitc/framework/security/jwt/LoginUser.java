package com.zjitc.framework.security.jwt;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class LoginUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;           // JWT token
    private String tokenId;         // token唯一标识
    private String userId;          // 用户ID
    private String username;        // 用户名
    private List<String> roles;     // 角色列表
    private List<String> permissions; // 权限列表
    private Long loginTime;         // 登录时间
    private String ipAddress;       // 登录IP
    private String deviceInfo;      // 设备信息
}