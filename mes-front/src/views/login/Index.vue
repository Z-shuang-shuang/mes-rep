<script lang="ts" setup>
import router from '@/router';
import axios from 'axios';
import {ref} from 'vue'

let username = ref("")
let password = ref("")
let loading = ref(false)

// 添加axios响应拦截器（全局处理token失效）
axios.interceptors.response.use(
  response => {
    // 正常响应直接返回
    return response
  },
  error => {
    // 处理错误响应
    if (error.response) {
      // 如果是401未授权（token过期或被踢下线）
      if (error.response.status === 401) {
        // 清除本地token
        localStorage.removeItem('token')
        // 跳转到登录页
        router.push('/')
        // 提示用户
        alert('登录已过期，请重新登录')
      }
    }
    return Promise.reject(error)
  }
)

// 添加axios请求拦截器（自动添加token）
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

function submit(): void {
  if (loading.value) return;
  loading.value = true;

  axios.post("http://localhost:8080/api/v1/auth/login", {
    username: username.value,
    password: password.value
  }).then(res => {
    console.log(res.data)
    if (res.data.code === 200) {
      localStorage.setItem("token", res.data.data.token)
      router.push({name: "Index"})
    } else {
      alert(res.data.msg || '登录失败')
    }
  }).catch(err => {
    alert('登录失败：' + (err.response?.data?.msg || '网络错误'))
  }).finally(() => {
    loading.value = false;
  })
}
</script>

<template>
  <form @submit.prevent="submit" style="max-width: 300px; margin: 50px auto;">
    <div style="margin-bottom: 10px;">
      <input v-model="username" placeholder="请输入用户名" style="width: 100%; padding: 8px;" />
    </div>
    <div style="margin-bottom: 10px;">
      <input v-model="password" type="password" placeholder="请输入密码" style="width: 100%; padding: 8px;" />
    </div>
    <div>
      <button type="submit" :disabled="loading" style="padding: 8px 16px; margin-right: 10px;">
        {{ loading ? "登录中..." : "提交" }}
      </button>
      <button type="reset" style="padding: 8px 16px;">重置</button>
    </div>
  </form>
</template>

<style scoped>
input {
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>