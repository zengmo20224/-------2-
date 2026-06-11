<template>
  <div class="pc-not-found">
    <h1 class="pc-not-found__code">404</h1>
    <h2 class="pc-not-found__title">页面不存在</h2>
    <p class="pc-not-found__message">您访问的页面不存在或已被移除。</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.pc-not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: var(--pc-spacing-md);
}

.pc-not-found__code {
  margin: 0;
  font-size: 72px;
  font-weight: 700;
  color: var(--pc-muted);
  line-height: 1;
}

.pc-not-found__title {
  margin: 0;
  font-size: var(--pc-font-size-lg);
  color: var(--pc-ink);
}

.pc-not-found__message {
  margin: 0;
  color: var(--pc-muted);
  font-size: var(--pc-font-size-base);
}
</style>
