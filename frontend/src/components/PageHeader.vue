<template>
  <div class="page-header">
    <div class="header-content">
      <div class="header-title-section">
        <div class="title-icon" :style="{ background: iconBg }">
          <slot name="icon">
            <el-icon :size="28"><component :is="iconComponent" /></el-icon>
          </slot>
        </div>
        <div class="title-text">
          <h1 class="page-title">{{ title }}</h1>
          <p class="page-subtitle">{{ subtitle }}</p>
        </div>
      </div>
      <div class="header-actions">
        <slot name="actions"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  icon: {
    type: Object,
    default: null
  },
  iconBg: {
    type: String,
    default: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)'
  }
})

const iconComponent = defineModel('icon', { default: null })
</script>

<style scoped>
.page-header {
  padding: 24px 32px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a855f7 100%);
  border-radius: 0 0 24px 24px;
  box-shadow: 0 10px 40px rgba(99, 102, 241, 0.3);
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.title-icon {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 35px rgba(0, 0, 0, 0.3);
  }
}

.title-text {
  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: white;
    margin: 0;
    letter-spacing: -0.5px;
  }
  
  .page-subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin: 6px 0 0 0;
  }
}

.header-actions {
  display: flex;
  gap: 12px;
}
</style>