<template>
  <div class="pc-blocked-feature">
    <el-icon class="pc-blocked-feature__icon" :size="40"><Lock /></el-icon>
    <h3 class="pc-blocked-feature__title">功能暂未开放</h3>
    <p class="pc-blocked-feature__reason">{{ reason }}</p>
  </div>
</template>

<script setup lang="ts">
import { Lock } from '@element-plus/icons-vue'

defineProps<{ reason: string }>()
</script>

<style scoped>
.pc-blocked-feature {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 160px;
  padding: var(--pc-spacing-xl);
  gap: var(--pc-spacing-sm);
  background: var(--pc-primary-soft);
  border-radius: var(--pc-radius);
  border: 1px solid var(--pc-line);
}

.pc-blocked-feature__icon {
  color: var(--pc-muted);
}

.pc-blocked-feature__title {
  margin: 0;
  font-size: var(--pc-font-size-lg);
  color: var(--pc-ink);
  font-weight: 600;
}

.pc-blocked-feature__reason {
  margin: 0;
  color: var(--pc-muted);
  font-size: var(--pc-font-size-base);
  text-align: center;
  max-width: 400px;
}
</style>
