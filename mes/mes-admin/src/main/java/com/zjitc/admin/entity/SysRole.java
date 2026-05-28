package com.zjitc.admin.entity;

import lombok.Data;

@Data
public class SysRole {
    private Integer id;
    private String roleCode;
    private String roleName;
    private String description;
}