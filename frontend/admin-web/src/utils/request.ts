import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
});

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  (response) => {
    const res = response.data;
    // Assuming backend returns { code, message, data } 
    // And code === 0 or 200 is success, here we just return data
    // based on "ApiResponse<T>" standard
    return res;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        localStorage.removeItem('admin_token');
        router.push('/login');
      } else if (status === 403) {
        router.push('/403');
      } else {
        ElMessage.error(data.message || 'Server Error');
      }
    } else {
      ElMessage.error('Network Error');
    }
    return Promise.reject(error);
  }
);

export default request;