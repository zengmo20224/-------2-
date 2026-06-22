/**
 * Marketing activity types for the user-facing H5 app.
 * Backend source: com.petcare.marketing.dto.MarketingActivityDtos.PublicActivitySummary
 */

/** Activity summary for public list/detail */
export interface ActivityItem {
  id: string
  title: string
  activityType: string
  description: string | null
  coverUrl: string | null
  startTime: string | null
  endTime: string | null
  products: ActivityProductCard[]
  services: ActivityServiceCard[]
  productNames: string[]
  serviceNames: string[]
}

export interface ActivityProductCard {
  id: string
  name: string
  coverUrl: string | null
  price: number
  salesCount: number | null
}

export interface ActivityServiceCard {
  id: string
  name: string
  coverUrl: string | null
  price: number
  durationMinutes: number
  serviceMode: string
}
