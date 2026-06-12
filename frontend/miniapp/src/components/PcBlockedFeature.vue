<template>
  <view class="pc-blocked-feature">
    <text class="pc-blocked-feature__icon">&#x1F512;</text>
    <text class="pc-blocked-feature__title">{{ title }}</text>
    <text v-if="reason" class="pc-blocked-feature__reason">{{ reason }}</text>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  reason?: string
}>(), {
  reason: '',
})
</script>

<style scoped>
.pc-blocked-feature {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background: #fff;
  border-radius: var(--pc-radius-card);
  gap: 8px;
}

.pc-blocked-feature__icon {
  font-size: 28px;
}

.pc-blocked-feature__title {
  font-size: var(--pc-font-card-title);
  font-weight: 600;
  color: var(--pc-user-ink);
}

.pc-blocked-feature__reason {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
}
</style>
