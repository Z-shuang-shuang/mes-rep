package com.zjitc.framework.security.jwt;

/**
 * 用户上下文持有者
 * 请求到达后端 --> tomcat收到请求，分配线程来处理请求(一个请求=一个线程)
 * 使用静态ThreadLocal确保在同一线程的任何地方都能获取到用户信息
 */
public class UserContextHolder {

    /**
     * 静态ThreadLocal，所有实例共享同一个ThreadLocal
     */
    private static final ThreadLocal<LoginUser> userContext = new ThreadLocal<>();

    /**
     * 获取当前线程中的用户信息
     * @return 当前登录用户
     */
    public static LoginUser getUser() {
        return userContext.get();
    }

    /**
     * 设置当前线程中的用户信息
     * @param loginUser 登录用户
     */
    public static void setUser(LoginUser loginUser) {
        userContext.set(loginUser);
    }

    /**
     * 清除当前线程中的用户信息（防止内存泄漏）
     */
    public static void clear() {
        userContext.remove();
    }
}