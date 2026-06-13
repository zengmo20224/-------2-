<template>
  <view class="pc-page service-detail">
    <PcStatePanel
      :status="pageStatus"
      empty-text="服务不存在"
      @retry="loadDetail"
    >
      <template v-if="service">
        <view class="service-detail__header">
          <text class="service-detail__name">{{ service.name }}</text>
          <PcStatusTag :label="modeLabel" type="primary" />
        </view>
        <view v-if="service.description" class="service-detail__desc">
          <text>{{ service.description }}</text>
        </view>
        <view class="service-detail__info">
          <text class="service-detail__label">时长：{{ durationText }}</text>
          <text class="service-detail__price">
            {{ priceText }}起
          </text>
        </view>
        <view class="service-detail__action">
          <PcPrimaryButton text="立即预约" @tap="goBooking" />
        </view>
      </template>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcStatusTag from '@/components/PcStatusTag.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import { getServiceDetail } from '@/api/service'
import type { ServiceItem } from '@/types/service'
import { formatDuration, formatYuan } from '@/utils/format'

const service = ref<ServiceItem | null>(null)
const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')

const modeLabel = computed(() => {
  const map: Record<string, string> = { STORE: '到店', HOME: '上门', BOTH: '到店/上门' }
  return map[service.value?.serviceMode ?? ''] ?? ''
})

const durationText = computed(() => formatDuration(service.value?.durationMinutes))
const priceText = computed(() => formatYuan(service.value?.price ?? 0))

async function loadDetail() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const id = currentPage?.options?.id

  if (!id) {
    pageStatus.value = 'empty'
    return
  }

  pageStatus.value = 'loading'
  const res = await getServiceDetail(String(id))

  if (!res.success || !res.data) {
    pageStatus.value = 'error'
    return
  }

  service.value = res.data
  pageStatus.value = 'success'
}

function goBooking() {
  if (service.value) {
    uni.navigateTo({ url: `/pages/booking/create?serviceId=${service.value.id}` })
  }
}

loadDetail()
</script>

<style scoped>
.service-detail {
  padding: var(--pc-page-padding);
}

.service-detail__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.service-detail__name {
  font-size: var(--pc-font-title);
  font-weight: 700;
  color: var(--pc-user-ink);
}

.service-detail__desc {
  margin-bottom: 16px;
}

.service-detail__desc text {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
  line-height: 1.6;
}

.service-detail__info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 24px;
}

.service-detail__label {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
}

.service-detail__price {
  font-size: 20px;
  color: var(--pc-user-accent);
  font-weight: 700;
}

.service-detail__action {
  margin-top: 16px;
}
</style>
