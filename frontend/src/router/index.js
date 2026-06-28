import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'medical-record',
        name: 'MedicalRecord',
        component: () => import('@/views/medical/MedicalRecord.vue'),
        meta: { title: '病历管理', requiresAuth: true }
      },
      {
        path: 'medication-plan',
        name: 'MedicationPlan',
        component: () => import('@/views/medication/MedicationPlan.vue'),
        meta: { title: '用药计划', requiresAuth: true }
      },
      {
        path: 'reminder',
        name: 'Reminder',
        component: () => import('@/views/reminder/Reminder.vue'),
        meta: { title: '提醒管理', requiresAuth: true }
      },
      {
        path: 'family',
        name: 'Family',
        component: () => import('@/views/family/Family.vue'),
        meta: { title: '家庭协作', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（暂跳过登录）
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 慢性病用药智能提醒系统` : '慢性病用药智能提醒系统'
  // 暂不校验登录，方便开发测试
  next()
})

export default router
