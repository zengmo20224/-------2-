<template>
  <view class="pc-page profile-page">
    <PcPageHeader title="我的" />

    <!-- Not logged in -->
    <view v-if="!isLoggedIn" class="profile-login">
      <text class="profile-login__hint">登录后享受预约、下单和社区互动</text>
      <view class="profile-login__btn" @tap="goLogin">
        <text class="profile-login__btn-text">手机号登录</text>
      </view>
      <view class="profile-login__links">
        <text class="profile-login__link" @tap="goRegister">新用户注册</text>
        <text class="profile-login__link" @tap="goForgotPassword">忘记密码</text>
      </view>
    </view>

    <!-- Logged in -->
    <view v-else class="profile-content">
      <view class="profile-avatar">
        <text class="profile-avatar__text">{{ displayName.charAt(0) }}</text>
      </view>
      <view class="profile-info">
        <text class="profile-info__name">{{ displayName }}</text>
        <text class="profile-info__phone">{{ displayPhone }}</text>
      </view>
    </view>

    <!-- Menu Items -->
    <view v-if="isLoggedIn" class="profile-menu">
      <view class="profile-menu-item" @tap="goPage('/pages/booking/list')">
        <text class="profile-menu-item__label">我的预约</text>
        <text class="profile-menu-item__arrow">></text>
      </view>
      <view class="profile-menu-item" @tap="goPage('/pages/order/list')">
        <text class="profile-menu-item__label">自提订单</text>
        <text class="profile-menu-item__arrow">></text>
      </view>
      <view class="profile-menu-item">
        <PcBlockedFeature title="我的宠物" reason="完整页面开发中" />
      </view>
      <view class="profile-menu-item">
        <PcBlockedFeature title="我的地址" reason="完整页面开发中" />
      </view>
      <view class="profile-menu-item profile-menu-item--logout" @tap="handleLogout">
        <text class="profile-menu-item__label profile-menu-item__label--danger">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcBlockedFeature from '@/components/PcBlockedFeature.vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const displayName = computed(() => userStore.profile?.nickname || '用户')
const displayPhone = computed(() => userStore.profile?.phone || '')

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}

function goForgotPassword() {
  uni.navigateTo({ url: '/pages/auth/forgot-password' })
}

function handleLogout() {
  userStore.logout()
  uni.showToast({ title: '已退出', icon: 'none' })
}

function goPage(url: string) {
  uni.navigateTo({ url })
}

onMounted(() => {
  if (isLoggedIn.value && !userStore.profile) {
    userStore.fetchProfile()
  }
})
</script>

<style scoped>
.profile-page {
  padding: var(--pc-page-padding);
}

.profile-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 32px 0;
}

.profile-login__hint {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
}

.profile-login__btn {
  height: var(--pc-btn-height);
  background: var(--pc-user-primary);
  border-radius: var(--pc-radius-card);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.profile-login__btn-text {
  color: #fff;
  font-size: var(--pc-font-card-title);
  font-weight: 600;
}

.profile-login__links {
  display: flex;
  gap: 24px;
}

.profile-login__link {
  font-size: var(--pc-font-body);
  color: var(--pc-user-primary);
}

.profile-content {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.profile-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--pc-user-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-avatar__text {
  font-size: var(--pc-font-card-title);
  color: #fff;
  font-weight: 600;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-info__name {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-ink);
}

.profile-info__phone {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
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

.profile-menu-item--logout {
  justify-content: center;
}

.profile-menu-item__label {
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
}

.profile-menu-item__label--danger {
  color: #e05050;
}

.profile-menu-item__arrow {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
}
</style>
