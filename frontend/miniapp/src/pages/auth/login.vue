<template>
  <view class="pc-page auth-page">
    <PcPageHeader title="登录" />

    <view class="auth-form">
      <PcFormField label="手机号">
        <input class="pc-input" type="text" v-model="phoneInput" placeholder="请输入手机号" />
      </PcFormField>
      <PcFormField label="密码">
        <input class="pc-input" type="text" v-model="passwordInput" placeholder="请输入密码" password />
      </PcFormField>

      <view class="auth-links">
        <text class="auth-link" @tap="goRegister">没有账号？去注册</text>
        <text class="auth-link" @tap="goForgotPassword">忘记密码</text>
      </view>

      <PcPrimaryButton text="登录" :loading="loginLoading" @tap="handleLogin" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcFormField from '@/components/PcFormField.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const phoneInput = ref('')
const passwordInput = ref('')
const loginLoading = ref(false)

async function handleLogin() {
  if (!phoneInput.value || !passwordInput.value) {
    uni.showToast({ title: '请填写手机号和密码', icon: 'none' })
    return
  }

  loginLoading.value = true
  const ok = await userStore.doLogin(phoneInput.value, passwordInput.value)
  loginLoading.value = false

  if (ok) {
    uni.showToast({ title: '登录成功', icon: 'success' })
    await userStore.fetchProfile()
    uni.switchTab({ url: '/pages/profile/index' })
  }
}

function goRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}

function goForgotPassword() {
  uni.navigateTo({ url: '/pages/auth/forgot-password' })
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

.auth-links {
  display: flex;
  justify-content: space-between;
}

.auth-link {
  font-size: 14px;
  color: #11796F;
  color: #11796F;
  font-weight: 700;
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
