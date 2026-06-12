<template>
  <view class="pc-page booking-detail">
    <PcPageHeader title="预约详情" />
    <PcStatePanel status="unauthorized" />
  </view>
</template>

<script setup lang="ts">
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
</script>

<style scoped>
.booking-detail {
  padding: var(--pc-page-padding);
}
</style>
