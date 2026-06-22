<template>
  <view class="pc-page auth-page">
    <PcPageHeader title="找回密码" />

    <!-- Step 1: Enter phone -->
    <view v-if="step === 1" class="auth-form">
      <PcFormField label="手机号">
        <input class="pc-input" type="text" v-model="phoneInput" placeholder="请输入注册手机号" />
      </PcFormField>
      <PcPrimaryButton text="获取安全问题" :loading="loading" @tap="handleGetQuestions" />
    </view>

    <!-- Step 2: Answer questions + set new password -->
    <view v-else-if="step === 2" class="auth-form">
      <text class="auth-step-label">请回答安全问题</text>
      <view v-for="q in questions" :key="q.id" class="auth-sq-item">
        <text class="auth-sq-question">{{ q.question }}</text>
        <PcFormField label="答案">
          <input class="pc-input" type="text" v-model="answers[q.id]" placeholder="请输入答案" />
        </PcFormField>
      </view>

      <PcFormField label="新密码">
        <input class="pc-input" type="text" v-model="newPassword" placeholder="请输入新密码" password />
      </PcFormField>
      <PcPrimaryButton text="重置密码" :loading="loading" @tap="handleReset" />
    </view>

    <!-- Step 3: Success -->
    <view v-else-if="step === 3" class="auth-success">
      <text class="auth-success-text">密码已重置，请使用新密码登录</text>
      <PcPrimaryButton text="去登录" @tap="goLogin" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcFormField from '@/components/PcFormField.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import { getSecurityQuestions, resetPassword, type SecurityQuestion } from '@/api/user'

const step = ref(1)
const loading = ref(false)
const phoneInput = ref('')
const questions = ref<SecurityQuestion[]>([])
const answers = ref<Record<string, string>>({})
const newPassword = ref('')

async function handleGetQuestions() {
  if (!phoneInput.value) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }

  loading.value = true
  const res = await getSecurityQuestions(phoneInput.value)
  loading.value = false

  if (res.success && res.data) {
    questions.value = res.data
    answers.value = {}
    step.value = 2
  }
}

async function handleReset() {
  if (!newPassword.value || newPassword.value.length < 8) {
    uni.showToast({ title: '密码至少 8 位', icon: 'none' })
    return
  }
  if (newPassword.value.length > 32) {
    uni.showToast({ title: '密码不能超过 32 位', icon: 'none' })
    return
  }
  if (!/(?=.*[A-Za-z])(?=.*\d)/.test(newPassword.value)) {
    uni.showToast({ title: '密码必须包含数字和字母', icon: 'none' })
    return
  }
  for (const q of questions.value) {
    if (!answers.value[q.id]) {
      uni.showToast({ title: '请回答所有问题', icon: 'none' })
      return
    }
  }

  loading.value = true
  const res = await resetPassword({
    phone: phoneInput.value,
    answers: questions.value.map(q => ({
      questionId: q.id,
      answer: answers.value[q.id],
    })),
    newPassword: newPassword.value,
  })
  loading.value = false

  if (res.success) {
    step.value = 3
  }
}

function goLogin() {
  uni.redirectTo({ url: '/pages/auth/login' })
}
</script>

<style scoped>
.auth-page {
  padding: 20px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
  padding: 20px;
  border: 1px solid #DCEBE7;
  border-radius: 24px;
  background: #FFFFFF;
  box-shadow: 0 12px 32px rgba(25, 50, 46, 0.09);
}

.auth-step-label {
  font-size: 16px;
  font-weight: 700;
  color: #19322E;
}

.auth-sq-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: #FFF7E6;
  background: #FAF8F3;
  border-radius: 16px;
  padding: 12px;
}

.auth-sq-question {
  font-size: 14px;
  font-weight: 600;
  color: #19322E;
}

.auth-success {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding-top: 48px;
}

.auth-success-text {
  font-size: 16px;
  color: #19322E;
  text-align: center;
}

.pc-input {
  height: 44px;
  border: 1px solid #E2E9E6;
  border: 1px solid #E2E9E6;
  border-radius: 12px;
  padding: 0 14px;
  font-size: 14px;
  color: #19322E;
  background: #fff;
}
</style>
