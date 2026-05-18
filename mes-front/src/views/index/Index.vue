<script lang="ts" setup>
import axios from 'axios';
import { useRouter } from 'vue-router'

const router = useRouter()

// 退出登录
const logout = () => {
    axios.post("http://localhost:8080/api/v1/auth/logout").then(() => {
        // 清除本地token
        localStorage.removeItem('token')
        // 跳转到登录页
        router.push({ name: "Login" })
    })
}
</script>

<template>
    <div>
        <h1>首页</h1>
        <button @click="logout">退出登录</button>
    </div>
</template>