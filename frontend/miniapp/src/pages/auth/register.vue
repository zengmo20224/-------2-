<template>
  <view class="pc-page auth-page">
    <PcPageHeader title="注册" />

    <view class="auth-form">
      <PcFormField label="手机号">
        <input class="pc-input" type="text" v-model="form.phone" placeholder="请输入手机号" />
      </PcFormField>
      <PcFormField label="密码">
        <input class="pc-input" type="text" v-model="form.password" placeholder="6-32位密码" password />
      </PcFormField>
      <PcFormField label="昵称">
        <input class="pc-input" type="text" v-model="form.nickname" placeholder="给自己起个昵称" />
      </PcFormField>

      <!-- Security Questions -->
      <text class="auth-section-title">安全问题（用于找回密码，请至少选择 2 个）</text>
      <view v-for="(sq, index) in form.securityQuestions" :key="index" class="auth-sq-item">
        <PcFormField :label="`问题 ${index + 1}`">
          <picker class="pc-picker" :range="availableQuestionTexts(index)" @change="onQuestionChange($event, index)">
            <view class="pc-picker-text" :class="{ 'pc-picker-text--placeholder': !sq.questionText }">
              {{ sq.questionText || '请选择安全问题' }}
            </view>
          </picker>
        </PcFormField>
        <PcFormField label="答案">
          <input class="pc-input" type="text" v-model="sq.answer" placeholder="输入答案" />
        </PcFormField>
      </view>

      <PcPrimaryButton text="注册" :loading="loading" @tap="handleRegister" />
      <view class="auth-back" @tap="goLogin">
        <text class="auth-link">已有账号？去登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcFormField from '@/components/PcFormField.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import { register, getPresetSecurityQuestions } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const presetQuestions = ref<string[]>([])

interface SecurityQuestionForm {
  questionIndex: number | null
  questionText: string
  answer: string
}

const form = ref({
  phone: '',
  password: '',
  nickname: '',
  securityQuestions: [
    { questionIndex: null, questionText: '', answer: '' } as SecurityQuestionForm,
    { questionIndex: null, questionText: '', answer: '' } as SecurityQuestionForm,
  ],
})

onMounted(async () => {
  const res = await getPresetSecurityQuestions()
  if (res.success && res.data) {
    presetQuestions.value = res.data
  }
})

/** For each picker: show all questions except those already chosen by other rows */
function availableQuestionTexts(currentIndex: number): string[] {
  const usedIndices = new Set<number>()
  form.value.securityQuestions.forEach((sq, i) => {
    if (i !== currentIndex && sq.questionIndex !== null) {
      usedIndices.add(sq.questionIndex)
    }
  })
  return presetQuestions.value.map((_, idx) => {
    if (usedIndices.has(idx)) return '— 已选择 —'
    return presetQuestions.value[idx]
  })
}

function onQuestionChange(e: any, index: number) {
  const picked = parseInt(e.detail.value)
  const realText = presetQuestions.value[picked]
  // Skip if it's the "already selected" placeholder
  if (realText === undefined) return
  // Check if this index is used by another row
  const isUsed = form.value.securityQuestions.some(
    (sq, i) => i !== index && sq.questionIndex === picked
  )
  if (isUsed) {
    uni.showToast({ title: '该问题已选择，不能重复', icon: 'none' })
    return
  }
  form.value.securityQuestions[index].questionIndex = picked
  form.value.securityQuestions[index].questionText = realText
}

async function handleRegister() {
  if (!form.value.phone || !form.value.password || !form.value.nickname) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (form.value.password.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }

  // Validate security questions
  const validQuestions = form.value.securityQuestions.filter(sq => sq.questionIndex !== null)
  if (validQuestions.length < 2) {
    uni.showToast({ title: '请至少选择 2 个安全问题', icon: 'none' })
    return
  }
  for (const sq of validQuestions) {
    if (!sq.answer.trim()) {
      uni.showToast({ title: '请填写所有安全问题的答案', icon: 'none' })
      return
    }
  }
  // Check no duplicate answers across questions
  const answers = validQuestions.map(sq => sq.answer.trim().toLowerCase())
  const uniqueAnswers = new Set(answers)
  if (uniqueAnswers.size !== answers.length) {
    uni.showToast({ title: '不同问题的答案不能相同', icon: 'none' })
    return
  }

  loading.value = true
  const res = await register({
    phone: form.value.phone,
    password: form.value.password,
    nickname: form.value.nickname,
    securityQuestions: validQuestions.map(sq => ({
      questionIndex: sq.questionIndex!,
      answer: sq.answer,
    })),
  })
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

.pc-input {
  height: 44px;
  border: 1px solid var(--pc-user-line);
  border-radius: 12px;
  padding: 0 14px;
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
  background: #fff;
}

.pc-picker {
  height: 44px;
  display: flex;
  align-items: center;
}

.pc-picker-text {
  height: 44px;
  line-height: 44px;
  border: 1px solid var(--pc-user-line);
  border-radius: 12px;
  padding: 0 14px;
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
  background: #fff;
}

.pc-picker-text--placeholder {
  color: var(--pc-user-muted);
}
</style>
