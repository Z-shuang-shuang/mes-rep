<script lang="ts" setup>
import axios from 'axios';
import { useRouter } from 'vue-router'
import { onMounted, ref } from 'vue'

const router = useRouter()
const userId = ref('')

// 退出登录（正常退出）
const logout = () => {
    axios.post("http://localhost:8080/api/v1/auth/logout").then(() => {
        localStorage.removeItem('token')
        router.push('/')  // 改为路径
    }).catch(() => {
        localStorage.removeItem('token')
        router.push('/')  // 改为路径
    })
}

// 强制下线（踢自己）
const forceLogout = () => {
    if (confirm('确定要强制下线吗？下次需要重新登录')) {
        axios.delete("http://localhost:8080/api/v1/auth/kick-self")
            .then(() => {
                localStorage.removeItem('token')
                router.push('/')  // 改为路径
            })
            .catch(() => {
                localStorage.removeItem('token')
                router.push('/')  // 改为路径
            })
    }
}

// 获取当前用户信息
const getCurrentUser = () => {
    axios.get("http://localhost:8080/api/v1/auth/current-user").then(res => {
        console.log("当前用户:", res.data)
        if (res.data.code === 200 && res.data.data) {
            const match = res.data.data.match(/\d+/)
            if (match) userId.value = match[0]
        }
    }).catch(err => {
        if (err.response?.status === 401) {
            localStorage.removeItem('token')
            router.push('/')  // 改为路径
        }
    })
}

// 添加管理员入口
const goToAdmin = () => {
    router.push('/admin')
}

onMounted(() => {
    getCurrentUser()
})
</script>

<template>
    <div>
        <h1>首页</h1>
        <p>当前用户ID: {{ userId }}</p>
        <button @click="logout">退出登录</button>
        <button @click="forceLogout" style="margin-left: 10px; background-color: #ff4444; color: white;">
            强制下线
        </button>
        <button @click="goToAdmin" style="margin-left: 10px; background-color: #2196f3; color: white;">
            后台管理
        </button>
    </div>
</template>