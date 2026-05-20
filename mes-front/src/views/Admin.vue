<script lang="ts" setup>
import axios from 'axios';
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const onlineUsers = ref<Record<string, any>>({})  // 改为 Record 类型
const loading = ref(false)

// 获取在线用户列表
const getOnlineUsers = () => {
    const token = localStorage.getItem('token');
    
    if (!token) {
        router.push('/')
        return;
    }
    
    loading.value = true
    axios.get("http://localhost:8080/api/v1/auth/online-users", {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
    .then(res => {
        if (res.data.code === 200) {
            onlineUsers.value = res.data.data || {}
        }
    })
    .catch(err => {
        if (err.response?.status === 401) {
            alert('登录已过期，请重新登录')
            localStorage.removeItem('token')
            router.push('/')
        }
    })
    .finally(() => {
        loading.value = false
    })
}

// 踢用户下线
const kickUser = (userId: string) => {
    const token = localStorage.getItem('token');
    if (confirm(`确定要踢掉用户 ${userId} 吗？`)) {
        axios.delete(`http://localhost:8080/api/v1/auth/kick/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(() => {
            alert(`用户 ${userId} 已被踢下线`)
            getOnlineUsers()
        })
    }
}

// 返回首页
const goHome = () => {
    router.push('/index')
}

// 退出管理员页面
const logout = () => {
    axios.post("http://localhost:8080/api/v1/auth/logout")
        .then(() => {
            localStorage.removeItem('token')
            router.push('/')
        })
        .catch(() => {
            localStorage.removeItem('token')
            router.push('/')
        })
}

onMounted(() => {
    const token = localStorage.getItem('token');
    if (!token) {
        router.push('/')
        return
    }
    getOnlineUsers()
})
</script>

<template>
    <div>
        <h1>管理员后台 - 在线用户管理</h1>
        <button @click="getOnlineUsers" :disabled="loading">刷新</button>
        <button @click="goHome" style="margin-left: 10px;">返回首页</button>
        <button @click="logout" style="margin-left: 10px; background-color: #999; color: white;">退出登录</button>
        
        <div v-if="loading">加载中...</div>
        <div v-else-if="Object.keys(onlineUsers).length === 0">
            暂无在线用户
        </div>
        <table v-else border="1" cellpadding="10" style="margin-top: 20px; border-collapse: collapse;">
            <thead>
                <tr>
                    <th>用户ID</th>
                    <th>在线设备数</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(tokens, userId) in onlineUsers" :key="String(userId)">
                    <td>{{ userId }}</td>
                    <td>{{ Array.isArray(tokens) ? tokens.length : 0 }}</td>
                    <td>
                        <button @click="kickUser(String(userId))" style="background-color: #ff4444; color: white;">
                            强制下线
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>