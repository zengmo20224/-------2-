import request from '../utils/request';

export interface StoreInfo {
  id?: number;
  storeName: string;
  phone: string;
  address: string;
  longitude?: number;
  latitude?: number;
  businessHours?: string;
  status: string; // e.g. open, closed
  description?: string;
}

export interface StoreConfig {
  id?: number;
  storeId?: number;
  homeServiceRadiusKm: number;
  bookingAdvanceDays: number;
  bookingCancelHours: number;
  timeSlotMinutes: number;
  autoConfirmBooking: number; // 0 or 1
  contentAutoPublish: number; // 0 or 1
}

// Fixed store ID for V1 single store
const defaultStoreId = 1;

export const getStoreInfo = () => {
  return request.get<any, { data: StoreInfo }>(`/v1/admin/stores/${defaultStoreId}`);
};

export const updateStoreInfo = (data: Partial<StoreInfo>) => {
  return request.patch<any, { data: StoreInfo }>(`/v1/admin/stores/${defaultStoreId}`, data);
};

export const getStoreConfig = () => {
  return request.get<any, { data: StoreConfig }>(`/v1/admin/stores/${defaultStoreId}/config`);
};

export const updateStoreConfig = (data: Partial<StoreConfig>) => {
  return request.put<any, { data: StoreConfig }>(`/v1/admin/stores/${defaultStoreId}/config`, data);
};