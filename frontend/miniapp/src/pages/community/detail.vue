<template>
  <view class="pc-page community-detail">
    <PcStatePanel
      :status="pageStatus"
      empty-text="帖子不存在"
      @retry="loadDetail"
    >
      <template v-if="post">
        <!-- Post Content -->
        <view class="community-detail__card">
          <text class="community-detail__title">{{ post.title }}</text>
          <text class="community-detail__content">{{ post.content }}</text>
          <view v-if="post.imageUrls && post.imageUrls.length > 0" class="community-detail__images">
            <image
              v-for="(url, index) in post.imageUrls"
              :key="index"
              class="community-detail__image"
              :src="url"
              mode="aspectFill"
            />
          </view>
          <view class="community-detail__meta">
            <text class="community-detail__stat">{{ post.likeCount }} 赞</text>
            <text class="community-detail__stat">{{ post.commentCount }} 评论</text>
            <text class="community-detail__stat">{{ post.viewCount }} 浏览</text>
          </view>
        </view>

        <!-- Comments Section -->
        <view class="community-detail__comments">
          <text class="community-detail__comments-title">评论</text>
          <PcStatePanel :status="commentsStatus" empty-text="暂无评论">
            <view class="community-detail__comment-list">
              <view v-for="comment in comments" :key="comment.id" class="community-detail__comment">
                <text class="community-detail__comment-text">{{ comment.content }}</text>
                <text class="community-detail__comment-meta">{{ comment.likeCount }} 赞</text>
              </view>
            </view>
          </PcStatePanel>
        </view>
      </template>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import { getPostDetail, getPostComments } from '@/api/community'
import type { PostDetail, CommentItem } from '@/types/community'

const post = ref<PostDetail | null>(null)
const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const comments = ref<CommentItem[]>([])
const commentsStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')

async function loadDetail() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const id = currentPage?.options?.id

  if (!id) {
    pageStatus.value = 'empty'
    return
  }

  pageStatus.value = 'loading'

  const [postRes, commentRes] = await Promise.all([
    getPostDetail(String(id)),
    getPostComments(String(id), { size: 50 }),
  ])

  if (!postRes.success || !postRes.data) {
    pageStatus.value = 'error'
    return
  }

  post.value = postRes.data
  pageStatus.value = 'success'

  if (commentRes.success && commentRes.data) {
    comments.value = commentRes.data.items
    commentsStatus.value = comments.value.length > 0 ? 'success' : 'empty'
  } else {
    commentsStatus.value = 'error'
  }
}

loadDetail()
</script>

<style scoped>
.community-detail {
  padding: var(--pc-page-padding);
}

.community-detail__card {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 20px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
  margin-bottom: 16px;
}

.community-detail__title {
  font-size: var(--pc-font-title);
  font-weight: 700;
  color: var(--pc-user-ink);
  margin-bottom: 8px;
}

.community-detail__content {
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
  line-height: 1.8;
  margin-bottom: 12px;
}

.community-detail__images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.community-detail__image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
}

.community-detail__meta {
  display: flex;
  gap: 16px;
}

.community-detail__stat {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
}

.community-detail__comments {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 20px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.community-detail__comments-title {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-ink);
  margin-bottom: 12px;
}

.community-detail__comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.community-detail__comment {
  padding: 10px 0;
  border-bottom: 1px solid var(--pc-user-line);
}

.community-detail__comment-text {
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
  line-height: 1.6;
}

.community-detail__comment-meta {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
  margin-top: 4px;
}
</style>
