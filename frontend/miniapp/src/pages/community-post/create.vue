<template>
  <view class="pc-page post-create">
    <PcPageHeader title="发布帖子" />
    <PcStatePanel status="unauthorized" />
  </view>
</template>

<script setup lang="ts">
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
</script>

<style scoped>
.post-create {
  padding: var(--pc-page-padding);
}
</style>
