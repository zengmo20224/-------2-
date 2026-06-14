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
        @tap="switchFilter(tab.value)"
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
          :mode="item.serviceMode"
          :duration-minutes="item.durationMinutes"
          :price="item.price"
          :image-url="item.coverUrl || undefined"
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
import { getServiceItems } from '@/api/service'
import type { ServiceItem } from '@/types/service'

type FilterValue = 'ALL' | 'STORE' | 'HOME'

const filterTabs: { label: string; value: FilterValue }[] = [
  { label: '全部', value: 'ALL' },
  { label: '到店服务', value: 'STORE' },
  { label: '上门服务', value: 'HOME' },
]

const activeFilter = ref<FilterValue>('ALL')
const listStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const services = ref<ServiceItem[]>([])

function switchFilter(value: FilterValue) {
  if (activeFilter.value === value) return
  activeFilter.value = value
  loadServices()
}

async function loadServices() {
  listStatus.value = 'loading'

  const params: { size: number; serviceMode?: string } = { size: 50 }
  if (activeFilter.value === 'STORE') {
    params.serviceMode = 'STORE'
  } else if (activeFilter.value === 'HOME') {
    params.serviceMode = 'HOME'
  }

  const res = await getServiceItems(params)

  if (!res.success || !res.data) {
    listStatus.value = 'error'
    return
  }

  services.value = res.data.items
  listStatus.value = services.value.length > 0 ? 'success' : 'empty'
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/services/detail?id=${id}` })
}

loadServices()
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
