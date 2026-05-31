import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/Index.vue')
    },
    {
      path: '/',
      component: () => import('@/layout/Index.vue'),
      redirect: '/index',
      children: [
        {
          path: '/index',
          name: 'Index',
          component: () => import('@/views/index/Index.vue')
        },
        {
          path: '/user',
          name: 'User',
          component: () => import('@/views/sys/user/Index.vue')
        },
        {
          path: '/admin',
          name: 'Admin',
          component: () => import('@/views/Admin.vue')
        }
      ]
    }
  ]
})

// 路由守卫：检查登录
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/index')
  } else {
    next()
  }
})

export default router