<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <h2 class="page-title">订单管理</h2>

      <div class="admin-panel table-panel">
        <el-table :data="orders" v-loading="loading">
          <el-table-column label="订单号" prop="orderNo" width="205" />
          <el-table-column label="用户" prop="username" width="95" />
          <el-table-column label="金额" width="90" align="center">
            <template #default="{ row }"><b>&yen;{{ row.totalAmount }}</b></template>
          </el-table-column>
          <el-table-column label="地址" prop="address" min-width="140" show-overflow-tooltip />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <span class="chip" :class="{ on: row.status === 'completed' }">{{ textMap[row.status] }}</span>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="180" align="center">
            <template #default="{ row }">{{ row.createdAt }}</template>
          </el-table-column>
          <el-table-column label="操作" width="110" align="center">
            <template #default="{ row }">
              <button class="link-btn" @click="showDetail(row.id)">详情</button>
              <button v-if="row.status === 'paid'" class="link-btn" @click="ship(row.id)">发货</button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pager" v-if="total > pageSize">
        <el-pagination layout="prev, pager, next" :total="total" :page-size="pageSize"
          v-model:current-page="page" @current-change="loadOrders" />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog title="订单详情" v-model="dialogVisible" width="650px" destroy-on-close>
      <div v-loading="detailLoading">
        <template v-if="detail">
          <div class="d-header">
            <div>
              <span class="d-label">订单号</span>
              <div class="d-value">{{ detail.orderNo }}</div>
            </div>
            <span class="chip" :class="{ on: detail.status === 'completed' }">{{ textMap[detail.status] }}</span>
          </div>

          <div class="d-row"><span class="d-key">用户</span><span>{{ detail.username }}</span></div>
          <div class="d-row"><span class="d-key">收货地址</span><span>{{ detail.address }}</span></div>
          <div class="d-row"><span class="d-key">下单时间</span><span>{{ detail.createdAt }}</span></div>

          <h4 class="d-section">商品明细</h4>
          <div class="d-item" v-for="item in detail.items" :key="item.id">
            <span>{{ item.title }} × {{ item.quantity }}</span>
            <span class="d-price">&yen;{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>

          <div class="d-total">合计 <span class="d-sum">&yen;{{ detail.totalAmount }}</span></div>

          <template v-if="detail.review">
            <h4 class="d-section">用户评价</h4>
            <el-rate v-model="detail.review.rating" disabled show-score size="small" />
            <p class="d-review">{{ detail.review.content }}</p>
          </template>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import { getAdminOrders, updateOrderStatus, getAdminOrderDetail } from '../../api/admin'
import { ElMessage } from 'element-plus'

const orders = ref([]), loading = ref(false), page = ref(1), pageSize = 10, total = ref(0)
const dialogVisible = ref(false), detail = ref(null), detailLoading = ref(false)
const textMap = { pending: '待支付', paid: '已支付', shipped: '已发货', completed: '已完成' }

async function loadOrders() { loading.value = true; try { const r = await getAdminOrders({ page: page.value, size: pageSize }); orders.value = r.data.records; total.value = r.data.total } finally { loading.value = false } }
async function ship(id) { await updateOrderStatus(id, 'shipped'); ElMessage.success('已发货'); loadOrders() }

async function showDetail(id) {
  dialogVisible.value = true
  detailLoading.value = true
  try {
    const res = await getAdminOrderDetail(id)
    detail.value = res.data
  } finally { detailLoading.value = false }
}

onMounted(loadOrders)
</script>

<style scoped>
/* reserve a full page of rows so the pager keeps one position across pages */
.table-panel {
  margin-top: 24px;
  min-height: 570px;
}
.pager { display: flex; justify-content: center; margin-top: 24px; }
.link-btn + .link-btn { margin-left: 16px; }

.d-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--hairline);
}
.d-label {
  font-family: var(--font-text);
  font-size: 12px;
  letter-spacing: -0.12px;
  color: var(--ink-muted-80);
}
.d-value {
  margin-top: 4px;
  font-family: var(--font-text);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.374px;
  color: var(--ink);
}

.d-row {
  display: flex;
  gap: 20px;
  padding: 8px 0;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink);
}
.d-key { width: 72px; flex-shrink: 0; color: var(--ink-muted-80); }

.d-section {
  margin: 20px 0 10px;
  padding-top: 16px;
  border-top: 1px solid var(--hairline);
  font-family: var(--font-text);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: -0.24px;
  color: var(--ink);
}
.d-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 0;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink);
}
.d-price { font-weight: 600; white-space: nowrap; }

.d-total {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 8px;
  margin-top: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--hairline);
  font-family: var(--font-text);
  font-size: 15px;
  letter-spacing: -0.24px;
  color: var(--ink-muted-80);
}
.d-sum {
  font-size: 22px;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.d-review {
  margin-top: 8px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}

:deep(.el-rate) {
  --el-rate-fill-color: var(--ink);
  --el-rate-void-color: var(--hairline);
  --el-rate-disabled-void-color: var(--hairline);
}
</style>
