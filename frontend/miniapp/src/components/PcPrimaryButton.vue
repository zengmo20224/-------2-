<template>
  <view
    class="pc-primary-button"
    :class="{ 'pc-primary-button--disabled': disabled || loading }"
    @tap="!(disabled || loading) && $emit('tap')"
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
  loading?: boolean
}>()

defineEmits<{
  (e: 'tap'): void
}>()
</script>

<style scoped>
.pc-primary-button {
  height: 48px;
  min-height: 48px;
  background: #11796F;
  background: #11796F;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 24px rgba(17, 121, 111, 0.24);
}

.pc-primary-button:active {
  transform: scale(0.98);
}

.pc-primary-button--disabled {
  opacity: 0.5;
}

.pc-primary-button__text {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}
</style>
