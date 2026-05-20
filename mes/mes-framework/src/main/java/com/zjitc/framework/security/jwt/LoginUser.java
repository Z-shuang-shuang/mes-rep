package com.zjitc.framework.security.jwt;

import lombok.Data;
import java.io.Serializable;

@Data
public class LoginUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;
    private String tokenId;
    private String userid;
    private String username;
}