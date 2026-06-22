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
          <!-- Author -->
          <view class="community-detail__author">
            <view class="community-detail__author-avatar">
              <image v-if="post.authorAvatar" class="community-detail__author-img" :src="fullUrl(post.authorAvatar)" mode="aspectFill" />
              <text v-else class="community-detail__author-initial">{{ (post.authorName || '?').charAt(0) }}</text>
            </view>
            <text class="community-detail__author-name">{{ post.authorName || '匿名用户' }}</text>
          </view>

          <text class="community-detail__title">{{ post.title }}</text>
          <text class="community-detail__content" decode>{{ post.content }}</text>
          <view v-if="post.imageUrls && post.imageUrls.length > 0" class="community-detail__images">
            <image
              v-for="(url, index) in post.imageUrls"
              :key="index"
              class="community-detail__image"
              :src="fullUrl(url)"
              mode="aspectFill"
              @tap="previewImage(index)"
            />
          </view>

          <!-- Tags -->
          <view v-if="post.tags && post.tags.length > 0" class="community-detail__tags">
            <text
              v-for="tag in post.tags"
              :key="tag"
              class="community-detail__tag"
              @tap="goTag(tag)"
            >#{{ tag }}</text>
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
          <text class="community-detail__comments-title">评论 ({{ post.commentCount }})</text>

          <!-- Comment Input -->
          <view v-if="isLoggedIn" class="community-detail__comment-input">
            <input
              class="community-detail__input"
              type="text"
              v-model="commentInput"
              :placeholder="replyPlaceholder"
              @confirm="handleComment"
            />
            <view v-if="replyTo" class="community-detail__cancel-reply" @tap="cancelReply">
              <text class="community-detail__cancel-reply-text">取消</text>
            </view>
            <view class="community-detail__send-btn" @tap="handleComment">
              <text class="community-detail__send-btn-text">{{ commenting ? '...' : '发送' }}</text>
            </view>
          </view>

          <PcStatePanel :status="commentsStatus" empty-text="暂无评论，快来抢沙发~">
            <view class="community-detail__comment-list">
              <!-- Top-level comment = "楼层" -->
              <view v-for="(comment, index) in comments" :key="comment.id" class="comment-thread">
                <!-- Floor header -->
                <view class="comment-thread__floor">
                  <view class="comment-thread__author">
                    <view class="comment-thread__author-avatar">
                      <image v-if="comment.authorAvatar" class="comment-thread__author-img" :src="fullUrl(comment.authorAvatar)" mode="aspectFill" />
                      <text v-else class="comment-thread__author-initial">{{ (comment.authorName || '?').charAt(0) }}</text>
                    </view>
                    <text class="comment-thread__author-name">{{ comment.authorName || '匿名用户' }}</text>
                  </view>
                  <text class="comment-thread__floor-time">{{ formatTime(comment.createTime) }}</text>
                </view>

                <!-- Main comment content -->
                <view class="comment-thread__body">
                  <text class="comment-thread__content">{{ comment.content }}</text>
                </view>

                <!-- Comment actions -->
                <view class="comment-thread__actions">
                  <view class="comment-thread__action" @tap="handleLikeComment(comment)">
                    <text>{{ likedCommentIds.has(comment.id) ? '❤️' : '🤍' }} {{ comment.likeCount }}</text>
                  </view>
                  <view v-if="isLoggedIn" class="comment-thread__action" @tap="startReply(comment, index)">
                    <text>回复</text>
                  </view>
                  <view v-if="isLoggedIn" class="comment-thread__action comment-thread__action--danger" @tap="handleDeleteComment(comment.id)">
                    <text>删除</text>
                  </view>
                </view>

                <!-- Expand/collapse replies toggle -->
                <view
                  v-if="comment.replies && comment.replies.length > 0"
                  class="comment-thread__toggle"
                  @tap="toggleThread(comment.id)"
                >
                  <text class="comment-thread__toggle-text">
                    {{ expandedThreads.has(comment.id) ? '收起回复' : `展开 ${comment.replies.length} 条回复` }}
                  </text>
                  <text class="comment-thread__toggle-arrow">{{ expandedThreads.has(comment.id) ? '▲' : '▼' }}</text>
                </view>

                <!-- Replies collection (expand/collapse) -->
                <view
                  v-if="comment.replies && comment.replies.length > 0 && expandedThreads.has(comment.id)"
                  class="comment-thread__replies"
                >
                  <view v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <view class="reply-item__header">
                      <view class="reply-item__author">
                        <view class="reply-item__author-avatar">
                          <image v-if="reply.authorAvatar" class="reply-item__author-img" :src="fullUrl(reply.authorAvatar)" mode="aspectFill" />
                          <text v-else class="reply-item__author-initial">{{ (reply.authorName || '?').charAt(0) }}</text>
                        </view>
                        <text class="reply-item__author-name">{{ reply.authorName || '匿名用户' }}</text>
                      </view>
                      <text class="reply-item__content">{{ reply.content }}</text>
                    </view>
                    <view class="reply-item__footer">
                      <text class="reply-item__time">{{ formatTime(reply.createTime) }}</text>
                      <view class="reply-item__actions">
                        <view class="reply-item__action" @tap="handleLikeComment(reply)">
                          <text>{{ likedCommentIds.has(reply.id) ? '❤️' : '🤍' }} {{ reply.likeCount }}</text>
                        </view>
                        <view v-if="isLoggedIn" class="reply-item__action" @tap="startReply(reply, index)">
                          <text>回复</text>
                        </view>
                        <view v-if="isLoggedIn" class="reply-item__action reply-item__action--danger" @tap="handleDeleteComment(reply.id)">
                          <text>删除</text>
                        </view>
                      </view>
                    </view>
                  </view>
                </view>
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
import { onLoad } from '@dcloudio/uni-app'
import PcStatePanel from '@/components/PcStatePanel.vue'
import {
  getPostDetail, getPostComments,
  createComment, deleteComment, likeComment, unlikeComment,
  likePost, unlikePost, favoritePost, unfavoritePost,
} from '@/api/community'
import { useUserStore } from '@/store/user'
import type { PostDetail, CommentTreeNode } from '@/types/community'
import { normalizeRouteParam } from '@/utils/route-query'
import { openCommunityTag } from '@/utils/community-navigation'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const post = ref<PostDetail | null>(null)
const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const comments = ref<CommentTreeNode[]>([])
const commentsStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')

const hasLiked = ref(false)
const hasFavorited = ref(false)
const commentInput = ref('')
const commenting = ref(false)
const replyTo = ref<CommentTreeNode | null>(null)
const likedCommentIds = ref<Set<string>>(new Set())
const expandedThreads = ref<Set<string>>(new Set())
const currentPostId = ref('')

/** Placeholder reflects whom the user is replying to, by name (not floor number). */
const replyPlaceholder = computed(() => {
  if (!replyTo.value) return '写下你的评论...'
  const name = replyTo.value.authorName || '匿名用户'
  return `回复 @${name}...`
})

const API_BASE = import.meta.env.VITE_API_BASE_URL || ''

function fullUrl(url: string | null): string {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return API_BASE + url
}

/** Open the native full-screen image viewer with swipe + pinch-zoom. */
function previewImage(index: number) {
  if (!post.value?.imageUrls || post.value.imageUrls.length === 0) return
  const urls = post.value.imageUrls.map(u => fullUrl(u))
  uni.previewImage({
    current: urls[index],
    urls,
  })
}

async function loadDetail(routeId?: unknown) {
  const id = normalizeRouteParam(routeId ?? currentPostId.value)

  if (!id) {
    post.value = null
    comments.value = []
    pageStatus.value = 'empty'
    commentsStatus.value = 'empty'
    return
  }

  currentPostId.value = id
  pageStatus.value = 'loading'
  commentsStatus.value = 'loading'

  const [postRes, commentRes] = await Promise.all([
    getPostDetail(id),
    getPostComments(id),
  ])

  if (!postRes.success || !postRes.data) {
    post.value = null
    comments.value = []
    pageStatus.value = 'error'
    commentsStatus.value = 'empty'
    return
  }

  post.value = postRes.data
  pageStatus.value = 'success'

  if (commentRes.success && commentRes.data) {
    comments.value = commentRes.data
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

function startReply(comment: CommentTreeNode, index: number) {
  replyTo.value = comment
  commentInput.value = ''
  // Auto-expand the thread so the user sees context
  expandedThreads.value.add(comment.id)
}

function cancelReply() {
  replyTo.value = null
  commentInput.value = ''
}

function toggleThread(commentId: string) {
  if (expandedThreads.value.has(commentId)) {
    expandedThreads.value.delete(commentId)
  } else {
    expandedThreads.value.add(commentId)
  }
}

async function handleComment() {
  if (!isLoggedIn.value || !post.value) {
    uni.showToast({ title: '请先登录', icon: 'none' }); return
  }
  if (!commentInput.value.trim()) return

  commenting.value = true
  const parentId = replyTo.value ? replyTo.value.id : undefined
  const res = await createComment(post.value.id, commentInput.value, parentId)
  commenting.value = false

  if (res.success) {
    commentInput.value = ''
    const repliedId = replyTo.value?.id
    replyTo.value = null
    uni.showToast({ title: '评论成功', icon: 'success' })
    await loadDetail()
    // Auto-expand the thread after replying
    if (repliedId) {
      expandedThreads.value.add(repliedId)
    }
  }
}

async function handleLikeComment(comment: CommentTreeNode) {
  if (!isLoggedIn.value || !post.value) {
    uni.showToast({ title: '请先登录', icon: 'none' }); return
  }
  if (likedCommentIds.value.has(comment.id)) {
    await unlikeComment(post.value.id, comment.id)
    likedCommentIds.value.delete(comment.id)
    comment.likeCount = Math.max(0, comment.likeCount - 1)
  } else {
    const res = await likeComment(post.value.id, comment.id)
    if (res.success) {
      likedCommentIds.value.add(comment.id)
      comment.likeCount++
    }
  }
}

async function handleDeleteComment(commentId: string) {
  if (!post.value) return
  uni.showModal({
    title: '确认删除',
    content: '确定删除这条评论吗？',
    success: async (res) => {
      if (!res.confirm || !post.value) return
      const deleteRes = await deleteComment(post.value.id, commentId)
      if (deleteRes.success) {
        uni.showToast({ title: '已删除', icon: 'success' })
        await loadDetail()
      }
    },
  })
}

function formatTime(time: string): string {
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

onLoad((query) => {
  loadDetail(query?.id)
})

function goTag(tag: string) {
  openCommunityTag(tag)
}
</script>

<style scoped>
.community-detail {
  padding: 20px;
}

.community-detail__card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
  margin-bottom: 16px;
}

.community-detail__title {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #19322E;
  margin-bottom: 12px;
}

.community-detail__content {
  display: block;
  width: 100%;
  font-size: 14px;
  color: #19322E;
  line-height: 1.8;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
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
  cursor: pointer;
}

.community-detail__image:active {
  opacity: 0.85;
}

.community-detail__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.community-detail__tag {
  font-size: 11px;
  color: #11796F;
  background: rgba(43, 122, 120, 0.08);
  padding: 4px 10px;
  border-radius: 6px;
}

.community-detail__meta {
  display: flex;
  gap: 16px;
}

.community-detail__stat {
  font-size: 11px;
  color: #71817D;
}

.community-detail__actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #E2E9E6;
}

.community-detail__action-btn {
  padding: 6px 16px;
  border-radius: 16px;
  background: #DFF2ED;
  font-size: 14px;
  color: #11796F;
}

/* Comments */
.community-detail__comments {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

/* Post author */
.community-detail__author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.community-detail__author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #DFF2ED;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.community-detail__author-img {
  width: 100%;
  height: 100%;
}

.community-detail__author-initial {
  font-size: 16px;
  color: #11796F;
  font-weight: 600;
}

.community-detail__author-name {
  font-size: 14px;
  font-weight: 600;
  color: #19322E;
}

.community-detail__comments-title {
  font-size: 16px;
  font-weight: 700;
  color: #19322E;
  margin-bottom: 12px;
}

.community-detail__comment-input {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}

.community-detail__input {
  flex: 1;
  height: 40px;
  border: 1px solid #E2E9E6;
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
  color: #19322E;
  background: #FAF8F3;
}

.community-detail__cancel-reply {
  padding: 4px 8px;
}

.community-detail__cancel-reply-text {
  font-size: 11px;
  color: #71817D;
}

.community-detail__send-btn {
  height: 40px;
  padding: 0 20px;
  border-radius: 20px;
  background: #11796F;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.community-detail__send-btn-text {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.community-detail__comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Thread = one top-level comment as a "floor" */
.comment-thread {
  background: #FAF8F3;
  border-radius: 12px;
  padding: 14px;
}

.comment-thread__floor {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-thread__author {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.comment-thread__author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #DFF2ED;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.comment-thread__author-img {
  width: 100%;
  height: 100%;
}

.comment-thread__author-initial {
  font-size: 11px;
  color: #11796F;
  font-weight: 600;
}

.comment-thread__author-name {
  font-size: 11px;
  font-weight: 600;
  color: #19322E;
}

.comment-thread__floor-time {
  font-size: 11px;
  color: #71817D;
}

.comment-thread__body {
  margin-bottom: 8px;
}

.comment-thread__content {
  font-size: 14px;
  color: #19322E;
  line-height: 1.6;
}

.comment-thread__actions {
  display: flex;
  gap: 16px;
  margin-bottom: 4px;
}

.comment-thread__action text {
  font-size: 11px;
  color: #71817D;
}

.comment-thread__action--danger text {
  color: #e65555;
}

/* Expand/collapse toggle */
.comment-thread__toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 4px 0;
}

.comment-thread__toggle-text {
  font-size: 11px;
  color: #11796F;
  font-weight: 600;
}

.comment-thread__toggle-arrow {
  font-size: 10px;
  color: #11796F;
}

/* Replies collection */
.comment-thread__replies {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reply-item {
  padding: 6px 0;
  border-bottom: 1px solid #E2E9E6;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-item__header {
  margin-bottom: 4px;
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.reply-item__author {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.reply-item__author-avatar {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #DFF2ED;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.reply-item__author-img {
  width: 100%;
  height: 100%;
}

.reply-item__author-initial {
  font-size: 9px;
  color: #11796F;
  font-weight: 600;
}

.reply-item__author-name {
  font-size: 11px;
  font-weight: 600;
  color: #19322E;
  white-space: nowrap;
}

.reply-item__content {
  font-size: 11px;
  color: #19322E;
  line-height: 1.5;
}

.reply-item__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.reply-item__time {
  font-size: 11px;
  color: #71817D;
}

.reply-item__actions {
  display: flex;
  gap: 12px;
}

.reply-item__action text {
  font-size: 11px;
  color: #71817D;
}

.reply-item__action--danger text {
  color: #e65555;
}
</style>
