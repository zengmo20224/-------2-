<template>
  <view class="pc-page booking-success">
    <view class="booking-success__content">
      <text class="booking-success__icon">&#x2705;</text>
      <text class="booking-success__title">预约提交成功</text>
      <text class="booking-success__hint">我们将尽快确认您的预约</text>
      <PcPrimaryButton text="查看我的预约" @tap="goMyBookings" />
    </view>
  </view>
</template>

<script setup lang="ts">
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'

function goMyBookings() {
  uni.navigateTo({ url: '/pages/booking/list' })
}
</script>

<style scoped>
.booking-success {
  padding: 20px;
}

.booking-success__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 60px;
  gap: 12px;
}

.booking-success__icon {
  font-size: 48px;
}

.booking-success__title {
  font-size: 24px;
  font-weight: 700;
  color: #19322E;
}

.booking-success__hint {
  font-size: 14px;
  color: #71817D;
  margin-bottom: 24px;
}
</style>
