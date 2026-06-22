<template>
  <view class="pc-booking-card" @tap="$emit('tap')">
    <view class="pc-booking-card__header">
      <text class="pc-booking-card__service">{{ serviceName }}</text>
      <PcStatusTag :label="statusLabel" :type="statusType" />
    </view>
    <view class="pc-booking-card__detail">
      <text class="pc-booking-card__date">{{ dateText }}</text>
      <text v-if="timeSlot" class="pc-booking-card__time">{{ timeSlot }}</text>
    </view>
    <view v-if="staffName" class="pc-booking-card__staff">
      <text>{{ staffName }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PcStatusTag from './PcStatusTag.vue'
import { formatDate } from '@/utils/format'

const props = defineProps<{
  serviceName: string
  status: string
  statusLabel: string
  bookingDate: string
  timeSlot?: string
  staffName?: string
}>()

defineEmits<{
  (e: 'tap'): void
}>()

const statusType = computed(() => {
  const map: Record<string, string> = {
    PENDING_CONFIRM: 'warning',
    CONFIRMED: 'primary',
    IN_SERVICE: 'success',
    COMPLETED: 'info',
    CANCELLED: 'danger',
    REJECTED: 'danger',
  }
  return map[props.status] ?? 'info'
})

const dateText = computed(() => formatDate(props.bookingDate))
</script>

<style scoped>
.pc-booking-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.pc-booking-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.pc-booking-card__service {
  font-size: 16px;
  font-weight: 600;
  color: #19322E;
}

.pc-booking-card__detail {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pc-booking-card__date,
.pc-booking-card__time {
  font-size: 14px;
  color: #71817D;
}

.pc-booking-card__staff {
  margin-top: 4px;
  font-size: 11px;
  color: #71817D;
}
</style>
