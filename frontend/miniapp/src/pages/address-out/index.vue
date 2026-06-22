<template>
  <view class="pc-page address-out">
    <view class="address-out__content">
      <text class="address-out__icon">&#x1F4CD;</text>
      <text class="address-out__title">地址超出服务范围</text>
      <text class="address-out__hint">您的地址不在上门服务范围内，请选择其他服务方式</text>
      <PcPrimaryButton text="选择到店服务" @tap="goServices" />
    </view>
  </view>
</template>

<script setup lang="ts">
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'

function goServices() {
  uni.switchTab({ url: '/pages/services/index' })
}
</script>

<style scoped>
.address-out {
  padding: 20px;
}

.address-out__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 60px;
  gap: 12px;
}

.address-out__icon {
  font-size: 48px;
}

.address-out__title {
  font-size: 24px;
  font-weight: 700;
  color: #19322E;
}

.address-out__hint {
  font-size: 14px;
  color: #71817D;
  text-align: center;
  margin-bottom: 24px;
}
</style>
