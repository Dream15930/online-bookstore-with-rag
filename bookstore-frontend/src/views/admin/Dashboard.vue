<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <h2>仪表盘</h2>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon books"><el-icon :size="28"><Notebook /></el-icon></div>
          <div class="stat-num">{{ stats.bookCount }}</div>
          <div class="stat-label">图书总数</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon orders"><el-icon :size="28"><List /></el-icon></div>
          <div class="stat-num">{{ stats.orderCount }}</div>
          <div class="stat-label">有效订单</div>
        </div>
        <div class="stat-card">
          <div class="stat-icon sales"><el-icon :size="28"><Money /></el-icon></div>
          <div class="stat-num">&yen;{{ stats.todaySales || '0.00' }}</div>
          <div class="stat-label">今日销售额</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import { getDashboard } from '../../api/admin'

const stats = ref({ bookCount: 0, orderCount: 0, todaySales: 0 })

onMounted(async () => {
  const res = await getDashboard()
  stats.value = res.data
})
</script>

<style scoped>
.admin-page { display: flex; gap: 24px; }
.admin-main { flex: 1; }
.admin-main h2 { font-size: 22px; font-weight: 700; margin-bottom: 24px; }

.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.stat-card {
  background: var(--white); border-radius: var(--radius); padding: 28px 24px;
  border: 1px solid var(--border); transition: all var(--transition);
}
.stat-card:hover { box-shadow: var(--shadow-md); }
.stat-icon {
  width: 52px; height: 52px; border-radius: 12px; display: flex;
  align-items: center; justify-content: center; margin-bottom: 16px;
}
.stat-icon.books { background: #eff6ff; color: #3b82f6; }
.stat-icon.orders { background: #fef3c7; color: #f59e0b; }
.stat-icon.sales { background: #ecfdf5; color: #10b981; }
.stat-num { font-size: 32px; font-weight: 800; letter-spacing: -0.5px; }
.stat-label { margin-top: 6px; font-size: 14px; color: var(--text-secondary); }
</style>
