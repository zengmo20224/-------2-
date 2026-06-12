<template>
  <view class="pc-page order-confirm">
    <PcPageHeader title="确认订单" />
    <PcStatePanel status="unauthorized" />
  </view>
</template>

<script setup lang="ts">
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
</script>

<style scoped>
.order-confirm {
  padding: var(--pc-page-padding);
}
</style>
