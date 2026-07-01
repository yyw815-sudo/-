<template>
  <div class="content-card" :class="{ 'card-hover': hoverable }">
    <div v-if="$slots.header" class="card-header">
      <slot name="header"></slot>
    </div>
    <div class="card-body">
      <slot></slot>
    </div>
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  hoverable: {
    type: Boolean,
    default: true
  }
})
</script>

<style scoped>
.content-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(226, 232, 240, 0.6);
  transition: all 0.3s ease;
  
  &.card-hover:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
    transform: translateY(-1px);
  }
}

.card-header {
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  :deep(.el-card__header) {
    padding: 0;
    border: none;
    margin: 0;
  }
}

.card-body {
  min-height: 60px;
}

.card-footer {
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid rgba(226, 232, 240, 0.6);
}
</style>