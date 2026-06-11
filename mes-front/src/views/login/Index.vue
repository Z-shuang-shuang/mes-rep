<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'  // 改这里：引入封装好的实例

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)

const login = async () => {
  if (loading.value) return
  loading.value = true
  
  try {
    // 用法不变，但会自动携带 token 和统一处理错误
    const res = await request.post('/v1/auth/login', {
      username: username.value,
      password: password.value
    })
    
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      router.push('/index')
    }
  } catch (err: any) {
    // 错误已在拦截器处理，这里可以什么都不写或者只记录日志
    console.error('登录失败:', err)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div style="display: flex; justify-content: center; align-items: center; height: 100vh; background: #f0f2f5;">
    <form @submit.prevent="login" style="background: white; padding: 30px; border-radius: 8px; width: 300px;">
      <h2 style="text-align: center; margin-bottom: 20px;">登录</h2>
      <input 
        v-model="username" 
        type="text" 
        placeholder="用户名" 
        style="width: 100%; padding: 10px; margin-bottom: 10px; border: 1px solid #ddd; border-radius: 4px;"
      />
      <input 
        v-model="password" 
        type="password" 
        placeholder="密码" 
        style="width: 100%; padding: 10px; margin-bottom: 20px; border: 1px solid #ddd; border-radius: 4px;"
      />
      <button type="submit" :disabled="loading" style="width: 100%; padding: 10px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer;">
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </form>
  </div>
</template>