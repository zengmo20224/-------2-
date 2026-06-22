<template>
  <view class="pc-page ai-status">
    <PcBlockedFeature title="智能助手暂未开放" reason="AI 能力正在开发中（D-008），敬请期待" />
  </view>
</template>

<script setup lang="ts">
import PcBlockedFeature from '@/components/PcBlockedFeature.vue'
</script>

<style scoped>
.ai-status {
  padding: 20px;
  padding-top: 60px;
}
</style>
