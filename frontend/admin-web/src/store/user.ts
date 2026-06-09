import { defineStore } from 'pinia'
import { login, getUserInfo } from '../api/auth'
import type { LoginParams, AdminUserInfo } from '../api/auth'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('admin_token'))
  const userInfo = ref<AdminUserInfo | null>(null)

  const loginAction = async (data: LoginParams) => {
    const res = await login(data)
    if (res.data?.accessToken) {
      token.value = res.data.accessToken
      localStorage.setItem('admin_token', res.data.accessToken)
    }
  }

  const getInfoAction = async () => {
    const res = await getUserInfo()
    if (res.data) {
      userInfo.value = res.data
    }
  }

  const logoutAction = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('admin_token')
  }

  /** Check if current user has a specific permission code */
  const hasPermission = (perm: string): boolean => {
    return userInfo.value?.permissions?.includes(perm) ?? false
  }

  return { token, userInfo, loginAction, getInfoAction, logoutAction, hasPermission }
})
