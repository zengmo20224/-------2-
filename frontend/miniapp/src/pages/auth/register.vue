<template>
  <view class="pc-page auth-page">
    <PcPageHeader title="注册" />

    <view class="auth-form">
      <PcFormField label="手机号">
        <input class="pc-input" type="text" v-model="form.phone" placeholder="请输入手机号" />
      </PcFormField>
      <PcFormField label="密码">
        <input class="pc-input" type="text" v-model="form.password" placeholder="请输入密码" password />
      </PcFormField>
      <PcFormField label="昵称">
        <input class="pc-input" type="text" v-model="form.nickname" placeholder="给自己起个昵称" />
      </PcFormField>

      <!-- Security Questions -->
      <text class="auth-section-title">安全问题（用于找回密码，请至少选择 2 个）</text>
      <view v-for="(sq, index) in form.securityQuestions" :key="index" class="auth-sq-item">
        <PcFormField :label="`问题 ${index + 1}`">
          <view class="pc-select-wrap">
            <select class="pc-select" @change="onQuestionChange($event, index)">
              <option value="">请选择安全问题</option>
              <option
                v-for="q in presetQuestions"
                :key="q.index"
                :value="q.index"
                :disabled="isQuestionUsed(q.index, index)"
                :class="{ 'pc-option-disabled': isQuestionUsed(q.index, index) }"
              >
                {{ q.text }}{{ isQuestionUsed(q.index, index) ? '（已选）' : '' }}
              </option>
            </select>
          </view>
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
import { ref, onMounted } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcFormField from '@/components/PcFormField.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import { register, getPresetSecurityQuestions } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const presetQuestions = ref<{ index: number; text: string }[]>([])

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
    presetQuestions.value = res.data.map((text, index) => ({ index, text }))
  }
})

function isQuestionUsed(qIndex: number, currentRow: number): boolean {
  return form.value.securityQuestions.some(
    (sq, i) => i !== currentRow && sq.questionIndex === qIndex
  )
}

function onQuestionChange(e: any, index: number) {
  const picked = parseInt(e.target.value)
  if (isNaN(picked)) {
    form.value.securityQuestions[index].questionIndex = null
    form.value.securityQuestions[index].questionText = ''
    return
  }
  const found = presetQuestions.value.find(q => q.index === picked)
  if (!found) return
  if (isQuestionUsed(picked, index)) {
    uni.showToast({ title: '该问题已选择，不能重复', icon: 'none' })
    return
  }
  form.value.securityQuestions[index].questionIndex = picked
  form.value.securityQuestions[index].questionText = found.text
}

async function handleRegister() {
  if (!form.value.phone || !form.value.password || !form.value.nickname) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  if (form.value.password.length < 8) {
    uni.showToast({ title: '密码至少 8 位', icon: 'none' })
    return
  }
  if (form.value.password.length > 32) {
    uni.showToast({ title: '密码不能超过 32 位', icon: 'none' })
    return
  }
  if (!/(?=.*[A-Za-z])(?=.*\d)/.test(form.value.password)) {
    uni.showToast({ title: '密码必须包含数字和字母', icon: 'none' })
    return
  }

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
  const answers = validQuestions.map(sq => sq.answer.trim().toLowerCase())
  if (new Set(answers).size !== answers.length) {
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

.pc-select-wrap {
  position: relative;
}

.pc-select {
  width: 100%;
  height: 44px;
  border: 1px solid var(--pc-user-line);
  border-radius: 12px;
  padding: 0 14px;
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
  background: #fff;
  appearance: auto;
  -webkit-appearance: auto;
}

.pc-option-disabled {
  color: var(--pc-user-muted);
}
</style>
