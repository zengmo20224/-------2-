/**
 * Product API.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { ProductItem } from '@/types/product'

/** List products (paginated) */
export function getProducts(params?: PageParams): Promise<ApiResponse<PageResponse<ProductItem>>> {
  return http.get<PageResponse<ProductItem>>('/api/user/products', params as Record<string, unknown>)
}

/** Get product detail */
export function getProductDetail(id: string): Promise<ApiResponse<ProductItem>> {
  return http.get<ProductItem>(`/api/user/products/${id}`)
}
