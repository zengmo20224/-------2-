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
  startTime: string | null
  endTime: string | null
  productNames: string[]
  serviceNames: string[]
}
