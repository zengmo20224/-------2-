<template>
  <div class="pc-page-loading">
    <el-icon class="pc-page-loading__icon" :size="32"><Loading /></el-icon>
    <span class="pc-page-loading__text">{{ text }}</span>
  </div>
</template>

<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'

withDefaults(defineProps<{ text?: string }>(), {
  text: '加载中...',
})
</script>

<style scoped>
.pc-page-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: var(--pc-spacing-md);
}

.pc-page-loading__icon {
  animation: spin 1s linear infinite;
  color: var(--pc-primary);
}

.pc-page-loading__text {
  color: var(--pc-muted);
  font-size: var(--pc-font-size-base);
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
