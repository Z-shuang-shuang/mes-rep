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