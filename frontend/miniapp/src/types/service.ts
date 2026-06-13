/**
 * Service-related types for the user-facing H5 app.
 * Backend source: com.petcare.service.dto.ServiceItemResponse / ServiceCategoryResponse
 * Field names match backend JSON exactly (snake_case not used — backend uses camelCase).
 */

/** Service mode: in-store or home visit */
export type ServiceMode = 'STORE' | 'HOME' | 'BOTH'

/** Service item as returned by backend API */
export interface ServiceItem {
  id: string
  categoryId: string
  name: string
  serviceMode: string
  price: number
  durationMinutes: number
  petType: string
  petSize: string
  needAddress: number
  needPet: number
  description: string | null
  coverUrl: string | null
}

/** Service category as returned by backend API */
export interface ServiceCategory {
  id: string
  name: string
  iconUrl: string | null
  sort: number
}
