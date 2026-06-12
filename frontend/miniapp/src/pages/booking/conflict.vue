<template>
  <view class="pc-page booking-conflict">
    <view class="booking-conflict__content">
      <text class="booking-conflict__icon">&#x26A0;</text>
      <text class="booking-conflict__title">需要重新选择时间</text>
      <text class="booking-conflict__hint">您选择的时间段已被预约，请选择其他时间</text>
      <PcPrimaryButton text="重新选择" @tap="goBack" />
    </view>
  </view>
</template>

<script setup lang="ts">
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'

function goBack() {
  uni.navigateBack()
}
</script>

<style scoped>
.booking-conflict {
  padding: var(--pc-page-padding);
}

.booking-conflict__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 60px;
  gap: 12px;
}

.booking-conflict__icon {
  font-size: 48px;
}

.booking-conflict__title {
  font-size: var(--pc-font-title);
  font-weight: 700;
  color: var(--pc-user-ink);
}

.booking-conflict__hint {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
  margin-bottom: 24px;
}
</style>
