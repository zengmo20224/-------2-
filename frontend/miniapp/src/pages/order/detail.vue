<template>
  <view class="pc-page order-detail">
    <PcPageHeader title="订单详情" />
    <PcStatePanel status="unauthorized" />
  </view>
</template>

<script setup lang="ts">
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
</script>

<style scoped>
.order-detail {
  padding: var(--pc-page-padding);
}
</style>
