<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const users = ref<any[]>([])
const loading = ref(false)

const getUsers = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/v1/auth/online-users')
    if (res.data.code === 200) {
      const data = res.data.data
      users.value = Object.keys(data).map(id => ({
        id,
        tokenCount: data[id].length
      }))
    }
  } catch (err: any) {
    if (err.response?.status === 403) {
      alert('权限不足')
    }
  } finally {
    loading.value = false
  }
}

const kick = async (userId: string) => {
  if (!confirm(`确定踢掉用户 ${userId} 吗？`)) return
  try {
    await axios.delete(`/api/v1/auth/kick/${userId}`)
    alert('已踢下线')
    getUsers()
  } catch (err: any) {
    alert(err.response?.data?.msg || '操作失败')
  }
}

onMounted(() => {
  getUsers()
})
</script>

<template>
  <div>
    <h1>在线用户管理</h1>
    <button @click="getUsers" style="margin-bottom: 20px; padding: 5px 10px;">刷新</button>
    
    <div v-if="loading">加载中...</div>
    <div v-else-if="users.length === 0">暂无在线用户</div>
    <table v-else border="1" cellpadding="8" style="border-collapse: collapse; width: 100%;">
      <thead>
        <tr style="background: #f0f0f0;">
          <th>用户ID</th>
          <th>在线设备数</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.tokenCount }}</td>
          <td><button @click="kick(u.id)" style="background: #ff4444; color: white; border: none; padding: 4px 8px; cursor: pointer;">踢下线</button></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>