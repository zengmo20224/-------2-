import request from '../utils/request'
import type { PageResponse, PageParams } from '../types/api'

// ─── Types ───

export interface ProductOrder {
  id: number
  orderNo: string
  totalAmount: number
  paymentMethod: string
  paymentStatus: string
  pickupStatus: string
  status: string
  contactName: string
  contactPhone: string
  remark: string | null
  createTime: string
  confirmTime: string | null
  completeTime: string | null
  cancelTime: string | null
}

export interface ProductOrderItem {
  id: number
  productId: number
  productName: string
  productCoverUrl: string
  price: number
  quantity: number
  totalAmount: number
}

export interface ProductOrderDetail extends ProductOrder {
  userId: number
  storeId: number
  merchantRemark: string | null
  items: ProductOrderItem[]
}

export interface ProductOrderQueryParams extends PageParams {
  status?: string
}

export interface AdminOrderActionParams {
  reason?: string
}

// ─── API Functions ───

export const getProductOrderList = (params: ProductOrderQueryParams) => {
  return request.get<PageResponse<ProductOrder>>('/v1/admin/product-orders', { params })
}

export const getProductOrderDetail = (id: number) => {
  return request.get<ProductOrderDetail>(`/v1/admin/product-orders/${id}`)
}

export const confirmProductOrder = (id: number) => {
  return request.post<ProductOrder>(`/v1/admin/product-orders/${id}/confirm`)
}

export const readyProductOrder = (id: number) => {
  return request.post<ProductOrder>(`/v1/admin/product-orders/${id}/ready`)
}

export const confirmPaymentOrder = (id: number) => {
  return request.post<ProductOrder>(`/v1/admin/product-orders/${id}/confirm-payment`)
}

export const completeProductOrder = (id: number) => {
  return request.post<ProductOrder>(`/v1/admin/product-orders/${id}/complete`)
}

export const cancelProductOrder = (id: number, data?: AdminOrderActionParams) => {
  return request.post<ProductOrder>(`/v1/admin/product-orders/${id}/cancel`, data)
}

export function outOfStockProductOrder(id: number, data?: AdminOrderActionParams) {
  return request.post<ProductOrder>(`/v1/admin/product-orders/${id}/out-of-stock`, data)
}
