<template>
  <view class="pc-time-slot-grid">
    <view
      v-for="slot in slots"
      :key="slot.time"
      class="pc-time-slot-grid__item"
      :class="{
        'pc-time-slot-grid__item--active': modelValue === slot.time,
        'pc-time-slot-grid__item--disabled': slot.disabled,
      }"
      @tap="!slot.disabled && $emit('update:modelValue', slot.time)"
    >
      <text>{{ slot.time }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
interface TimeSlot {
  time: string
  disabled?: boolean
}

defineProps<{
  slots: TimeSlot[]
  modelValue?: string
}>()

defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<style scoped>
.pc-time-slot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.pc-time-slot-grid__item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #E2E9E6;
  font-size: 14px;
  color: #19322E;
}

.pc-time-slot-grid__item--active {
  background: #11796F;
  border-color: #11796F;
  color: #fff;
}

.pc-time-slot-grid__item--disabled {
  opacity: 0.4;
}
</style>
