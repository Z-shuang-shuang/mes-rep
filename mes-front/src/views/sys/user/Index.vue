<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOnlineUsersApi, kickUserApi } from '@/api'  // ✅ 从 api 层导入
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref<Array<{ id: string; tokenCount: number }>>([])
const loading = ref(false)

const getUsers = async () => {
  loading.value = true
  try {
    // ✅ 调用封装好的 API
    const res = await getOnlineUsersApi()
    const data = res.data
    users.value = Object.keys(data).map(id => ({
      id,
      tokenCount: data[id].length
    }))
  } catch (err) {
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
    
    // ✅ 调用封装好的 API
    await kickUserApi(userId)
    ElMessage.success('已踢下线')
    getUsers()
  } catch (err) {
    if (err !== 'cancel') {
      console.error('操作失败', err)
    }
  }
}

onMounted(() => {
  getUsers()
})
</script>