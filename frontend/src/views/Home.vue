<template>
  <div class="home-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <div class="logo">
          <h3>用药管理系统</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-sub-menu index="/user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户中心</span>
            </template>
            <el-menu-item index="/user">个人信息</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/medication">
            <template #title>
              <el-icon><Medicine /></el-icon>
              <span>用药管理</span>
            </template>
            <el-menu-item index="/medical-records">电子病历</el-menu-item>
            <el-menu-item index="/medication-plans">用药计划</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/reminder">
            <template #title>
              <el-icon><Bell /></el-icon>
              <span>提醒管理</span>
            </template>
            <el-menu-item index="/reminders">提醒设置</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/family">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>家庭协作</span>
            </template>
            <el-menu-item index="/family">家属管理</el-menu-item>
          </el-sub-menu>

          <el-menu-item v-if="isAdmin" index="/admin">
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部栏 -->
        <el-header>
          <div class="header-content">
            <span class="page-title">{{ pageTitle }}</span>
            <div class="user-info">
              <span>欢迎，{{ username }}</span>
              <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
            </div>
          </div>
        </el-header>

        <!-- 内容区 -->
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { HomeFilled, User, Medicine, Bell, UserFilled, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const username = ref('')
const isAdmin = ref(false)

const activeMenu = computed(() => route.path)

const pageTitle = computed(() => {
  const titles = {
    '/home': '首页',
    '/user': '个人信息',
    '/medical-records': '电子病历',
    '/medication-plans': '用药计划',
    '/reminders': '提醒设置',
    '/family': '家属管理',
    '/admin': '系统管理'
  }
  return titles[route.path] || '首页'
})

onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  username.value = userInfo.username || '用户'
  isAdmin.value = userInfo.role === 'admin'
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  })
}
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b3a4a;
}

.logo h3 {
  margin: 0;
  color: #fff;
  font-size: 16px;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
