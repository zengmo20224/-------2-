/**
 * Product order API. Requires authentication.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { OrderItem, OrderDetail } from '@/types/product'

/** Create order from checked cart items */
export function createOrder(data: {
  storeId: string
  contactName: string
  contactPhone: string
  remark?: string
}): Promise<ApiResponse<OrderItem>> {
  return http.post<OrderItem>('/api/v1/product-orders', data as any)
}

/** List current user's orders */
export function getMyOrders(params?: PageParams): Promise<ApiResponse<PageResponse<OrderItem>>> {
  return http.get<PageResponse<OrderItem>>('/api/v1/product-orders/my', params as Record<string, unknown>)
}

/** Get order detail */
export function getOrderDetail(id: string): Promise<ApiResponse<OrderDetail>> {
  return http.get<OrderDetail>(`/api/v1/product-orders/${id}`)
}

/** Cancel an order */
export function cancelOrder(id: string): Promise<ApiResponse<OrderItem>> {
  return http.post<OrderItem>(`/api/v1/product-orders/${id}/cancel`)
}
