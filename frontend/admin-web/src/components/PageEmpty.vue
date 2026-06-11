<template>
  <div class="pc-page-empty">
    <el-icon class="pc-page-empty__icon" :size="48"><Box /></el-icon>
    <p class="pc-page-empty__message">{{ message }}</p>
    <div v-if="$slots.action" class="pc-page-empty__action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Box } from '@element-plus/icons-vue'

withDefaults(defineProps<{ message?: string }>(), {
  message: '暂无数据',
})
</script>

<style scoped>
.pc-page-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: var(--pc-spacing-md);
}

.pc-page-empty__icon {
  color: var(--pc-line);
}

.pc-page-empty__message {
  color: var(--pc-muted);
  font-size: var(--pc-font-size-base);
  margin: 0;
}

.pc-page-empty__action {
  margin-top: var(--pc-spacing-sm);
}
</style>
