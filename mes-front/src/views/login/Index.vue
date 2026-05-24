<script lang="ts" setup>
import router from '@/router';
import axios from 'axios';
import {ref} from 'vue'

let username = ref("")
let password = ref("")
let loading = ref(false)

// 添加axios拦截器（统一处理token）
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

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
      alert(res.data.message || '登录失败')
    }
  }).catch(err => {
    alert('登录失败：' + (err.response?.data?.message || '网络错误'))
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