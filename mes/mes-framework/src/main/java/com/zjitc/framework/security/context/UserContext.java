//package com.zjitc.framework.security.context;
//
//import com.zjitc.framework.security.jwt.LoginUser;
//
//public class UserContext {
//
//    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();
//    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();
//    private static final ThreadLocal<LoginUser> loginUserHolder = new ThreadLocal<>();
//
//    public static void setUserId(String userId) {
//        userIdHolder.set(userId);
//    }
//
//    public static String getUserId() {
//        return userIdHolder.get();
//    }
//
//    public static void setToken(String token) {
//        tokenHolder.set(token);
//    }
//
//    public static String getToken() {
//        return tokenHolder.get();
//    }
//
//    public static void setLoginUser(LoginUser loginUser) {
//        loginUserHolder.set(loginUser);
//        if (loginUser != null) {
//            // 修改这里：getUserid() 改为 getUserId()
//            userIdHolder.set(loginUser.getUserId());  // 注意：是 getUserId() 不是 getUserid()
//            tokenHolder.set(loginUser.getToken());
//        }
//    }
//
//    public static LoginUser getLoginUser() {
//        return loginUserHolder.get();
//    }
//
//    public static void remove() {
//        userIdHolder.remove();
//        tokenHolder.remove();
//        loginUserHolder.remove();
//    }
//}