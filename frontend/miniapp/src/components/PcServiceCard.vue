<template>
  <view class="pc-service-card" @tap="$emit('tap')">
    <view class="pc-service-card__image-wrap">
      <image v-if="imageUrl" class="pc-service-card__image" :src="imageUrl" mode="aspectFill" />
      <view v-else class="pc-service-card__image pc-service-card__image--placeholder">
        <text class="pc-service-card__placeholder-text">{{ placeholderText }}</text>
      </view>
    </view>
    <view class="pc-service-card__body">
      <view class="pc-service-card__header">
        <text class="pc-service-card__name">{{ name }}</text>
        <PcStatusTag :label="modeLabel" type="primary" />
      </view>
      <view class="pc-service-card__info">
        <text class="pc-service-card__duration">{{ durationText }}</text>
        <text class="pc-service-card__price">{{ priceText }}</text>
      </view>
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
  price?: number
  imageUrl?: string
}>()

defineEmits<{
  (e: 'tap'): void
}>()

const modeLabel = computed(() => {
  const map: Record<string, string> = { STORE: '到店', HOME: '上门', BOTH: '到店/上门' }
  return map[props.mode] ?? props.mode
})

const durationText = computed(() => formatDuration(props.durationMinutes))
const priceText = computed(() => props.price != null ? `${formatYuan(props.price)}起` : '')

const placeholderText = computed(() => {
  const map: Record<string, string> = { STORE: '到店', HOME: '上门', BOTH: '服务' }
  return map[props.mode] ?? '服务'
})
</script>

<style scoped>
.pc-service-card {
  background: #fff;
  border-radius: var(--pc-radius-card);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.pc-service-card__image-wrap {
  width: 100%;
  height: 140px;
}

.pc-service-card__image {
  width: 100%;
  height: 100%;
}

.pc-service-card__image--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--pc-user-soft);
}

.pc-service-card__placeholder-text {
  font-size: 32px;
  font-weight: 700;
  color: var(--pc-user-primary);
  opacity: 0.3;
}

.pc-service-card__body {
  padding: 14px 16px;
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
