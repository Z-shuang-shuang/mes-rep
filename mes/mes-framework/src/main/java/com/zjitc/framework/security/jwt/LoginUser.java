package com.zjitc.framework.security.jwt;

import lombok.Data;

@Data
public class LoginUser {

    //    private String token;
    private String tokenId;

    private String userid;
    private String username;
    private String password;

    //权限信息

}
