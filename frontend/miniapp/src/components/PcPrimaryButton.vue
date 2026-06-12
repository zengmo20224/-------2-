<template>
  <view
    class="pc-primary-button"
    :class="{ 'pc-primary-button--disabled': disabled }"
    @tap="!disabled && $emit('tap')"
  >
    <text class="pc-primary-button__text">
      <slot>{{ text }}</slot>
    </text>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  text?: string
  disabled?: boolean
}>()

defineEmits<{
  (e: 'tap'): void
}>()
</script>

<style scoped>
.pc-primary-button {
  height: var(--pc-btn-height);
  background: var(--pc-user-primary);
  border-radius: var(--pc-radius-card);
  display: flex;
  align-items: center;
  justify-content: center;
}

.pc-primary-button--disabled {
  opacity: 0.5;
}

.pc-primary-button__text {
  color: #fff;
  font-size: var(--pc-font-card-title);
  font-weight: 600;
}
</style>
