<template>
  <view class="pc-page service-detail">
    <PcStatePanel
      :status="pageStatus"
      empty-text="服务不存在"
      @retry="loadDetail"
    >
      <template v-if="service">
        <!-- Cover Image -->
        <view class="service-detail__cover">
          <image v-if="service.coverUrl" class="service-detail__cover-img" :src="service.coverUrl" mode="aspectFill" />
          <view v-else class="service-detail__cover-img service-detail__cover-placeholder">
            <text class="service-detail__cover-placeholder-text">{{ modeLabel }}</text>
          </view>
        </view>

        <view class="service-detail__body">
          <view class="service-detail__header">
            <text class="service-detail__name">{{ service.name }}</text>
            <PcStatusTag :label="modeLabel" type="primary" />
          </view>
          <view v-if="service.description" class="service-detail__desc">
            <text>{{ service.description }}</text>
          </view>
          <view class="service-detail__info">
            <view class="service-detail__info-row">
              <text class="service-detail__label">服务时长</text>
              <text class="service-detail__value">{{ durationText }}</text>
            </view>
            <view class="service-detail__info-row">
              <text class="service-detail__label">适用宠物</text>
              <text class="service-detail__value">{{ petTypeText }}</text>
            </view>
            <view class="service-detail__info-row">
              <text class="service-detail__label">参考价格</text>
              <text class="service-detail__price">{{ priceText }}起</text>
            </view>
          </view>
          <view class="service-detail__action">
            <PcPrimaryButton text="立即预约" @tap="goBooking" />
          </view>
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

const petTypeText = computed(() => {
  const map: Record<string, string> = { DOG: '犬类', CAT: '猫类', ALL: '所有宠物' }
  return map[service.value?.petType ?? ''] ?? '所有宠物'
})

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

.service-detail__cover {
  width: 100%;
  height: 200px;
  border-radius: var(--pc-radius-card-lg);
  overflow: hidden;
  margin-bottom: 16px;
}

.service-detail__cover-img {
  width: 100%;
  height: 100%;
}

.service-detail__cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--pc-user-soft);
}

.service-detail__cover-placeholder-text {
  font-size: 48px;
  font-weight: 700;
  color: var(--pc-user-primary);
  opacity: 0.3;
}

.service-detail__body {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 20px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
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
  padding-bottom: 16px;
  border-bottom: 1px solid var(--pc-user-line);
}

.service-detail__desc text {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
  line-height: 1.6;
}

.service-detail__info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 24px;
}

.service-detail__info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.service-detail__label {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
}

.service-detail__value {
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
  font-weight: 500;
}

.service-detail__price {
  font-size: 20px;
  color: var(--pc-user-accent);
  font-weight: 700;
}

.service-detail__action {
  margin-top: 8px;
}
</style>
