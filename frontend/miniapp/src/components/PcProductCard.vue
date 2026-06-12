<template>
  <view class="pc-product-card" @tap="$emit('tap')">
    <view v-if="imageUrl" class="pc-product-card__image-wrap">
      <image class="pc-product-card__image" :src="imageUrl" mode="aspectFill" />
    </view>
    <view class="pc-product-card__info">
      <text class="pc-product-card__name">{{ name }}</text>
      <text class="pc-product-card__price">{{ priceText }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatYuan } from '@/utils/format'

const props = defineProps<{
  name: string
  price: number
  imageUrl?: string
}>()

defineEmits<{
  (e: 'tap'): void
}>()

const priceText = computed(() => formatYuan(props.price))
</script>

<style scoped>
.pc-product-card {
  background: #fff;
  border-radius: var(--pc-radius-card);
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.pc-product-card__image-wrap {
  width: 100%;
  height: 120px;
}

.pc-product-card__image {
  width: 100%;
  height: 100%;
}

.pc-product-card__info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pc-product-card__name {
  font-size: var(--pc-font-card-title);
  font-weight: 600;
  color: var(--pc-user-ink);
}

.pc-product-card__price {
  font-size: var(--pc-font-body);
  color: var(--pc-user-accent);
  font-weight: 600;
}
</style>
