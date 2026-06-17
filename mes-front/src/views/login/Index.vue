<!-- views/login/Index.vue -->
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { loginApi, getCurrentUserApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const password = ref('')
const loading = ref(false)

const login = async () => {
  if (loading.value) return
  loading.value = true
  
  try {
    const res = await loginApi({
      username: username.value,
      password: password.value
    })
    
    userStore.setToken(res.data.token)
    
    const userRes = await getCurrentUserApi()
    userStore.userInfo = userRes.data
    
    ElMessage.success('登录成功')
    router.push('/index')
  } catch (err) {
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
<style scoped> 
div{
  background: #debef8;
  min-height: 100vh;
}
</style>