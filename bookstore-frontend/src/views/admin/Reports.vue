<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <h2 class="page-title">统计报表</h2>

      <div class="filter-bar">
        <input type="date" v-model="startDate" class="native-date" />
        <span class="filter-label">至</span>
        <input type="date" v-model="endDate" class="native-date" />
        <el-radio-group v-model="period">
          <el-radio-button value="monthly">月报</el-radio-button>
          <el-radio-button value="quarterly">季报</el-radio-button>
          <el-radio-button value="yearly">年报</el-radio-button>
        </el-radio-group>
        <el-select v-model="categoryId" placeholder="全部分类" clearable style="width:140px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="bookName" placeholder="书名" clearable style="width:140px" />
        <button class="pill small" @click="loadAll">查询</button>
      </div>

      <div class="summary-row" v-if="summary">
        <div class="summary-card"><div class="s-num">{{ summary.orderCount }}</div><div class="s-label">订单数</div></div>
        <div class="summary-card"><div class="s-num">{{ summary.bookCount }}</div><div class="s-label">售出册数</div></div>
        <div class="summary-card"><div class="s-num">&yen;{{ summary.totalRevenue }}</div><div class="s-label">销售额</div></div>
      </div>

      <div class="panels">
        <section class="admin-panel panel">
          <h3 class="panel-title">销售趋势（{{ periodText }}）</h3>
          <el-table :data="trendData" empty-text="暂无数据" size="small">
            <el-table-column label="周期" prop="period" width="110" />
            <el-table-column label="订单数" prop="orderCount" width="80" align="center" />
            <el-table-column label="销售额" align="center">
              <template #default="{ row }">&yen;{{ row.totalSales }}</template>
            </el-table-column>
          </el-table>
        </section>

        <section class="admin-panel panel">
          <h3 class="panel-title">分类销售</h3>
          <el-table :data="catData" empty-text="暂无数据" size="small">
            <el-table-column label="分类" prop="categoryName" min-width="90" />
            <el-table-column label="销量" prop="salesCount" width="70" align="center" />
            <el-table-column label="销售额" width="100" align="center">
              <template #default="{ row }">&yen;{{ row.totalRevenue }}</template>
            </el-table-column>
          </el-table>
        </section>

        <section class="admin-panel panel">
          <h3 class="panel-title">图书销量排行</h3>
          <el-table :data="bookData" empty-text="暂无数据" size="small">
            <el-table-column label="#" width="44" type="index" />
            <el-table-column label="书名" prop="title" min-width="120" show-overflow-tooltip />
            <el-table-column label="销量" prop="salesCount" width="70" align="center" />
            <el-table-column label="销售额" width="100" align="center">
              <template #default="{ row }">&yen;{{ row.totalRevenue }}</template>
            </el-table-column>
          </el-table>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import api from '../../api'
import { getCategories } from '../../api/book'

const now = new Date()
// 默认查最近一年
const startDate = ref(fmt(new Date(now.getFullYear() - 1, now.getMonth(), now.getDate())))
const endDate = ref(fmt(now))
const period = ref('monthly')
const categoryId = ref(null)
const bookName = ref('')
const categories = ref([])
const summary = ref(null)
const trendData = ref([])
const catData = ref([])
const bookData = ref([])

const periodText = computed(() => {
  const m = { monthly: '月报', quarterly: '季报', yearly: '年报' }
  return m[period.value] || '月报'
})

function fmt(d) {
  if (!d) return ''
  const dt = new Date(d)
  const y = dt.getFullYear()
  const m = String(dt.getMonth() + 1).padStart(2, '0')
  const day = String(dt.getDate()).padStart(2, '0')
  return y + '-' + m + '-' + day
}

async function loadAll() {
  if (!startDate.value || !endDate.value) return
  const s = startDate.value
  const e = endDate.value
  const p = { startDate: s, endDate: e }
  const [ss, tr, bk, ct] = await Promise.all([
    api.get('/admin/reports/summary', { params: p }),
    api.get('/admin/reports/trend', { params: { ...p, period: period.value } }),
    api.get('/admin/reports/books', { params: { ...p, categoryId: categoryId.value || undefined, bookName: bookName.value || undefined } }),
    api.get('/admin/reports/categories', { params: p }),
  ])
  summary.value = ss.data
  trendData.value = tr.data
  bookData.value = bk.data
  catData.value = ct.data
}

onMounted(async () => {
  const r = await getCategories()
  categories.value = r.data
  loadAll()
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  padding: 16px 20px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
}
.filter-label {
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.native-date {
  padding: 8px 12px;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-md);
  background: var(--canvas);
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink);
  outline: none;
}
.native-date:focus { border-color: var(--primary); }

.summary-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-top: 24px;
}
.summary-card {
  padding: 24px;
  text-align: center;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
}
.s-num {
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 600;
  line-height: 1.47;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.s-label {
  margin-top: 6px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}

/* the three reports sit side by side so the page fits without wheel-scrolling */
.panels {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
  margin-top: 24px;
  align-items: start;
}
.panel { margin-top: 0; }
/* 表格体自带滚动：页面高度被钉死，数据再多也不整页滚动 */
.panel :deep(.el-table__body-wrapper) {
  max-height: min(320px, 34vh);
  overflow-y: auto;
}
.panel-title {
  padding: 16px 16px 0;
  font-family: var(--font-text);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.374px;
  color: var(--ink);
}

@media (max-width: 1068px) {
  .panels { grid-template-columns: 1fr; }
}
@media (max-width: 833px) {
  .summary-row { grid-template-columns: 1fr; gap: 16px; }
}
</style>
