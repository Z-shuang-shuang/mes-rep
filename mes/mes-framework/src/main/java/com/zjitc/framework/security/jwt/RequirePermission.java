//package com.zjitc.framework.security.jwt;
//
//import java.lang.annotation.*;
//
///**
// * 权限注解 - 用于方法级别的权限控制
// * 作用：标记需要特定权限才能访问的方法
// */
//@Target({ElementType.METHOD, ElementType.TYPE})
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//public @interface RequirePermission {
//    String[] value(); // 需要的权限列表
//    Logical logical() default Logical.AND; // 权限逻辑：AND表示都需要，OR表示任一即可
//}
//
