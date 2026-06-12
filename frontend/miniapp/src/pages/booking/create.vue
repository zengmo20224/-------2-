<template>
  <view class="pc-page booking-create">
    <PcStatePanel status="unauthorized" />
  </view>
</template>

<script setup lang="ts">
import PcStatePanel from '@/components/PcStatePanel.vue'
</script>

<style scoped>
.booking-create {
  padding: var(--pc-page-padding);
}
</style>
