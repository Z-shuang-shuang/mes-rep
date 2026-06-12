// stores/user.ts
import { defineStore } from 'pinia'
import { http } from '@/utils/request'

interface UserState {
  token: string | null
  userInfo: any | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token'),
    userInfo: null
  }),
  
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    clearToken() {
      this.token = null
      localStorage.removeItem('token')
    },
    
    async getUserInfo() {
      try {
        const res = await http.get('/v1/auth/current-user')
        this.userInfo = res.data
        return res.data
      } catch (error) {
        console.error('获取用户信息失败', error)
        throw error
      }
    },
    
    logout() {
      this.clearToken()
      this.userInfo = null
    }
  }
})