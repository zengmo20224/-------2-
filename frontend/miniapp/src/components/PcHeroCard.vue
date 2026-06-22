<template>
  <view class="pc-hero-card">
    <view class="pc-hero-card__eyebrow">
      <text class="pc-hero-card__eyebrow-text">专业护理 · 安心陪伴</text>
    </view>
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
  position: relative;
  overflow: hidden;
  min-height: 174px;
  background:
    radial-gradient(circle at 92% 12%, rgba(255, 218, 138, 0.82) 0 52px, transparent 53px),
    radial-gradient(circle at 84% 62%, rgba(255, 255, 255, 0.16) 0 82px, transparent 83px),
    linear-gradient(135deg, #16877C, #0B3D39);
  background:
    radial-gradient(circle at 92% 12%, rgba(245, 166, 35, 0.42) 0 52px, transparent 53px),
    radial-gradient(circle at 84% 62%, rgba(255, 255, 255, 0.12) 0 82px, transparent 83px),
    linear-gradient(135deg, #16877c, #0C4D48);
  border-radius: 26px;
  padding: 22px 22px 24px;
  color: #fff;
  box-shadow: 0 18px 42px rgba(12, 77, 72, 0.24);
  box-shadow: 0 16px 40px rgba(12, 77, 72, 0.16);
}

.pc-hero-card__eyebrow {
  display: inline-flex;
  padding: 4px 10px;
  margin-bottom: 18px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
}

.pc-hero-card__eyebrow-text {
  font-size: 10px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, 0.88);
}

.pc-hero-card__content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pc-hero-card__title {
  max-width: 260px;
  font-size: 27px;
  font-weight: 800;
  line-height: 1.25;
  color: #fff;
}

.pc-hero-card__subtitle {
  max-width: 280px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}
</style>
