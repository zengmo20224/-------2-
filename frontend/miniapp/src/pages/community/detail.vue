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

          <!-- Action Buttons -->
          <view class="community-detail__actions">
            <view class="community-detail__action-btn" @tap="handleLike">
              <text>{{ hasLiked ? '❤️' : '🤍' }} 赞</text>
            </view>
            <view class="community-detail__action-btn" @tap="handleFavorite">
              <text>{{ hasFavorited ? '⭐' : '☆' }} 收藏</text>
            </view>
          </view>
        </view>

        <!-- Comments Section -->
        <view class="community-detail__comments">
          <text class="community-detail__comments-title">评论</text>

          <!-- Comment Input -->
          <view v-if="isLoggedIn" class="community-detail__comment-input">
            <PcFormField label="" placeholder="写下你的评论..." v-model="commentInput" />
            <PcPrimaryButton text="发送" :loading="commenting" @tap="handleComment" />
          </view>

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
import { ref, computed } from 'vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import PcFormField from '@/components/PcFormField.vue'
import {
  getPostDetail, getPostComments,
  createComment, likePost, unlikePost, favoritePost, unfavoritePost,
} from '@/api/community'
import { useUserStore } from '@/store/user'
import type { PostDetail, CommentItem } from '@/types/community'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const post = ref<PostDetail | null>(null)
const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const comments = ref<CommentItem[]>([])
const commentsStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')

const hasLiked = ref(false)
const hasFavorited = ref(false)
const commentInput = ref('')
const commenting = ref(false)

async function loadDetail() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const id = currentPage?.options?.id

  if (!id) { pageStatus.value = 'empty'; return }

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

async function handleLike() {
  if (!isLoggedIn.value || !post.value) {
    uni.showToast({ title: '请先登录', icon: 'none' }); return
  }
  const id = post.value.id
  if (hasLiked.value) {
    await unlikePost(id)
    hasLiked.value = false
    if (post.value) post.value.likeCount--
  } else {
    const res = await likePost(id)
    if (res.success) {
      hasLiked.value = true
      if (post.value) post.value.likeCount++
    }
  }
}

async function handleFavorite() {
  if (!isLoggedIn.value || !post.value) {
    uni.showToast({ title: '请先登录', icon: 'none' }); return
  }
  const id = post.value.id
  if (hasFavorited.value) {
    await unfavoritePost(id)
    hasFavorited.value = false
    uni.showToast({ title: '已取消收藏', icon: 'none' })
  } else {
    const res = await favoritePost(id)
    if (res.success) {
      hasFavorited.value = true
      uni.showToast({ title: '已收藏', icon: 'success' })
    }
  }
}

async function handleComment() {
  if (!isLoggedIn.value || !post.value) {
    uni.showToast({ title: '请先登录', icon: 'none' }); return
  }
  if (!commentInput.value.trim()) return

  commenting.value = true
  const res = await createComment(post.value.id, commentInput.value)
  commenting.value = false

  if (res.success) {
    commentInput.value = ''
    uni.showToast({ title: '评论成功', icon: 'success' })
    await loadDetail()
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

.community-detail__actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--pc-user-line);
}

.community-detail__action-btn {
  padding: 6px 16px;
  border-radius: 16px;
  background: var(--pc-user-soft);
  font-size: var(--pc-font-body);
  color: var(--pc-user-primary);
}

.community-detail__comment-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
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
