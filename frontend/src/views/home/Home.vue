<template>
  <div class="home-page">
    <el-card class="welcome-card">
      <h2>欢迎使用慢性病用药智能提醒系统</h2>
      <p>本系统帮助您管理慢性病用药，提供智能提醒和用药依从性分析。</p>
    </el-card>
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">0</div>
          <div class="stat-label">病历数量</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">0</div>
          <div class="stat-label">用药计划</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">0%</div>
          <div class="stat-label">依从率</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-number">0</div>
          <div class="stat-label">家庭成员</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.home-page {
  .welcome-card {
    margin-bottom: 20px;
    text-align: center;
    
    h2 {
      margin-bottom: 10px;
    }
    
    p {
      color: #666;
    }
  }
  
  .stats-row {
    .stat-card {
      text-align: center;
      
      .stat-number {
        font-size: 32px;
        font-weight: bold;
        color: #409eff;
      }
      
      .stat-label {
        margin-top: 10px;
        color: #666;
      }
    }
  }
}
</style>