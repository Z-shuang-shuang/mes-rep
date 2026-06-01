package com.zjitc.sys.user.entity;

import lombok.Data;

@Data
public class SysPermission {
    private Integer id;
    private String permissionCode;
    private String permissionName;
    private Integer parentId;
    private Integer type;
    private String url;
    private String method;
}