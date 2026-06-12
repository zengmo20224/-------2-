<template>
  <view class="pc-page services-page">
    <PcPageHeader title="服务" />

    <!-- Filter Tabs -->
    <view class="services-tabs">
      <view
        v-for="tab in filterTabs"
        :key="tab.value"
        class="services-tab"
        :class="{ 'services-tab--active': activeFilter === tab.value }"
        @tap="activeFilter = tab.value"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- Service List -->
    <PcStatePanel
      :status="listStatus"
      empty-text="暂无可用服务"
      error-message=""
      @retry="loadServices"
    >
      <view class="services-list">
        <PcServiceCard
          v-for="item in services"
          :key="item.id"
          :name="item.name"
          :mode="item.mode"
          :duration-minutes="item.durationMinutes"
          :price-min="item.priceMin"
          @tap="goDetail(item.id)"
        />
      </view>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcServiceCard from '@/components/PcServiceCard.vue'
import type { ServiceItem } from '@/types/service'

const filterTabs = [
  { label: '全部', value: 'ALL' },
  { label: '到店服务', value: 'STORE' },
  { label: '上门服务', value: 'HOME' },
]

const activeFilter = ref('ALL')
const listStatus = ref<'loading' | 'empty' | 'success' | 'error'>('empty')
const services = ref<ServiceItem[]>([])

function loadServices() {
  // Will connect to API in phase 12 implementation
  listStatus.value = 'empty'
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/services/detail?id=${id}` })
}
</script>

<style scoped>
.services-page {
  padding: var(--pc-page-padding);
}

.services-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.services-tab {
  padding: 6px 16px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid var(--pc-user-line);
}

.services-tab--active {
  background: var(--pc-user-primary);
  border-color: var(--pc-user-primary);
}

.services-tab--active text {
  color: #fff;
}

.services-tab text {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
}

.services-list {
  display: flex;
  flex-direction: column;
  gap: var(--pc-card-gap);
}
</style>
