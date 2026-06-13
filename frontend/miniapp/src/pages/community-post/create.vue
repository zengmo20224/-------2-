<template>
  <view class="pc-page post-create">
    <PcPageHeader title="发布帖子" />

    <PcStatePanel v-if="!isLoggedIn" status="unauthorized" />

    <view v-else class="post-create__form">
      <PcFormField label="标题" placeholder="帖子标题（1-120字）" v-model="title" />
      <view class="post-create__content">
        <textarea
          class="post-create__textarea"
          placeholder="分享你和宠物的故事..."
          v-model="content"
          maxlength="5000"
        />
      </view>
      <view class="post-create__action">
        <PcPrimaryButton text="发布" :loading="submitting" @tap="handleSubmit" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import PcFormField from '@/components/PcFormField.vue'
import { createPost } from '@/api/community'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const title = ref('')
const content = ref('')
const submitting = ref(false)

async function handleSubmit() {
  if (!title.value.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' }); return
  }
  if (!content.value.trim()) {
    uni.showToast({ title: '请输入内容', icon: 'none' }); return
  }

  submitting.value = true
  const res = await createPost({
    title: title.value,
    content: content.value,
  })
  submitting.value = false

  if (res.success) {
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  }
}
</script>

<style scoped>
.post-create {
  padding: var(--pc-page-padding);
}

.post-create__form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-create__content {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 16px;
}

.post-create__textarea {
  width: 100%;
  min-height: 200px;
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
  line-height: 1.6;
  border: none;
  outline: none;
  resize: none;
}

.post-create__action {
  margin-top: 8px;
}
</style>
