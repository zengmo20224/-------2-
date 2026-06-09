<template>
  <div class="error-container">
    <h2>403 Forbidden</h2>
    <p>You don't have permission to access this page.</p>
    <el-button type="primary" @click="$router.push('/')">Go Home</el-button>
  </div>
</template>

<style scoped>
.error-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style>