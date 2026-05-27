<script lang="ts" setup>
import axios from 'axios';
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const onlineUsers = ref<Array<{userId: string, username: string, tokenCount: number, tokens: string[]}>>([])
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
        if (res.data.code === 200 && res.data.data) {
            // 将对象转换为数组格式
            const data = res.data.data
            onlineUsers.value = Object.keys(data).map(userId => ({
                userId: userId,
                username: `用户${userId}`, // 如果后端返回了用户名可以替换
                tokenCount: data[userId].length,
                tokens: data[userId]
            }))
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
        } else {
            alert('获取在线用户失败：' + (err.response?.data?.msg || '未知错误'))
        }
    })
    .finally(() => {
        loading.value = false
    })
}

// 踢用户下线
const kickUser = (userId: string) => {
    if (confirm(`确定要踢掉用户 ${userId} 吗？`)) {
        const token = localStorage.getItem('token');
        axios.delete(`http://localhost:8080/api/v1/auth/kick/${userId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(res => {
            if (res.data.code === 200) {
                alert(`用户 ${userId} 已被踢下线`)
                getOnlineUsers()
            } else {
                alert(res.data.msg || '操作失败')
            }
        })
        .catch(err => {
            if (err.response?.status === 403) {
                alert('权限不足，只有管理员可以操作')
            } else if (err.response?.status === 401) {
                alert('登录已过期')
                localStorage.removeItem('token')
                router.push('/')
            } else {
                alert('操作失败：' + (err.response?.data?.msg || '网络错误'))
            }
        })
    }
}

// 返回首页
const goHome = () => {
    router.push('/index')
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
        <h1>在线用户管理</h1>
        <div style="margin-bottom: 20px;">
            <button @click="getOnlineUsers" :disabled="loading" style="margin-right: 10px;">
                {{ loading ? "刷新中..." : "刷新" }}
            </button>
            <button @click="goHome">返回首页</button>
        </div>
        
        <div v-if="loading">加载中...</div>
        <div v-else-if="onlineUsers.length === 0" style="color: #999; text-align: center; padding: 40px;">
            暂无在线用户
        </div>
        <table v-else border="1" cellpadding="10" style="margin-top: 20px; border-collapse: collapse; width: 100%;">
            <thead>
                <tr style="background-color: #f5f5f5;">
                    <th>用户ID</th>
                    <th>用户名</th>
                    <th>在线设备数</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="user in onlineUsers" :key="user.userId">
                    <td style="text-align: center;">{{ user.userId }}</td>
                    <td style="text-align: center;">{{ user.username }}</td>
                    <td style="text-align: center;">{{ user.tokenCount }} 个设备</td>
                    <td style="text-align: center;">
                        <button @click="kickUser(user.userId)" style="background-color: #ff4444; color: white; padding: 4px 12px;">
                            踢下线
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<style scoped>
button {
    padding: 6px 12px;
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

table {
    width: 100%;
    border-collapse: collapse;
}

th, td {
    padding: 10px;
    border: 1px solid #ddd;
}

tr:hover {
    background-color: #f9f9f9;
}
</style>