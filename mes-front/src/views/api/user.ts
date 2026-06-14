// api/user.ts
import { http } from '@/utils/request'

// 定义接口返回的数据类型
export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  username: string
  userid: string
}

export interface UserInfo {
  userId: string
  username: string
  roles: string[]
  permissions: string[]
}

export interface OnlineUser {
  id: string
  tokenCount: number
}

// ========== 认证相关 API ==========

/**
 * 用户登录
 */
export const loginApi = (data: LoginParams) => {
  return http.post<LoginResult>('/v1/auth/login', data)
}

/**
 * 用户登出
 */
export const logoutApi = () => {
  return http.post('/v1/auth/logout')
}

/**
 * 获取当前用户信息
 */
export const getCurrentUserApi = () => {
  return http.get<UserInfo>('/v1/auth/current-user')
}

/**
 * 踢自己下线（强制下线）
 */
export const kickSelfApi = () => {
  return http.delete('/v1/auth/kick-self')
}

// ========== 管理员相关 API ==========

/**
 * 获取所有在线用户列表（需要 ADMIN 角色）
 */
export const getOnlineUsersApi = () => {
  return http.get<Record<string, string[]>>('/v1/auth/online-users')
}

/**
 * 踢指定用户下线（需要 ADMIN 角色）
 */
export const kickUserApi = (userId: string) => {
  return http.delete(`/v1/auth/kick/${userId}`)
}