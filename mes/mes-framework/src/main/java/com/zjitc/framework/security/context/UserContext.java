package com.zjitc.framework.security.context;

import org.springframework.stereotype.Component;

@Component
public class UserContext {

    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> TOKEN_HOLDER = new ThreadLocal<>();

    public static void setUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static String getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void setToken(String token) {
        TOKEN_HOLDER.set(token);
    }

    public static String getToken() {
        return TOKEN_HOLDER.get();
    }

    public static void remove() {
        USER_ID_HOLDER.remove();
        TOKEN_HOLDER.remove();
    }
}