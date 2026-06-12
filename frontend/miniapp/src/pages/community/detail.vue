<template>
  <view class="pc-page community-detail">
    <PcStatePanel
      :status="pageStatus"
      empty-text="帖子不存在"
      @retry="loadDetail"
    >
      <template v-if="post">
        <view class="community-detail__card">
          <text class="community-detail__content">{{ post.content }}</text>
          <view class="community-detail__meta">
            <text class="community-detail__stat">{{ post.likeCount }} 赞</text>
            <text class="community-detail__stat">{{ post.commentCount }} 评论</text>
          </view>
        </view>
      </template>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import type { PostItem } from '@/types/community'

const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('empty')
const post = ref<PostItem | null>(null)

function loadDetail() {
  pageStatus.value = 'empty'
}
</script>

<style scoped>
.community-detail {
  padding: var(--pc-page-padding);
}

.community-detail__card {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 20px;
}

.community-detail__content {
  font-size: var(--pc-font-card-title);
  color: var(--pc-user-ink);
  line-height: 1.8;
  margin-bottom: 12px;
}

.community-detail__meta {
  display: flex;
  gap: 16px;
}

.community-detail__stat {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
}
</style>
