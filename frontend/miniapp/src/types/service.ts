/**
 * Service-related types for user-facing miniapp.
 * Backend source: com.petcare.service.dto.*
 */

/** Service mode: in-store or home visit */
export type ServiceMode = 'STORE' | 'HOME' | 'BOTH'

/** Service item as shown in list/detail */
export interface ServiceItem {
  id: string
  name: string
  description?: string
  mode: ServiceMode
  durationMinutes: number
  petTypes?: string[]
  petSizes?: string[]
  priceMin?: number
  priceMax?: number
  imageUrl?: string
  status: string
}

/** Service category for grouping */
export interface ServiceCategory {
  id: string
  name: string
  iconUrl?: string
  sortIndex: number
}
