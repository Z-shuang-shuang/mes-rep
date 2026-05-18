<script lang="ts" setup>
import router from '@/router';
import axios from 'axios';
import {ref} from 'vue'

let username = ref("")
let password = ref("")
let loading = ref(false)  // 只加这一个

axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

function submit(): void {
  if (loading.value) return;  // 防重复提交
  loading.value = true;

  axios.post("http://localhost:8080/api/v1/auth/login", {
    username: username.value,
    password: password.value
  }).then(res => {
    console.log(res.data)
    if (res.data.code === 200) {
      localStorage.setItem("token", res.data.data.token)
      router.push({name: "Index"})
    }
  }).finally(() => {
    loading.value = false;  // 重置loading
  })
}
</script>

<template>
  <form @submit.prevent="submit">
    <input v-model="username" placeholder="请输入用户名" />
    <input v-model="password" type="password" placeholder="请输入密码" />
    <button type="submit" :disabled="loading">
      {{ loading ? "登录中..." : "提交" }}
    </button>
    <button type="reset">重置</button>
  </form>
</template>


<style scoped>

</style>