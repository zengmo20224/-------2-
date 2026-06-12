<template>
  <view class="pc-hero-card">
    <view class="pc-hero-card__content">
      <text class="pc-hero-card__title">{{ title }}</text>
      <text v-if="subtitle" class="pc-hero-card__subtitle">{{ subtitle }}</text>
      <slot name="action" />
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<style scoped>
.pc-hero-card {
  background: linear-gradient(135deg, var(--pc-user-primary), var(--pc-user-dark));
  border-radius: var(--pc-radius-card-lg);
  padding: 28px 24px;
  color: #fff;
}

.pc-hero-card__content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pc-hero-card__title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}

.pc-hero-card__subtitle {
  font-size: var(--pc-font-body);
  color: rgba(255, 255, 255, 0.85);
}
</style>
