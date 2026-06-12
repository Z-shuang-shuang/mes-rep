<!-- 首页组件 -->
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { http } from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const user = ref({ username: '', userId: '' })

onMounted(async () => {
  try {
    // 使用新的 http 实例
    const res = await http.get('/v1/auth/current-user')
    user.value = res.data
  } catch (e) {
    console.error(e)
  }
})
</script>

<template>
  <div>
    <h1>首页</h1>
    <p>用户名：{{ user.username }}</p>
    <p>用户ID：{{ user.userId }}</p>
  </div>
</template>