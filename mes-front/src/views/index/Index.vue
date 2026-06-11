<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'  // 改这里：引入封装好的实例

const user = ref({ username: '', userId: '' })

onMounted(async () => {
  try {
    const res = await request.get('/v1/auth/current-user')
    if (res.code === 200) {
      user.value = res.data
    }
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