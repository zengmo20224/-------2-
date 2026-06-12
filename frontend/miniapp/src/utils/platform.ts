/**
 * Platform adaptation utilities for miniapp.
 */

/** Check if current platform is WeChat mini program */
export function isWeixinMiniProgram(): boolean {
  // #ifdef MP-WEIXIN
  return true
  // #endif
  // #ifndef MP-WEIXIN
  return false
  // #endif
}

/** Get system info (cached) */
let systemInfoCache: UniApp.GetSystemInfoResult | null = null

export function getSystemInfo(): UniApp.GetSystemInfoResult {
  if (!systemInfoCache) {
    systemInfoCache = uni.getSystemInfoSync()
  }
  return systemInfoCache
}

/** Get status bar height for custom navigation */
export function getStatusBarHeight(): number {
  return getSystemInfo().statusBarHeight ?? 0
}

/** Navigate to a page */
export function navigateTo(url: string): void {
  uni.navigateTo({ url })
}

/** Navigate back */
export function navigateBack(delta = 1): void {
  uni.navigateBack({ delta })
}

/** Switch to a tab page */
export function switchTab(url: string): void {
  uni.switchTab({ url })
}

/** Make a phone call */
export function makePhoneCall(phoneNumber: string): void {
  uni.makePhoneCall({ phoneNumber })
}
