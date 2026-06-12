<!-- 用户管理组件（更新为使用新封装） -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref<any[]>([])
const loading = ref(false)

const getUsers = async () => {
  loading.value = true
  try {
    const res = await http.get('/v1/auth/online-users')
    const data = res.data
    users.value = Object.keys(data).map(id => ({
      id,
      tokenCount: data[id].length
    }))
  } catch (err: any) {
    // 错误已统一处理
    console.error('获取用户列表失败', err)
  } finally {
    loading.value = false
  }
}

const kick = async (userId: string) => {
  try {
    await ElMessageBox.confirm(`确定踢掉用户 ${userId} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await http.delete(`/v1/auth/kick/${userId}`)
    ElMessage.success('已踢下线')
    getUsers()
  } catch (err: any) {
    if (err !== 'cancel') {
      console.error('操作失败', err)
    }
  }
}

onMounted(() => {
  getUsers()
})
</script>

<template>
  <div>
    <h1>在线用户管理</h1>
    <el-button @click="getUsers" style="margin-bottom: 20px;">刷新</el-button>
    
    <div v-if="loading">加载中...</div>
    <div v-else-if="users.length === 0">暂无在线用户</div>
    <el-table v-else :data="users" border style="width: 100%">
      <el-table-column prop="id" label="用户ID" />
      <el-table-column prop="tokenCount" label="在线设备数" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="kick(row.id)">
            踢下线
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>