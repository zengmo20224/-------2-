/**
 * Store API. Public (anonymous) for browsing pickup stores.
 */

import { http } from './request'
import type { ApiResponse } from '@/types/api'
import type { StoreItem } from '@/types/store'

/** List open stores (for pickup store selection) */
export function getStores(): Promise<ApiResponse<StoreItem[]>> {
  return http.get<StoreItem[]>('/api/v1/stores')
}
