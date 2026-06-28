<template>
  <div class="admin-dashboard">
    <h2 class="page-title">数据概览</h2>

    <div class="stats-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon user-icon">👤</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon plan-icon">💊</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalPlans }}</div>
            <div class="stat-label">用药计划</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon reminder-icon">🔔</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalReminders }}</div>
            <div class="stat-label">提醒配置</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon record-icon">📋</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalRecords }}</div>
            <div class="stat-label">服药记录</div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="dashboard-content">
      <el-card class="content-card" shadow="hover">
        <template #header>
          <span>最近注册用户</span>
        </template>
        <el-table :data="recentUsers" stripe style="width: 100%">
          <el-table-column prop="userId" label="ID" width="80" />
          <el-table-column prop="userName" label="用户名" width="140" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="140" />
          <el-table-column prop="createTime" label="注册时间">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="content-card" shadow="hover">
        <template #header>
          <span>系统信息</span>
        </template>
        <div class="system-info">
          <div class="info-item">
            <span class="info-label">系统名称</span>
            <span class="info-value">慢性病用药智能提醒系统</span>
          </div>
          <div class="info-item">
            <span class="info-label">当前版本</span>
            <span class="info-value">v1.0.0</span>
          </div>
          <div class="info-item">
            <span class="info-label">技术栈</span>
            <span class="info-value">Spring Boot + Vue 3</span>
          </div>
          <div class="info-item">
            <span class="info-label">数据库</span>
            <span class="info-value">MySQL 8.0</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList } from '@/api/admin'

const stats = reactive({
  totalUsers: 0,
  totalPlans: 0,
  totalReminders: 0,
  totalRecords: 0
})

const recentUsers = ref([])

function fetchStats() {
  getUserList({ pageNum: 1, pageSize: 5 }).then(res => {
    if (res.code === 200) {
      stats.totalUsers = res.data.total
      recentUsers.value = res.data.list
    }
  })
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 0;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.user-icon {
  background: #dbeafe;
}

.plan-icon {
  background: #dcfce7;
}

.reminder-icon {
  background: #fef3c7;
}

.record-icon {
  background: #fce7f3;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.content-card {
  border-radius: 12px;
}

.system-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.info-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-label {
  color: #6b7280;
  font-size: 14px;
}

.info-value {
  color: #1f2937;
  font-size: 14px;
  font-weight: 500;
}
</style>
