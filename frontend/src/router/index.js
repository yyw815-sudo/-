import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue')
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    name: 'UserCenter',
    component: () => import('@/views/user/UserCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/medical-records',
    name: 'MedicalRecords',
    component: () => import('@/views/medication/MedicalRecords.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/medication-plans',
    name: 'MedicationPlans',
    component: () => import('@/views/medication/MedicationPlans.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reminders',
    name: 'Reminders',
    component: () => import('@/views/reminder/Reminders.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/family',
    name: 'Family',
    component: () => import('@/views/family/Family.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
