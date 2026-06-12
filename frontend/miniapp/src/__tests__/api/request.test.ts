import { describe, it, expect } from 'vitest'
import type { ApiResponse, PageResponse, PageParams, ApiError, ApiFieldError } from '@/types/api'

describe('API Types Contract', () => {
  it('ApiResponse<T> structure matches backend', () => {
    const response: ApiResponse<string> = {
      success: true,
      data: 'test',
    }
    expect(response.success).toBe(true)
    expect(response.data).toBe('test')
  })

  it('ApiResponse<T> with error structure matches backend', () => {
    const response: ApiResponse<never> = {
      success: false,
      error: {
        code: 'NOT_FOUND',
        message: '资源不存在',
      },
    }
    expect(response.success).toBe(false)
    expect(response.error?.code).toBe('NOT_FOUND')
  })

  it('ApiResponse<T> with field errors', () => {
    const fieldErrors: ApiFieldError[] = [
      { field: 'name', message: '名称不能为空' },
    ]
    const error: ApiError = {
      code: 'VALIDATION_ERROR',
      message: '验证失败',
      details: fieldErrors,
    }
    expect(error.details).toHaveLength(1)
    expect(error.details?.[0].field).toBe('name')
  })

  it('PageResponse<T> structure matches backend', () => {
    const page: PageResponse<string> = {
      items: ['a', 'b'],
      total: 10,
      page: 1,
      size: 2,
      totalPages: 5,
    }
    expect(page.items).toHaveLength(2)
    expect(page.totalPages).toBe(5)
  })

  it('PageParams has optional page and size', () => {
    const params1: PageParams = {}
    const params2: PageParams = { page: 1, size: 20 }
    expect(params1.page).toBeUndefined()
    expect(params2.page).toBe(1)
  })
})
