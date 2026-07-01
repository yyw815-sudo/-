import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '管理员登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/user/ForgotPassword.vue'),
    meta: { title: '重置密码' }
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
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/admin/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { title: '管理后台', requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据概览', requiresAdmin: true }
      },
      {
        path: 'system/statistics',
        name: 'DataStatistics',
        component: () => import('@/views/admin/DataStatistics.vue'),
        meta: { title: '数据统计', requiresAdmin: true }
      },
      {
        path: 'system/announcement',
        name: 'SystemAnnouncement',
        component: () => import('@/views/admin/SystemAnnouncement.vue'),
        meta: { title: '系统公告', requiresAdmin: true }
      },
      {
        path: 'system/ai-config',
        name: 'AIConfig',
        component: () => import('@/views/admin/AIConfig.vue'),
        meta: { title: 'AI配置', requiresAdmin: true }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/AdminProfile.vue'),
        meta: { title: '个人中心', requiresAdmin: true }
      },
      {
        path: 'reminder-manage',
        name: 'ReminderManage',
        component: () => import('@/views/reminder/Reminder.vue'),
        meta: { title: '提醒管理', requiresAdmin: true }
      },
      {
        path: 'family-manage',
        name: 'FamilyManage',
        component: () => import('@/views/family/Family.vue'),
        meta: { title: '家庭协作', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 慢性病用药智能提醒系统` : '慢性病用药智能提醒系统'
  
  const userStore = useUserStore()
  
  if (to.meta.requiresAdmin) {
    if (!userStore.isLoggedIn || !userStore.isAdmin) {
      next('/admin/login')
    } else {
      next()
    }
  } else if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router