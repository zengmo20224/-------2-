<template>
  <div class="pc-page-error">
    <el-icon class="pc-page-error__icon" :size="48"><CircleCloseFilled /></el-icon>
    <p class="pc-page-error__message">{{ displayMessage }}</p>
    <el-button type="primary" @click="$emit('retry')">重试</el-button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CircleCloseFilled } from '@element-plus/icons-vue'
import { sanitizeErrorMessage } from '../utils/error-sanitizer'

const props = withDefaults(defineProps<{ message?: string }>(), {
  message: '加载失败',
})

defineEmits<{ retry: [] }>()

const displayMessage = computed(() => sanitizeErrorMessage(props.message))
</script>

<style scoped>
.pc-page-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: var(--pc-spacing-md);
}

.pc-page-error__icon {
  color: var(--pc-danger);
}

.pc-page-error__message {
  color: var(--pc-muted);
  font-size: var(--pc-font-size-base);
  margin: 0;
}
</style>
