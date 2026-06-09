import request from '../utils/request'
import type { PageResponse, PageParams } from '../types/api'

// ─── Types ───

export interface StaffMember {
  id: number
  storeId: number
  name: string
  phone: string
  avatarUrl: string
  role: string // GROOMER | WALKER | FEEDER | MANAGER
  status: string
  description: string
}

export interface StaffCreateParams {
  storeId: number
  name: string
  phone?: string
  avatarUrl?: string
  role: string
  description?: string
}

export interface StaffSkillView {
  staffId: number
  serviceCategoryIds: number[]
}

export interface StaffSkillUpdateParams {
  serviceCategoryIds: number[]
}

export interface StaffSchedule {
  id: number
  staffId: number
  storeId: number
  workDate: string
  startTime: string
  endTime: string
  status: string // AVAILABLE | UNAVAILABLE
  remark: string
}

export interface StaffScheduleCreateParams {
  storeId: number
  workDate: string
  startTime: string
  endTime: string
  status: string
  remark?: string
}

export interface StaffQueryParams extends PageParams {
  status?: string
}

// ─── Staff CRUD ───

export const getStaffList = (params: StaffQueryParams) => {
  return request.get<PageResponse<StaffMember>>('/v1/admin/staff', { params })
}

export const createStaff = (data: StaffCreateParams) => {
  return request.post<StaffMember>('/v1/admin/staff', data)
}

export const updateStaff = (id: number, data: StaffCreateParams) => {
  return request.put<StaffMember>(`/v1/admin/staff/${id}`, data)
}

export const disableStaff = (id: number) => {
  return request.post<StaffMember>(`/v1/admin/staff/${id}/disable`)
}

// ─── Staff Skills ───

export const getStaffSkills = (staffId: number) => {
  return request.get<StaffSkillView>(`/v1/admin/staff/${staffId}/skills`)
}

export const updateStaffSkills = (staffId: number, data: StaffSkillUpdateParams) => {
  return request.put<StaffSkillView>(`/v1/admin/staff/${staffId}/skills`, data)
}

// ─── Staff Schedules ───

export const getStaffSchedules = (staffId: number, params?: PageParams) => {
  return request.get<PageResponse<StaffSchedule>>(`/v1/admin/staff/${staffId}/schedules`, { params })
}

export const createStaffSchedule = (staffId: number, data: StaffScheduleCreateParams) => {
  return request.post<StaffSchedule>(`/v1/admin/staff/${staffId}/schedules`, data)
}

export const updateStaffSchedule = (staffId: number, scheduleId: number, data: StaffScheduleCreateParams) => {
  return request.put<StaffSchedule>(`/v1/admin/staff/${staffId}/schedules/${scheduleId}`, data)
}
