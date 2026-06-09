import { defineStore } from 'pinia';
import { login, getUserInfo, LoginParams, AdminUser } from '../api/auth';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('admin_token'));
  const userInfo = ref<AdminUser | null>(null);

  const loginAction = async (data: LoginParams) => {
    const res = await login(data);
    if (res.data && res.data.token) {
      token.value = res.data.token;
      localStorage.setItem('admin_token', res.data.token);
    }
  };

  const getInfoAction = async () => {
    const res = await getUserInfo();
    if (res.data) {
      userInfo.value = res.data;
    }
  };

  const logoutAction = () => {
    token.value = null;
    userInfo.value = null;
    localStorage.removeItem('admin_token');
  };

  return { token, userInfo, loginAction, getInfoAction, logoutAction };
});