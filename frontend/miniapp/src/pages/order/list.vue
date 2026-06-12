<template>
  <view class="pc-page order-list">
    <PcPageHeader title="我的订单" />
    <PcStatePanel status="unauthorized" />
  </view>
</template>

<script setup lang="ts">
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
</script>

<style scoped>
.order-list {
  padding: var(--pc-page-padding);
}
</style>
