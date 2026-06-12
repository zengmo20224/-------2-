import { describe, it, expect } from 'vitest'
import { sanitizeErrorMessage } from '@/utils/error-sanitizer'

describe('sanitizeErrorMessage', () => {
  it('returns generic message for non-string input', () => {
    expect(sanitizeErrorMessage(null)).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage(undefined)).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage(123)).toBe('操作失败，请稍后重试')
  })

  it('returns generic message for empty string', () => {
    expect(sanitizeErrorMessage('')).toBe('操作失败，请稍后重试')
  })

  it('returns generic message for overly long strings', () => {
    const longMsg = 'a'.repeat(201)
    expect(sanitizeErrorMessage(longMsg)).toBe('操作失败，请稍后重试')
  })

  it('passes through safe messages', () => {
    expect(sanitizeErrorMessage('服务不存在')).toBe('服务不存在')
    expect(sanitizeErrorMessage('预约已取消')).toBe('预约已取消')
  })

  it('filters SQL statements', () => {
    expect(sanitizeErrorMessage('SELECT * FROM users')).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage('INSERT INTO bookings')).toBe('操作失败，请稍后重试')
  })

  it('filters Java stack traces', () => {
    expect(sanitizeErrorMessage('java.lang.NullPointerException')).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage('at com.petcare.service.impl')).toBe('操作失败，请稍后重试')
  })

  it('filters API keys', () => {
    expect(sanitizeErrorMessage('api_key=abc123')).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage('sk-abc123def')).toBe('操作失败，请稍后重试')
  })

  it('filters provider names', () => {
    expect(sanitizeErrorMessage('DeepSeek error occurred')).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage('OpenAI API failed')).toBe('操作失败，请稍后重试')
  })

  it('filters miniapp-specific sensitive data', () => {
    expect(sanitizeErrorMessage('openid=xxx')).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage('session_key leaked')).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage('access_token expired')).toBe('操作失败，请稍后重试')
  })

  it('filters password references', () => {
    expect(sanitizeErrorMessage('password mismatch')).toBe('操作失败，请稍后重试')
    expect(sanitizeErrorMessage('credential invalid')).toBe('操作失败，请稍后重试')
  })
})
