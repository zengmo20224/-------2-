<template>
  <scroll-view scroll-x class="pc-date-selector">
    <view class="pc-date-selector__list">
      <view
        v-for="(day, idx) in days"
        :key="idx"
        class="pc-date-selector__item"
        :class="{ 'pc-date-selector__item--active': modelValue === day.value }"
        @tap="$emit('update:modelValue', day.value)"
      >
        <text class="pc-date-selector__weekday">{{ day.weekday }}</text>
        <text class="pc-date-selector__date">{{ day.label }}</text>
      </view>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

defineProps<{
  modelValue?: string
}>()

defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const WEEKDAYS = ['日', '一', '二', '三', '四', '五', '六']

const days = computed(() => {
  const result: { value: string; label: string; weekday: string }[] = []
  const now = new Date()
  for (let i = 0; i < 14; i++) {
    const d = new Date(now)
    d.setDate(d.getDate() + i)
    const month = d.getMonth() + 1
    const day = d.getDate()
    result.push({
      value: `${d.getFullYear()}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`,
      label: `${month}/${day}`,
      weekday: i === 0 ? '今天' : `周${WEEKDAYS[d.getDay()]}`,
    })
  }
  return result
})
</script>

<style scoped>
.pc-date-selector {
  white-space: nowrap;
}

.pc-date-selector__list {
  display: flex;
  gap: 8px;
  padding: 4px 0;
}

.pc-date-selector__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #E2E9E6;
  min-width: 52px;
}

.pc-date-selector__item--active {
  background: #11796F;
  border-color: #11796F;
}

.pc-date-selector__item--active .pc-date-selector__weekday,
.pc-date-selector__item--active .pc-date-selector__date {
  color: #fff;
}

.pc-date-selector__weekday {
  font-size: 11px;
  color: #71817D;
}

.pc-date-selector__date {
  font-size: 14px;
  color: #19322E;
  font-weight: 600;
}
</style>
