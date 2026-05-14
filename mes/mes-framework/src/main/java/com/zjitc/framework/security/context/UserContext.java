package com.zjitc.framework.security.context;

import org.springframework.stereotype.Component;

@Component
public class UserContext {

    // 创建 ThreadLocal 变量，存储当前线程的用户ID
    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();

    // 设置用户ID到当前线程
    public static void setUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }

    // 从当前线程获取用户ID
    public static String getUserId() {
        return USER_ID_HOLDER.get();
    }

    // 移除当前线程的用户ID（防止内存泄漏）
    public static void remove() {
        USER_ID_HOLDER.remove();
    }
}