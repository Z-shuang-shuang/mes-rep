<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { loginApi, getCurrentUserApi } from '@/api'  // ✅ 从 api 层导入
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const password = ref('')
const loading = ref(false)

const login = async () => {
  if (loading.value) return
  loading.value = true
  
  try {
    // ✅ 调用封装好的 API，不直接写路径
    const res = await loginApi({
      username: username.value,
      password: password.value
    })
    
    // 保存 token
    userStore.setToken(res.data.token)
    
    // 获取用户信息（也可以封装到 store 里）
    const userRes = await getCurrentUserApi()
    userStore.userInfo = userRes.data
    
    ElMessage.success('登录成功')
    router.push('/index')
  } catch (err) {
    console.error('登录失败:', err)
  } finally {
    loading.value = false
  }
}
</script>