/**
 * User store — authentication boundary.
 *
 * WeChat login deferred per H5-first strategy.
 * This store manages token persistence for user JWT.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(loadToken())
  const isLoggedIn = computed(() => !!token)

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

  function logout(): void {
    setToken(null)
  }

  return { token, isLoggedIn, isWechatLoginEnabled, setToken, logout }
})
