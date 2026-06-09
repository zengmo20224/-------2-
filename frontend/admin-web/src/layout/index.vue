<template>
  <el-container class="app-wrapper">
    <el-aside width="200px" class="sidebar-container">
      <div class="logo-container">
        <span class="logo-text">PetCare Admin</span>
      </div>
      <el-menu
        router
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        style="height: calc(100% - 50px)"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Menu /></el-icon>
          <span>Dashboard</span>
        </el-menu-item>

        <el-sub-menu v-if="userStore.hasPermission('store:info:read') || userStore.hasPermission('store:config:read')" index="store-sub">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>门店管理</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('store:info:read')" index="/store/info">门店信息</el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('store:config:read')" index="/store/config">门店配置</el-menu-item>
        </el-sub-menu>

        <el-menu-item v-if="userStore.hasPermission('service:item:read')" index="/services">
          <el-icon><Briefcase /></el-icon>
          <span>服务项目</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.hasPermission('staff:profile:read')" index="/staff">
          <el-icon><User /></el-icon>
          <span>员工管理</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.hasPermission('booking:booking:read')" index="/bookings">
          <el-icon><Calendar /></el-icon>
          <span>预约管理</span>
        </el-menu-item>

        <el-sub-menu v-if="userStore.hasPermission('product:item:read') || userStore.hasPermission('product:order:read')" index="product-sub">
          <template #title>
            <el-icon><ShoppingBag /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('product:item:read')" index="/products">商品列表</el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('product:order:read')" index="/product-orders">自提订单</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="userStore.hasPermission('community:post:read') || userStore.hasPermission('community:report:handle')" index="community-sub">
          <template #title>
            <el-icon><ChatDotRound /></el-icon>
            <span>社区管理</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('community:post:read')" index="/community/posts">帖子审核</el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('community:report:handle')" index="/community/reports">举报处理</el-menu-item>
        </el-sub-menu>

        <el-menu-item v-if="userStore.hasPermission('community:sensitive-word:manage')" index="/moderation/sensitive-words">
          <el-icon><Filter /></el-icon>
          <span>敏感词</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.hasPermission('admin:operation-log:read')" index="/operation-logs">
          <el-icon><Document /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main-container">
      <el-header class="navbar">
        <div class="right-menu">
          <span class="user-name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
          <el-tag size="small" type="info">{{ userStore.userInfo?.role }}</el-tag>
          <el-button type="primary" link @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useUserStore } from '../store/user'
import { useRouter } from 'vue-router'
import {
  Menu,
  OfficeBuilding,
  Briefcase,
  User,
  Calendar,
  ShoppingBag,
  ChatDotRound,
  Filter,
  Document,
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  userStore.logoutAction()
  router.push('/login')
}
</script>

<style scoped>
.app-wrapper {
  height: 100vh;
  width: 100vw;
}
.sidebar-container {
  background-color: #304156;
  overflow-y: auto;
}
.logo-container {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;
}
.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
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
  gap: 12px;
}
.user-name {
  font-size: 14px;
  color: #606266;
}
</style>
