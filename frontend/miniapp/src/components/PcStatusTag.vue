<template>
  <text class="pc-status-tag" :style="tagStyle">{{ label }}</text>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { TAG_STYLES } from '@/types/status'

const props = defineProps<{
  label: string
  type?: string
}>()

const tagStyle = computed(() => {
  const typeMap: Record<string, string> = {
    primary: 'pc-tag-primary',
    success: 'pc-tag-success',
    warning: 'pc-tag-warning',
    danger: 'pc-tag-danger',
    info: 'pc-tag-info',
  }
  const tagClass = typeMap[props.type ?? 'info'] ?? 'pc-tag-info'
  const style = TAG_STYLES[tagClass] ?? TAG_STYLES['pc-tag-info']
  return {
    backgroundColor: style.bg,
    color: style.color,
  }
})
</script>

<style scoped>
.pc-status-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: var(--pc-font-caption);
  font-weight: 500;
}
</style>
