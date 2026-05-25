<script lang="ts" setup>
import axios from 'axios';
import { useRouter } from 'vue-router'
import { onMounted, ref } from 'vue'

const router = useRouter()
const userId = ref('')
const userRoles = ref<string[]>([])
const userPermissions = ref<string[]>([])

// 退出登录
const logout = () => {
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
    })
}

// 强制下线（踢自己）
const kickSelf = () => {
    if (confirm('确定要强制下线吗？下次需要重新登录')) {
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
            userRoles.value = res.data.data.roles || []
            userPermissions.value = res.data.data.permissions || []
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
        <p>当前用户ID: {{ userId }}</p>
        <p>角色: {{ userRoles.join(', ') || '无' }}</p>
        <p>权限: {{ userPermissions.join(', ') || '无' }}</p>
        
        <div style="margin-top: 20px;">
            <button @click="logout" style="margin-right: 10px;">退出登录</button>
            <button @click="kickSelf" style="margin-right: 10px; background-color: #ff4444; color: white;">
                强制下线
            </button>
            <button v-if="userPermissions.includes('user:delete')" 
                    @click="goToAdmin" 
                    style="background-color: #2196f3; color: white;">
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
</style>