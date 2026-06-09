<template>
  <el-container class="app-wrapper">
    <el-aside width="200px" class="sidebar-container">
      <el-menu
        router
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        style="height: 100%"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Menu /></el-icon>
          <span>Dashboard</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main-container">
      <el-header class="navbar">
        <div class="right-menu">
          <span class="user-name">{{ userStore.userInfo?.username }}</span>
          <el-button type="primary" link @click="handleLogout">Logout</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useUserStore } from '../store/user';
import { useRouter } from 'vue-router';
import { Menu } from '@element-plus/icons-vue';

const userStore = useUserStore();
const router = useRouter();

const handleLogout = () => {
  userStore.logoutAction();
  router.push('/login');
};
</script>

<style scoped>
.app-wrapper {
  height: 100vh;
  width: 100vw;
}
.sidebar-container {
  background-color: #304156;
}
.navbar {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}
.right-menu {
  display: flex;
  align-items: center;
  gap: 15px;
}
.user-name {
  font-size: 14px;
  color: #606266;
}
</style>