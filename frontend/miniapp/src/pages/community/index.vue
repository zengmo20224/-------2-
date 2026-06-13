<template>
  <view class="pc-page community-page">
    <PcPageHeader title="社区">
      <template #action>
        <view class="community-post-btn" @tap="goCreatePost">
          <text class="community-post-btn__text">发帖</text>
        </view>
      </template>
    </PcPageHeader>

    <PcStatePanel
      :status="listStatus"
      empty-text="暂无社区内容"
      @retry="loadPosts"
    >
      <view class="community-list">
        <view
          v-for="post in posts"
          :key="post.id"
          class="community-card"
          @tap="goDetail(post.id)"
        >
          <text class="community-card__title">{{ post.title }}</text>
          <text class="community-card__content">{{ post.content }}</text>
          <view class="community-card__meta">
            <text class="community-card__stat">{{ post.likeCount }} 赞</text>
            <text class="community-card__stat">{{ post.commentCount }} 评论</text>
            <text class="community-card__stat">{{ post.viewCount }} 浏览</text>
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
import { getPosts } from '@/api/community'
import type { PostItem } from '@/types/community'

const listStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const posts = ref<PostItem[]>([])

async function loadPosts() {
  listStatus.value = 'loading'

  const res = await getPosts({ size: 20 })

  if (!res.success || !res.data) {
    listStatus.value = 'error'
    return
  }

  posts.value = res.data.items
  listStatus.value = posts.value.length > 0 ? 'success' : 'empty'
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/community/detail?id=${id}` })
}

function goCreatePost() {
  uni.navigateTo({ url: '/pages/community-post/create' })
}

loadPosts()
</script>

<style scoped>
.community-page {
  padding: var(--pc-page-padding);
}

.community-post-btn {
  padding: 6px 16px;
  background: var(--pc-user-primary);
  border-radius: 16px;
}

.community-post-btn__text {
  color: #fff;
  font-size: var(--pc-font-body);
  font-weight: 600;
}

.community-list {
  display: flex;
  flex-direction: column;
  gap: var(--pc-card-gap);
}

.community-card {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 16px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.community-card__title {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-ink);
  margin-bottom: 6px;
}

.community-card__content {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
  line-height: 1.6;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.community-card__meta {
  display: flex;
  gap: 16px;
}

.community-card__stat {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
}
</style>
