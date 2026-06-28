<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">
        <span class="logo-icon">🏥</span>
        <span class="logo-text">管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        background-color="#1f2937"
        text-color="#d1d5db"
        active-text-color="#60a5fa"
        @select="handleMenuSelect"
      >
        <el-menu-item index="dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="user-manage">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="reminder-manage">
          <el-icon><Bell /></el-icon>
          <span>提醒管理</span>
        </el-menu-item>
        <el-menu-item index="family-manage">
          <el-icon><Avatar /></el-icon>
          <span>家庭协作</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-info">
              <el-icon class="admin-avatar"><UserFilled /></el-icon>
              <span class="admin-name">{{ adminName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataLine,
  User,
  Bell,
  Avatar,
  UserFilled,
  ArrowDown
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const adminName = computed(() => userStore.userName || '管理员')

const activeMenu = computed(() => {
  const path = route.path
  if (path.includes('user-manage')) return 'user-manage'
  if (path.includes('reminder-manage')) return 'reminder-manage'
  if (path.includes('family-manage')) return 'family-manage'
  return 'dashboard'
})

const currentPageTitle = computed(() => {
  const titles = {
    'dashboard': '数据概览',
    'user-manage': '用户管理',
    'reminder-manage': '提醒管理',
    'family-manage': '家庭协作'
  }
  return titles[activeMenu.value] || '数据概览'
})

function handleMenuSelect(index) {
  if (index === 'dashboard') {
    router.push('/admin/dashboard')
  } else if (index === 'user-manage') {
    router.push('/admin/user-manage')
  } else if (index === 'reminder-manage') {
    router.push('/admin/reminder-manage')
  } else if (index === 'family-manage') {
    router.push('/admin/family-manage')
  }
}

function handleCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/admin/profile')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-aside {
  background-color: #1f2937;
  overflow: hidden;
}

.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #374151;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.admin-menu {
  border-right: none;
  margin-top: 10px;
}

.admin-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

.admin-menu :deep(.el-menu-item:hover) {
  background-color: #374151;
}

.admin-header {
  background-color: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  flex: 1;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #374151;
}

.admin-avatar {
  font-size: 20px;
  color: #6b7280;
}

.admin-name {
  font-size: 14px;
}

.admin-main {
  background-color: #f3f4f6;
  padding: 20px;
}
</style>
