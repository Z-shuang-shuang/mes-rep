package com.zjitc.sys.auth.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String username;
    private String userid;
}
