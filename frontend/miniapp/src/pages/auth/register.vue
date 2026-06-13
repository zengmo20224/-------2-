<template>
  <view class="pc-page auth-page">
    <PcPageHeader title="注册" />

    <view class="auth-form">
      <PcFormField label="手机号" placeholder="请输入手机号" v-model="form.phone" />
      <PcFormField label="密码" placeholder="6-32位密码" v-model="form.password" />
      <PcFormField label="昵称" placeholder="给自己起个昵称" v-model="form.nickname" />

      <!-- Security Questions -->
      <text class="auth-section-title">安全问题（用于找回密码）</text>
      <view v-for="(sq, index) in form.securityQuestions" :key="index" class="auth-sq-item">
        <PcFormField :label="`问题${index + 1}`" placeholder="设置一个安全问题" v-model="sq.question" />
        <PcFormField label="答案" placeholder="安全问题答案" v-model="sq.answer" />
      </view>

      <PcPrimaryButton text="注册" :loading="loading" @tap="handleRegister" />
      <view class="auth-back" @tap="goLogin">
        <text class="auth-link">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcFormField from '@/components/PcFormField.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import { register } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)

const form = ref({
  phone: '',
  password: '',
  nickname: '',
  securityQuestions: [
    { question: '', answer: '' },
    { question: '', answer: '' },
  ],
})

async function handleRegister() {
  if (!form.value.phone || !form.value.password || !form.value.nickname) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (form.value.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  for (const sq of form.value.securityQuestions) {
    if (!sq.question || !sq.answer) {
      uni.showToast({ title: '请完成安全问题设置', icon: 'none' })
      return
    }
  }

  loading.value = true
  const res = await register(form.value)
  loading.value = false

  if (res.success && res.data) {
    userStore.setAuthToken(res.data.accessToken)
    uni.showToast({ title: '注册成功', icon: 'success' })
    await userStore.fetchProfile()
    uni.switchTab({ url: '/pages/profile/index' })
  }
}

function goLogin() {
  uni.navigateBack()
}
</script>

<style scoped>
.auth-page {
  padding: var(--pc-page-padding);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
}

.auth-section-title {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-ink);
  margin-top: 8px;
}

.auth-sq-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: var(--pc-user-cream);
  border-radius: var(--pc-radius-card);
  padding: 12px;
}

.auth-back {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

.auth-link {
  font-size: var(--pc-font-body);
  color: var(--pc-user-primary);
}
</style>
