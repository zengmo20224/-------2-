<template>
  <view class="pc-page-header">
    <text class="pc-page-header__title">{{ title }}</text>
    <slot name="action" />
  </view>
</template>

<script setup lang="ts">
defineProps<{
  title: string
}>()
</script>

<style scoped>
.pc-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0 12px;
}

.pc-page-header__title {
  font-size: var(--pc-font-title);
  font-weight: 700;
  color: var(--pc-user-ink);
}
</style>
