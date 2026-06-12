<template>
  <div class="pc-login">
    <div class="pc-login__card">
      <div class="pc-login__header">
        <h1 class="pc-login__title">PetCare Admin</h1>
        <p class="pc-login__subtitle">管理后台</p>
      </div>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" size="large">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            class="pc-login__button"
            size="large"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../store/user'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { showError } from '../../utils/feedback'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
})

const loginRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
})

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.loginAction(loginForm)
      const rawRedirect = route.query.redirect as string
      const safeRedirect = rawRedirect && rawRedirect.startsWith('/') && !rawRedirect.startsWith('//') && !rawRedirect.includes('//') && !rawRedirect.includes(':')
        ? rawRedirect
        : '/'
      router.push(safeRedirect)
    } catch (error) {
      const msg = error instanceof Error ? error.message : '登录失败'
      showError(msg)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.pc-login {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--pc-primary-dark);
}

.pc-login__card {
  width: 400px;
  padding: var(--pc-spacing-xl) var(--pc-spacing-xl) var(--pc-spacing-lg);
  background: #fff;
  border-radius: var(--pc-radius-lg);
  box-shadow: var(--pc-shadow-lg);
}

.pc-login__header {
  text-align: center;
  margin-bottom: var(--pc-spacing-xl);
}

.pc-login__title {
  margin: 0 0 var(--pc-spacing-xs) 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--pc-primary);
}

.pc-login__subtitle {
  margin: 0;
  font-size: var(--pc-font-size-base);
  color: var(--pc-muted);
}

.pc-login__button {
  width: 100%;
}
</style>
