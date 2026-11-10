<template>
  <nav class="sidebar">
    <div class="sidebar-title">管理后台</div>
    <router-link to="/admin" class="sidebar-item" :class="{ active: route.path === '/admin' }">
      <el-icon><DataAnalysis /></el-icon> 仪表盘
    </router-link>
    <router-link to="/admin/books" class="sidebar-item" :class="{ active: route.path === '/admin/books' }">
      <el-icon><Notebook /></el-icon> 图书管理
    </router-link>
    <router-link to="/admin/reports" class="sidebar-item" :class="{ active: route.path === '/admin/reports' }">
      <el-icon><DataLine /></el-icon> 统计报表
    </router-link>
    <router-link to="/admin/orders" class="sidebar-item" :class="{ active: route.path === '/admin/orders' }">
      <el-icon><List /></el-icon> 订单管理
    </router-link>
    <router-link to="/admin/reviews" class="sidebar-item" :class="{ active: route.path === '/admin/reviews' }">
      <el-icon><ChatLineSquare /></el-icon> 评价管理
    </router-link>
    <router-link to="/admin/users" class="sidebar-item" :class="{ active: route.path === '/admin/users' }">
      <el-icon><User /></el-icon> 用户管理
    </router-link>
  </nav>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 后台各页共用此侧栏，用它作为「隐藏滚动条」的单一挂载点
onMounted(() => document.documentElement.classList.add('admin-scroll'))
onUnmounted(() => document.documentElement.classList.remove('admin-scroll'))
</script>

<style scoped>
.sidebar {
  width: 220px;
  flex-shrink: 0;
  padding: 12px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
  position: sticky;
  top: 68px;
}
.sidebar-title {
  padding: 8px 12px 14px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--hairline);
  font-family: var(--font-text);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  color: var(--ink-muted-80);
}
.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  font-family: var(--font-text);
  font-size: 15px;
  letter-spacing: -0.24px;
  color: var(--ink-muted-80);
  text-decoration: none;
}
.sidebar-item:hover { background: var(--canvas-parchment); color: var(--ink); }
.sidebar-item.active {
  background: rgba(0, 102, 204, 0.08);
  color: var(--primary);
  font-weight: 600;
}

@media (max-width: 833px) {
  .sidebar {
    width: 100%;
    position: static;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
  .sidebar-title { width: 100%; }
}
</style>
