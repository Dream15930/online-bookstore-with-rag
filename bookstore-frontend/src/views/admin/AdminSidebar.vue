<template>
  <div class="sidebar">
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
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<style scoped>
.sidebar {
  width: 220px; flex-shrink: 0;
  background: var(--white); border-radius: var(--radius);
  border: 1px solid var(--border); padding: 20px 0;
  min-height: calc(100vh - 140px - 40px);
  position: sticky; top: 80px;
}
.sidebar-title {
  font-size: 13px; font-weight: 600; color: var(--text-secondary);
  text-transform: uppercase; letter-spacing: 1px;
  padding: 0 20px 16px; border-bottom: 1px solid var(--border);
  margin-bottom: 8px;
}
.sidebar-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 20px; font-size: 14px; font-weight: 500;
  color: var(--text-secondary); text-decoration: none;
  margin: 2px 12px; border-radius: 8px;
  transition: all var(--transition);
}
.sidebar-item:hover { background: #f3f4f6; color: var(--text); }
.sidebar-item.active {
  background: var(--primary-light); color: var(--primary); font-weight: 600;
}
</style>
