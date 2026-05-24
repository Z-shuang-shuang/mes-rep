<script lang="ts" setup>
import axios from 'axios';
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const onlineUsers = ref<Record<string, string[]>>({})  // 改为 string[] 类型
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
            // 后端返回的是 Map<String, Set<String>>
            onlineUsers.value = res.data.data || {}
            console.log('在线用户:', onlineUsers.value)
        }
    })
    .catch(err => {
        if (err.response?.status === 401) {
            alert('登录已过期，请重新登录')
            localStorage.removeItem('token')
            router.push('/')
        } else if (err.response?.status === 403) {
            alert('权限不足，只有管理员可以访问')
            router.push('/index')
        }
    })
    .finally(() => {
        loading.value = false
    })
}

// 踢用户下线
const kickUser = (userId: string) => {
    const token = localStorage.getItem('token');
    if (confirm(`确定要踢掉用户 ${userId} 吗？该用户的所有设备都将下线`)) {
        axios.delete(`http://localhost:8080/api/v1/auth/kick/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(() => {
            alert(`用户 ${userId} 已被踢下线`)
            getOnlineUsers() // 刷新列表
        })
        .catch(err => {
            if (err.response?.status === 403) {
                alert('权限不足，无法踢用户下线')
            } else {
                alert('操作失败：' + (err.response?.data?.message || '未知错误'))
            }
        })
    }
}

// 返回首页
const goHome = () => {
    router.push('/index')
}

// 退出登录
const logout = () => {
    axios.post("http://localhost:8080/api/v1/auth/logout", {}, {
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
    <div style="padding: 20px;">
        <h1>管理员后台 - 在线用户管理</h1>
        <div style="margin-bottom: 20px;">
            <button @click="getOnlineUsers" :disabled="loading" style="margin-right: 10px;">
                {{ loading ? "刷新中..." : "刷新" }}
            </button>
            <button @click="goHome" style="margin-right: 10px;">返回首页</button>
            <button @click="logout" style="background-color: #999; color: white;">退出登录</button>
        </div>
        
        <div v-if="loading">加载中...</div>
        <div v-else-if="Object.keys(onlineUsers).length === 0" style="color: #999;">
            暂无在线用户
        </div>
        <table v-else border="1" cellpadding="10" style="margin-top: 20px; border-collapse: collapse; width: 100%;">
            <thead>
                <tr style="background-color: #f5f5f5;">
                    <th>用户ID</th>
                    <th>在线设备数</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(tokens, userId) in onlineUsers" :key="String(userId)">
                    <td>{{ userId }}</td>
                    <td>{{ tokens.length }} 个设备</td>
                    <td>
                        <button @click="kickUser(String(userId))" style="background-color: #ff4444; color: white; padding: 5px 10px;">
                            强制下线所有设备
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
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

button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

table {
    width: 100%;
    border-collapse: collapse;
}

th, td {
    padding: 12px;
    text-align: center;
    border: 1px solid #ddd;
}

tr:hover {
    background-color: #f9f9f9;
}
</style>