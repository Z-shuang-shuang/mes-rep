import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/login/Index.vue'
import Index from '@/views/index/Index.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path:"/",
      name:"Login",
      component:Login
    },{
      path:"/index",
      name:"Index",
      component:Index
    }
  ],
})

export default router
