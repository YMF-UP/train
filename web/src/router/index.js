import { createRouter, createWebHistory } from 'vue-router' // 修改为 createWebHistory
import HomeView from '../views/HomeView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/about',
    name: 'about',
    component: () => import('../views/AboutView.vue')
  },
  {
    path: '/login',
    component: () => import('../views/login.vue') // 确保文件名大小写正确
  }
]

const router = createRouter({
  history: createWebHistory(), // 修改为 createWebHistory
  routes
})

export default router
