<script lang="ts" setup>
import axios from 'axios';
import { useRouter } from 'vue-router'
import { onMounted, ref } from 'vue'

const router = useRouter()
const userId = ref('')
const userPermissions = ref<string[]>([])  // 新增：用户权限
const devices = ref<any[]>([])  // 新增：设备列表
const showDevices = ref(false)  // 新增：是否显示设备列表

// 退出登录（正常退出）
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
const forceLogout = () => {
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
        console.log("当前用户:", res.data)
        if (res.data.code === 200 && res.data.data) {
            userId.value = res.data.data.userId || ''
            userPermissions.value = res.data.data.permissions || []
            // 如果有管理员权限，显示管理员入口
            if (userPermissions.value.includes('user:delete')) {
                console.log('当前用户是管理员')
            }
        }
    }).catch(err => {
        if (err.response?.status === 401) {
            localStorage.removeItem('token')
            router.push('/')
        }
    })
}

// 获取我的设备列表（新增功能）
const getMyDevices = () => {
    axios.get("http://localhost:8080/api/v1/auth/my-devices", {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
    }).then(res => {
        if (res.data.code === 200 && res.data.data) {
            devices.value = res.data.data.devices || []
            showDevices.value = true
        }
    }).catch(err => {
        alert('获取设备列表失败：' + (err.response?.data?.message || '未知错误'))
    })
}

// 删除指定设备
const deleteDevice = (tokenId: string) => {
    if (confirm('确定要下线该设备吗？')) {
        axios.delete(`http://localhost:8080/api/v1/auth/delete-device/${tokenId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        }).then(res => {
            alert(res.data.message || '设备已下线')
            // 如果下线的是当前设备，跳转到登录页
            if (res.data.message?.includes('当前设备已下线')) {
                localStorage.removeItem('token')
                router.push('/')
            } else {
                getMyDevices() // 刷新设备列表
            }
        }).catch(err => {
            alert('操作失败：' + (err.response?.data?.message || '未知错误'))
        })
    }
}

// 关闭设备列表弹窗
const closeDevices = () => {
    showDevices.value = false
}

// 添加管理员入口
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
        <p v-if="userPermissions.includes('admin')">角色: 管理员</p>
        <p v-else>角色: 普通用户</p>
        
        <div style="margin-top: 20px;">
            <button @click="logout" style="margin-right: 10px;">退出登录</button>
            <button @click="forceLogout" style="margin-right: 10px; background-color: #ff4444; color: white;">
                强制下线当前设备
            </button>
            <button @click="getMyDevices" style="margin-right: 10px; background-color: #4caf50; color: white;">
                查看我的设备
            </button>
            <button v-if="userPermissions.includes('user:delete')" 
                    @click="goToAdmin" 
                    style="background-color: #2196f3; color: white;">
                后台管理
            </button>
        </div>

        <!-- 设备列表弹窗 -->
        <div v-if="showDevices" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000;">
            <div style="background: white; padding: 20px; border-radius: 8px; min-width: 500px;">
                <h3>我的在线设备</h3>
                <table style="width: 100%; border-collapse: collapse; margin-top: 10px;">
                    <thead>
                        <tr style="background-color: #f5f5f5;">
                            <th style="padding: 8px;">设备ID</th>
                            <th style="padding: 8px;">剩余有效时间</th>
                            <th style="padding: 8px;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="device in devices" :key="device.tokenId">
                            <td style="padding: 8px;">{{ device.tokenId.substring(0, 20) }}...</td>
                            <td style="padding: 8px;">{{ Math.floor(device.remainingTTL / 3600) }}小时{{ Math.floor((device.remainingTTL % 3600) / 60) }}分钟</td>
                            <td style="padding: 8px;">
                                <button @click="deleteDevice(device.tokenId)" style="background-color: #ff9800; color: white; padding: 4px 8px;">
                                    下线该设备
                                </button>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div style="margin-top: 20px; text-align: center;">
                    <button @click="closeDevices" style="padding: 8px 16px;">关闭</button>
                </div>
            </div>
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