<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const user = ref({ username: '', userId: '' })

onMounted(async () => {
  try {
    const res = await axios.get('/api/v1/auth/current-user')
    if (res.data.code === 200) {
      user.value = res.data.data
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