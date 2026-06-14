<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menus = [
  { path: '/index', name: '首页' },
  { path: '/user', name: '用户管理' },
  { path: '/admin', name: '在线用户' }
]

const goTo = (path: string) => {
  router.push(path)
}

// ✅ 使用 store 的 logout 方法
const logout = () => {
  userStore.logout()  // store 里会清理 token 和 userInfo
  router.push('/login')
}
</script>
<template>
  <div style="display: flex; height: 100vh;">
    <!-- 左侧菜单 -->
    <div style="width: 200px; background: #333; color: #fff; padding: 20px;">
      <div v-for="menu in menus" :key="menu.path" 
           @click="goTo(menu.path)"
           :style="{ 
             padding: '10px', 
             marginBottom: '10px', 
             cursor: 'pointer',
             background: route.path === menu.path ? '#555' : 'transparent',
             borderRadius: '4px'
           }">
        {{ menu.name }}
      </div>
      <div @click="logout" style="padding: 10px; margin-top: 50px; cursor: pointer; background: #d9534f; border-radius: 4px; text-align: center;">
        退出登录
      </div>
    </div>

    <!-- 右侧内容 -->
    <div style="flex: 1; padding: 20px; background: #f5f5f5;">
      <router-view />
    </div>
  </div>
</template>