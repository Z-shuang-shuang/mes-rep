<script lang="ts" setup>
import axios from 'axios';
import { useRouter } from 'vue-router'
import { onMounted, ref } from 'vue'

const router = useRouter()
const userId = ref('')
const username = ref('')
const loading = ref(false)

// 退出登录
const logout = () => {
    if (!confirm('确定要退出登录吗？')) return;
    
    loading.value = true
    axios.post("http://localhost:8080/api/v1/auth/logout", {}, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    }).then(() => {
        localStorage.removeItem('token')
        router.push('/')
    }).catch(() => {
        localStorage.removeItem('token')
        router.push('/')
    }).finally(() => {
        loading.value = false
    })
}

// 强制下线（踢自己）
const kickSelf = () => {
    if (confirm('确定要强制下线吗？下次需要重新登录')) {
        loading.value = true
        axios.delete("http://localhost:8080/api/v1/auth/kick-self", {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        })
        .then(() => {
            localStorage.removeItem('token')
            router.push('/')
        })
        .catch(() => {
            localStorage.removeItem('token')
            router.push('/')
        })
        .finally(() => {
            loading.value = false
        })
    }
}

// 获取当前用户信息
const getCurrentUser = () => {
    axios.get("http://localhost:8080/api/v1/auth/current-user", {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    }).then(res => {
        if (res.data.code === 200 && res.data.data) {
            userId.value = res.data.data.userId || ''
            username.value = res.data.data.username || ''
        }
    }).catch(err => {
        if (err.response?.status === 401) {
            localStorage.removeItem('token')
            router.push('/')
        }
    })
}

// 进入管理后台
const goToAdmin = () => {
    router.push('/admin')
}

onMounted(() => {
    const token = localStorage.getItem('token');
    if (!token) {
        router.push('/')
        return
    }
    getCurrentUser()
})
</script>

<template>
    <div style="padding: 20px;">
        <h1>首页</h1>
        <p>用户名: {{ username }}</p>
        <p>用户ID: {{ userId }}</p>
        
        <div style="margin-top: 20px;">
            <button @click="logout" :disabled="loading" style="margin-right: 10px;">
                {{ loading ? "处理中..." : "退出登录" }}
            </button>
            <button @click="kickSelf" :disabled="loading" style="margin-right: 10px; background-color: #ff4444; color: white;">
                强制下线
            </button>
            <button @click="goToAdmin" style="background-color: #2196f3; color: white;">
                在线用户管理
            </button>
        </div>
    </div>
</template>

<style scoped>
button {
    padding: 8px 16px;
    cursor: pointer;
    border: none;
    border-radius: 4px;
    font-size: 14px;
}

button:hover {
    opacity: 0.8;
}

button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}
</style>