<!-- views/index/Index.vue -->
<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

onMounted(async () => {
  // 如果 store 中没有用户信息，则获取
  if (!userInfo.value) {
    await userStore.getUserInfo()
  }
})
</script>

<template>
  <div>
    <h1>首页</h1>
    <div v-if="userInfo">
      <p>用户名：{{ userInfo.username }}</p>
      <p>用户ID：{{ userInfo.userId }}</p>
      <p>角色：{{ userInfo.roles?.join(', ') }}</p>
      <p>权限：{{ userInfo.permissions?.join(', ') }}</p>
    </div>
    <div v-else>
      <p>加载中...</p>
    </div>
  </div>
</template>