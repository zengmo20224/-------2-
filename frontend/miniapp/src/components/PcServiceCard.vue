<template>
  <view class="pc-service-card" @tap="$emit('tap')">
    <view class="pc-service-card__header">
      <text class="pc-service-card__name">{{ name }}</text>
      <PcStatusTag :label="modeLabel" type="primary" />
    </view>
    <view class="pc-service-card__info">
      <text class="pc-service-card__duration">{{ durationText }}</text>
      <text class="pc-service-card__price">{{ priceText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PcStatusTag from './PcStatusTag.vue'
import { formatDuration, formatYuan } from '@/utils/format'

const props = defineProps<{
  name: string
  mode: string
  durationMinutes?: number
  priceMin?: number
}>()

defineEmits<{
  (e: 'tap'): void
}>()

const modeLabel = computed(() => {
  const map: Record<string, string> = { STORE: '到店', HOME: '上门', BOTH: '到店/上门' }
  return map[props.mode] ?? props.mode
})

const durationText = computed(() => formatDuration(props.durationMinutes))
const priceText = computed(() => props.priceMin != null ? `${formatYuan(props.priceMin)}起` : '')
</script>

<style scoped>
.pc-service-card {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 16px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.pc-service-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.pc-service-card__name {
  font-size: var(--pc-font-card-title);
  font-weight: 600;
  color: var(--pc-user-ink);
}

.pc-service-card__info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pc-service-card__duration {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
}

.pc-service-card__price {
  font-size: var(--pc-font-body);
  color: var(--pc-user-accent);
  font-weight: 600;
}
</style>
