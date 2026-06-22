/**
 * Cart API. Requires authentication.
 */

import { http } from './request'
import type { ApiResponse } from '@/types/api'
import type { CartItem } from '@/types/product'

/** List cart items */
export function getCartItems(): Promise<ApiResponse<CartItem[]>> {
  return http.get<CartItem[]>('/api/v1/cart-items')
}

/** Add product to cart */
export function addCartItem(productId: string, quantity: number = 1): Promise<ApiResponse<CartItem>> {
  return http.post<CartItem>('/api/v1/cart-items', { productId, quantity } as any)
}

/** Update cart item quantity */
export function updateCartItem(id: string, quantity: number): Promise<ApiResponse<CartItem>> {
  return http.put<CartItem>(`/api/v1/cart-items/${id}`, { quantity } as any)
}

/** Remove cart item */
export function deleteCartItem(id: string): Promise<ApiResponse<void>> {
  return http.delete<void>(`/api/v1/cart-items/${id}`)
}

/** Check/uncheck cart items (controls which items are included in checkout) */
export function checkCartItems(cartItemIds: string[], checked: boolean): Promise<ApiResponse<CartItem[]>> {
  return http.post<CartItem[]>('/api/v1/cart-items/check', { cartItemIds, checked } as any)
}
