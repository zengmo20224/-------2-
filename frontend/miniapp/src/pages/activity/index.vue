<template>
  <view class="pc-page activity-page">
    <PcPageHeader title="优惠活动" />

    <PcStatePanel
      :status="listStatus"
      empty-text="暂无活动"
      @retry="loadActivities"
    >
      <view class="activity-list">
        <view
          v-for="item in activities"
          :key="item.id"
          class="activity-card"
          @tap="goDetail(item.id)"
        >
          <text class="activity-card__title">{{ item.title }}</text>
          <text v-if="item.description" class="activity-card__desc">{{ item.description }}</text>
          <view class="activity-card__tags">
            <text v-if="item.productNames.length > 0" class="activity-card__tag">
              {{ item.productNames.length }} 件商品
            </text>
            <text v-if="item.serviceNames.length > 0" class="activity-card__tag">
              {{ item.serviceNames.length }} 项服务
            </text>
          </view>
        </view>
      </view>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import { getActivities } from '@/api/activity'
import type { ActivityItem } from '@/types/activity'

const listStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const activities = ref<ActivityItem[]>([])

async function loadActivities() {
  listStatus.value = 'loading'

  const res = await getActivities({ size: 20 })

  if (!res.success || !res.data) {
    listStatus.value = 'error'
    return
  }

  activities.value = res.data.items
  listStatus.value = activities.value.length > 0 ? 'success' : 'empty'
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
}

loadActivities()
</script>

<style scoped>
.activity-page {
  padding: var(--pc-page-padding);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--pc-card-gap);
}

.activity-card {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 16px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.activity-card__title {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-ink);
  margin-bottom: 6px;
}

.activity-card__desc {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
  line-height: 1.6;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-card__tags {
  display: flex;
  gap: 8px;
}

.activity-card__tag {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-primary);
  background: var(--pc-user-soft);
  padding: 2px 8px;
  border-radius: 8px;
}
</style>
