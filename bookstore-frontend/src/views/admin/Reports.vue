<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <h2>统计报表</h2>

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
        <el-button type="primary" @click="loadAll">查询</el-button>
      </div>

      <div class="summary-row" v-if="summary">
        <div class="summary-card"><div class="s-num">{{ summary.orderCount }}</div><div class="s-label">订单数</div></div>
        <div class="summary-card"><div class="s-num">{{ summary.bookCount }}</div><div class="s-label">售出册数</div></div>
        <div class="summary-card"><div class="s-num">&yen;{{ summary.totalRevenue }}</div><div class="s-label">销售额</div></div>
      </div>

      <div class="panel">
        <h3>销售趋势（{{ periodText }}）</h3>
        <el-table :data="trendData" border stripe empty-text="暂无数据" size="small">
          <el-table-column label="周期" prop="period" width="120" />
          <el-table-column label="订单数" prop="orderCount" width="100" align="center" />
          <el-table-column label="销售额" align="center">
            <template #default="{ row }">&yen;{{ row.totalSales }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel">
        <h3>分类销售</h3>
        <el-table :data="catData" border stripe empty-text="暂无数据" size="small">
          <el-table-column label="分类" prop="categoryName" />
          <el-table-column label="销量" prop="salesCount" width="100" align="center" />
          <el-table-column label="销售额" width="140" align="center">
            <template #default="{ row }">&yen;{{ row.totalRevenue }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="panel">
        <h3>图书销量排行</h3>
        <el-table :data="bookData" border stripe empty-text="暂无数据" size="small">
          <el-table-column label="#" width="50" type="index" />
          <el-table-column label="书名" prop="title" min-width="180" />
          <el-table-column label="作者" prop="author" width="140" />
          <el-table-column label="销量" prop="salesCount" width="80" align="center" />
          <el-table-column label="销售额" width="120" align="center">
            <template #default="{ row }">&yen;{{ row.totalRevenue }}</template>
          </el-table-column>
        </el-table>
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
const startDate = ref(fmt(new Date(now.getFullYear(), now.getMonth(), 1)))
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
.admin-page { display: flex; gap: 24px; }
.admin-main { flex: 1; }
.admin-main > h2 { font-size: 22px; font-weight: 700; margin-bottom: 20px; }

.filter-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin-bottom: 24px; background: var(--white); padding: 16px 20px; border-radius: var(--radius); border: 1px solid var(--border); }

.summary-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 20px; }
.summary-card { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); padding: 24px; text-align: center; }
.s-num { font-size: 28px; font-weight: 800; color: var(--primary); }
.s-label { font-size: 13px; color: var(--text-secondary); margin-top: 6px; }

.native-date { border: 1px solid var(--border); border-radius: 6px; padding: 6px 12px; font-size: 14px; color: var(--text); background: var(--white); outline: none; }
.native-date:focus { border-color: var(--primary); }

.panel { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); padding: 20px; margin-bottom: 20px; }
.panel h3 { font-size: 15px; font-weight: 600; margin-bottom: 16px; }
</style>
