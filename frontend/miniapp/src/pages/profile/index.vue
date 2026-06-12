<template>
  <view class="pc-page profile-page">
    <PcPageHeader title="我的" />

    <!-- Login Status -->
    <PcStatePanel v-if="!isLoggedIn" status="unauthorized" />

    <!-- Profile Content -->
    <view v-else class="profile-content">
      <view class="profile-avatar">
        <text class="profile-avatar__placeholder">用户</text>
      </view>
    </view>

    <!-- Menu Items -->
    <view class="profile-menu">
      <view class="profile-menu-item" @tap="goPage('/pages/booking/list')">
        <text class="profile-menu-item__label">我的预约</text>
        <text class="profile-menu-item__arrow">></text>
      </view>
      <view class="profile-menu-item" @tap="goPage('/pages/order/list')">
        <text class="profile-menu-item__label">自提订单</text>
        <text class="profile-menu-item__arrow">></text>
      </view>
      <view class="profile-menu-item">
        <PcBlockedFeature title="我的宠物" reason="API 待补齐" />
      </view>
      <view class="profile-menu-item">
        <PcBlockedFeature title="我的地址" reason="API 待补齐" />
      </view>
      <view class="profile-menu-item">
        <PcBlockedFeature title="收藏与互动" reason="功能开发中" />
      </view>
      <view class="profile-menu-item">
        <text class="profile-menu-item__label">联系客服</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcBlockedFeature from '@/components/PcBlockedFeature.vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

function goPage(url: string) {
  uni.navigateTo({ url })
}
</script>

<style scoped>
.profile-page {
  padding: var(--pc-page-padding);
}

.profile-content {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--pc-user-soft);
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-avatar__placeholder {
  font-size: var(--pc-font-card-title);
  color: var(--pc-user-primary);
  font-weight: 600;
}

.profile-menu {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: var(--pc-user-line);
  border-radius: var(--pc-radius-card);
  overflow: hidden;
}

.profile-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fff;
}

.profile-menu-item__label {
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
}

.profile-menu-item__arrow {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
}
</style>
