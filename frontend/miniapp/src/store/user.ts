/**
 * User store — authentication boundary.
 *
 * WeChat login deferred per H5-first strategy.
 * This store manages token persistence for user JWT.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { testLogin as apiTestLogin, getUserProfile, type UserProfile } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(loadToken())
  const isLoggedIn = computed(() => !!token.value)
  const profile = ref<UserProfile | null>(null)

  /** WeChat login availability flag — deferred until H5 stabilizes */
  const isWechatLoginEnabled = ref(false)

  function loadToken(): string | null {
    try {
      return uni.getStorageSync('user_token') || null
    } catch {
      return null
    }
  }

  function setToken(newToken: string | null): void {
    token.value = newToken
    if (newToken) {
      try {
        uni.setStorageSync('user_token', newToken)
      } catch {
        // storage write failed — non-critical
      }
    } else {
      try {
        uni.removeStorageSync('user_token')
      } catch {
        // ignore
      }
    }
  }

  /** Attempt test login with phone number */
  async function doTestLogin(phone: string): Promise<boolean> {
    const res = await apiTestLogin(phone)
    if (res.success && res.data) {
      setToken(res.data.accessToken)
      return true
    }
    return false
  }

  /** Fetch current user profile */
  async function fetchProfile(): Promise<boolean> {
    if (!token.value) return false
    const res = await getUserProfile()
    if (res.success && res.data) {
      profile.value = res.data
      return true
    }
    return false
  }

  function logout(): void {
    setToken(null)
    profile.value = null
  }

  return { token, isLoggedIn, isWechatLoginEnabled, profile, setToken, doTestLogin, fetchProfile, logout }
})
