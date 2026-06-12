<template>
  <view class="pc-page booking-list">
    <PcPageHeader title="我的预约" />
    <PcStatePanel status="unauthorized" />
  </view>
</template>

<script setup lang="ts">
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
</script>

<style scoped>
.booking-list {
  padding: var(--pc-page-padding);
}
</style>
