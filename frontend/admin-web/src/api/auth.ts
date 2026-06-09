import request from '../utils/request';

export interface LoginParams {
  username: string;
  password?: string; // It depends on how we implemented login in Phase 9
}

export interface LoginResult {
  token: string;
}

export interface AdminUser {
  id: number;
  username: string;
  roleCode?: string;
  permissions?: string[];
  // other user info
}

export const login = (data: LoginParams) => {
  return request.post<any, { data: LoginResult }>('/v1/admin/auth/login', data);
};

export const getUserInfo = () => {
  return request.get<any, { data: AdminUser }>('/v1/admin/auth/me');
};