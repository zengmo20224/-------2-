/**
 * Product API.
 */

import { http } from './request'
import type { ApiResponse, PageResponse, PageParams } from '@/types/api'
import type { ProductItem, ProductDetail, ProductCategory } from '@/types/product'

/** List products (paginated) */
export function getProducts(params?: PageParams & {
  categoryId?: string
  keyword?: string
}): Promise<ApiResponse<PageResponse<ProductItem>>> {
  return http.get<PageResponse<ProductItem>>('/api/v1/products', params as Record<string, unknown>)
}

/** Get product detail */
export function getProductDetail(id: string): Promise<ApiResponse<ProductDetail>> {
  return http.get<ProductDetail>(`/api/v1/products/${id}`)
}

/** List product categories */
export function getProductCategories(): Promise<ApiResponse<ProductCategory[]>> {
  return http.get<ProductCategory[]>('/api/v1/product-categories')
}
