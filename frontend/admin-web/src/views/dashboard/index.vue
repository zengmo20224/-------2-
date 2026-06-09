<template>
  <div class="dashboard-container">
    <h2>Welcome to Admin Dashboard</h2>
    <p>Logged in as: {{ userStore.userInfo?.username }}</p>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}
</style>