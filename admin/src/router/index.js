import { createRouter, createWebHistory } from 'vue-router'



const routes = [


  {
    path: '/',
    component: () => import('../views/main.vue'), // 确保文件名大小写正确
    meta: {
      loginRequire: true
    },
    children:[{
      path: 'welcome',
      component: () => import('../views/main/welcome.vue'),
    },{
      path: 'about',
      component: () => import('../views/main/about.vue'),
    },{
      path: 'station',
      component: () => import('../views/main/station.vue'),
    },{
      path: 'train',
      component: () => import('../views/main/train.vue'),
    },{
      path: 'train-station',
      component: () => import('../views/main/train-station.vue'),
    },{
      path: 'train-carriage',
      component: () => import('../views/main/train-carriage.vue'),
    },{
      path: 'train-seat',
      component: () => import('../views/main/train-seat.vue'),
    },{
      path: 'batch-job',
      component: () => import('../views/main/batch/job.vue'),
    },{
      path: 'daily-train',
      component: () => import('../views/main/daily-train.vue'),
    },{
      path: 'daily-train-station',
      component: () => import('../views/main/daily-train-station.vue'),
    },{
      path: 'daily-train-carriage',
      component: () => import('../views/main/daily-train-carriage.vue'),
    },{
      path: 'daily-train-seat',
      component: () => import('../views/main/daily-train-seat.vue'),
    },{
      path: 'daily-train-ticket',
      component: () => import('../views/main/daily-train-ticket.vue'),
    },{
      path: 'confirm-order',
      component: () => import('../views/main/confirm-order.vue'),
    }
    ]

  },
  {
    path: '',
    redirect:'/welcome'
  }
]

const router = createRouter({
  history: createWebHistory(), // 修改为 createWebHistory
  routes
})



export default router
