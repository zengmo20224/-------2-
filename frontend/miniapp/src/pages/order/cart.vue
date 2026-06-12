<template>
  <view class="pc-page order-cart">
    <PcPageHeader title="购物车" />
    <PcStatePanel status="unauthorized" />
  </view>
</template>

<script setup lang="ts">
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
</script>

<style scoped>
.order-cart {
  padding: var(--pc-page-padding);
}
</style>
