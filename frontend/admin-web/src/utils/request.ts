import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import type { ApiResponse } from '../types/api'
import { sanitizeErrorMessage } from './error-sanitizer'

const axiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

/** Inject admin JWT token into every request */
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/**
 * Response interceptor — unwraps AxiosResponse to ApiResponse<T>.
 * Uses `as never` to bypass strict AxiosResponse return type constraint.
 */
axiosInstance.interceptors.response.use(
  (response) => {
    const apiResponse = response.data as ApiResponse<unknown>

    if (apiResponse && apiResponse.success === false) {
      const errorMsg = sanitizeErrorMessage(apiResponse.error?.message || '请求失败')
      ElMessage.error(errorMsg)
      return Promise.reject(new Error(errorMsg)) as never
    }

    return apiResponse as never
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      const msg = sanitizeErrorMessage(data?.error?.message || data?.message || '')

      switch (status) {
        case 401:
          localStorage.removeItem('admin_token')
          router.push('/login')
          ElMessage.warning('登录已过期，请重新登录')
          break
        case 403:
          router.push('/403')
          break
        case 409:
          ElMessage.warning(msg || '数据状态冲突，请刷新后重试')
          break
        case 422:
          ElMessage.warning(msg || '提交数据验证失败')
          break
        default:
          ElMessage.error(msg || '服务器错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  },
)

/**
 * Typed API client.
 * Because the response interceptor strips AxiosResponse and returns ApiResponse<T>,
 * these wrappers correctly resolve to Promise<ApiResponse<T>>.
 */
const request = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.get(url, config) as Promise<unknown> as Promise<ApiResponse<T>>
  },

  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.post(url, data, config) as Promise<unknown> as Promise<ApiResponse<T>>
  },

  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.put(url, data, config) as Promise<unknown> as Promise<ApiResponse<T>>
  },

  patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.patch(url, data, config) as Promise<unknown> as Promise<ApiResponse<T>>
  },

  delete<T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return axiosInstance.delete(url, config) as Promise<unknown> as Promise<ApiResponse<T>>
  },
}

export default request
