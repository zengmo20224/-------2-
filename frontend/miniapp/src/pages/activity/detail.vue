<template>
  <view class="pc-page activity-detail">
    <PcStatePanel
      :status="pageStatus"
      empty-text="活动不存在"
      @retry="loadDetail"
    >
      <template v-if="activity">
        <view class="activity-detail__card">
          <text class="activity-detail__title">{{ activity.title }}</text>
          <text v-if="activity.description" class="activity-detail__desc">{{ activity.description }}</text>
          <view v-if="activity.productNames.length > 0" class="activity-detail__section">
            <text class="activity-detail__section-title">参与商品</text>
            <view class="activity-detail__chips">
              <text v-for="name in activity.productNames" :key="name" class="activity-detail__chip">{{ name }}</text>
            </view>
          </view>
          <view v-if="activity.serviceNames.length > 0" class="activity-detail__section">
            <text class="activity-detail__section-title">参与服务</text>
            <view class="activity-detail__chips">
              <text v-for="name in activity.serviceNames" :key="name" class="activity-detail__chip">{{ name }}</text>
            </view>
          </view>
        </view>
      </template>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import { getActivityDetail } from '@/api/activity'
import type { ActivityItem } from '@/types/activity'

const activity = ref<ActivityItem | null>(null)
const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')

async function loadDetail() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const id = currentPage?.options?.id

  if (!id) {
    pageStatus.value = 'empty'
    return
  }

  pageStatus.value = 'loading'
  const res = await getActivityDetail(String(id))

  if (!res.success || !res.data) {
    pageStatus.value = 'error'
    return
  }

  activity.value = res.data
  pageStatus.value = 'success'
}

loadDetail()
</script>

<style scoped>
.activity-detail {
  padding: var(--pc-page-padding);
}

.activity-detail__card {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 20px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.activity-detail__title {
  font-size: var(--pc-font-title);
  font-weight: 700;
  color: var(--pc-user-ink);
  margin-bottom: 8px;
}

.activity-detail__desc {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
  line-height: 1.8;
  margin-bottom: 16px;
}

.activity-detail__section {
  margin-bottom: 16px;
}

.activity-detail__section-title {
  font-size: var(--pc-font-body);
  font-weight: 600;
  color: var(--pc-user-ink);
  margin-bottom: 8px;
}

.activity-detail__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.activity-detail__chip {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-primary);
  background: var(--pc-user-soft);
  padding: 4px 12px;
  border-radius: 12px;
}
</style>
