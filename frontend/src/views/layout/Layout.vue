<template>
  <el-container class="layout-container">
    <el-header class="layout-header">
      <div class="logo">慢性病用药智能提醒系统</div>
      <div class="user-info">
        <el-dropdown>
          <span class="user-name">
            <el-icon><User /></el-icon>
            {{ userStore.userInfo.realname || userStore.userInfo.userName }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="layout-aside">
        <el-menu :default-active="activeMenu" router>
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/medical-record">
            <el-icon><Document /></el-icon>
            <span>病历管理</span>
          </el-menu-item>
          <el-menu-item index="/medication-plan">
            <el-icon><Calendar /></el-icon>
            <span>用药计划</span>
          </el-menu-item>
          <el-menu-item index="/reminder">
            <el-icon><Bell /></el-icon>
            <span>提醒管理</span>
          </el-menu-item>
          <el-menu-item index="/family">
            <el-icon><UserFilled /></el-icon>
            <span>家庭协作</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  
  .layout-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    
    .logo {
      font-size: 18px;
      color: white;
      font-weight: bold;
    }
    
    .user-info {
      .user-name {
        color: white;
        display: flex;
        align-items: center;
        gap: 5px;
        cursor: pointer;
      }
    }
  }
  
  .layout-aside {
    background: #fff;
    border-right: 1px solid #e6e6e6;
    position: sticky;
    top: 0;
    align-self: flex-start;
    height: 100vh;
  }
  
  .layout-main {
    background: #f5f5f5;
    padding: 20px;
    overflow: visible;
  }
}
</style>