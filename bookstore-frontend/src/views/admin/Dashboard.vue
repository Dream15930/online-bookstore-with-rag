<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <h2 class="page-title">仪表盘</h2>

      <div class="stats-grid">
        <div class="stat">
          <div class="stat-num">{{ stats.bookCount }}</div>
          <div class="stat-label">图书总数</div>
        </div>
        <div class="stat">
          <div class="stat-num">{{ stats.orderCount }}</div>
          <div class="stat-label">有效订单</div>
        </div>
        <div class="stat">
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
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-top: 24px;
}
.stat {
  padding: 32px 24px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
}
.stat-num {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.stat-label {
  margin-top: 8px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}

@media (max-width: 833px) {
  .stats-grid { grid-template-columns: 1fr; gap: 16px; }
}
</style>
