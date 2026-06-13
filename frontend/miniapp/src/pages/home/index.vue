<template>
  <view class="pc-page home-page">
    <!-- Hero Section -->
    <PcHeroCard
      title="给它安心的照护时间"
      subtitle="洗护、美容与上门照护，为宠物安排专业服务"
    >
      <template #action>
        <view class="home-hero__btn" @tap="goBooking">
          <text class="home-hero__btn-text">开始预约</text>
        </view>
      </template>
    </PcHeroCard>

    <!-- Quick Service Shortcuts -->
    <view class="pc-section">
      <text class="home-section-title">常用服务</text>
      <view class="home-shortcuts">
        <view class="home-shortcut-item" @tap="goServices">
          <view class="home-shortcut-icon home-shortcut-icon--grooming">
            <text>洗护</text>
          </view>
          <text class="home-shortcut-label">洗护</text>
        </view>
        <view class="home-shortcut-item" @tap="goServices">
          <view class="home-shortcut-icon home-shortcut-icon--beauty">
            <text>美容</text>
          </view>
          <text class="home-shortcut-label">美容</text>
        </view>
        <view class="home-shortcut-item" @tap="goServices">
          <view class="home-shortcut-icon home-shortcut-icon--home">
            <text>上门</text>
          </view>
          <text class="home-shortcut-label">上门照护</text>
        </view>
        <view class="home-shortcut-item" @tap="goServices">
          <view class="home-shortcut-icon home-shortcut-icon--care">
            <text>照护</text>
          </view>
          <text class="home-shortcut-label">寄养</text>
        </view>
      </view>
    </view>

    <!-- Recent Community Posts -->
    <view class="pc-section">
      <text class="home-section-title">社区动态</text>
      <PcStatePanel :status="postsStatus" empty-text="暂无动态" error-message="">
        <view class="home-posts">
          <view
            v-for="post in recentPosts"
            :key="post.id"
            class="home-post-item"
            @tap="goPostDetail(post.id)"
          >
            <text class="home-post-title">{{ post.title }}</text>
            <view class="home-post-meta">
              <text class="home-post-stat">{{ post.likeCount }} 赞</text>
              <text class="home-post-stat">{{ post.commentCount }} 评论</text>
            </view>
          </view>
        </view>
      </PcStatePanel>
    </view>

    <!-- AI Status -->
    <view class="pc-section">
      <PcBlockedFeature title="智能助手暂未开放" reason="AI 能力正在开发中，敬请期待" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcHeroCard from '@/components/PcHeroCard.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcBlockedFeature from '@/components/PcBlockedFeature.vue'
import { getPosts } from '@/api/community'
import type { PostItem } from '@/types/community'

const recentPosts = ref<PostItem[]>([])
const postsStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')

async function loadRecentPosts() {
  postsStatus.value = 'loading'

  const res = await getPosts({ size: 5 })

  if (!res.success || !res.data) {
    postsStatus.value = 'error'
    return
  }

  recentPosts.value = res.data.items
  postsStatus.value = recentPosts.value.length > 0 ? 'success' : 'empty'
}

function goBooking() {
  uni.navigateTo({ url: '/pages/booking/create' })
}

function goServices() {
  uni.switchTab({ url: '/pages/services/index' })
}

function goPostDetail(id: string) {
  uni.navigateTo({ url: `/pages/community/detail?id=${id}` })
}

loadRecentPosts()
</script>

<style scoped>
.home-page {
  padding: var(--pc-page-padding);
}

.home-hero__btn {
  margin-top: 12px;
  height: 40px;
  background: var(--pc-user-accent);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  align-self: flex-start;
  padding: 0 24px;
}

.home-hero__btn-text {
  color: #fff;
  font-size: var(--pc-font-body);
  font-weight: 600;
}

.home-section-title {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-ink);
  margin-bottom: 12px;
}

.home-shortcuts {
  display: flex;
  gap: 12px;
  justify-content: space-between;
}

.home-shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.home-shortcut-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--pc-font-caption);
  font-weight: 600;
}

.home-shortcut-icon--grooming { background: var(--pc-user-soft); color: var(--pc-user-primary); }
.home-shortcut-icon--beauty { background: var(--pc-user-accent-soft); color: var(--pc-user-accent); }
.home-shortcut-icon--home { background: #E8F0FE; color: #4285F4; }
.home-shortcut-icon--care { background: #FCE4EC; color: #E91E63; }

.home-shortcut-label {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-ink);
}

.home-posts {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.home-post-item {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 14px 16px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.home-post-title {
  font-size: var(--pc-font-body);
  font-weight: 600;
  color: var(--pc-user-ink);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.home-post-meta {
  display: flex;
  gap: 16px;
  margin-top: 6px;
}

.home-post-stat {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
}
</style>
