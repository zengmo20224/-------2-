<template>
  <view class="pc-form-field">
    <text class="pc-form-field__label">{{ label }}</text>
    <slot />
  </view>
</template>

<script setup lang="ts">
defineProps<{
  label: string
}>()
</script>

<style scoped>
.pc-form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.pc-form-field__label {
  font-size: 14px;
  font-weight: 600;
  color: #19322E;
}
</style>
