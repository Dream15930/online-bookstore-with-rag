<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <h2>订单管理</h2>
      <div class="table-card">
        <el-table :data="orders" v-loading="loading" border stripe>
          <el-table-column label="订单号" prop="orderNo" width="200" />
          <el-table-column label="用户" prop="username" width="120" />
          <el-table-column label="金额" width="100" align="center">
            <template #default="{ row }"><b>&yen;{{ row.totalAmount }}</b></template>
          </el-table-column>
          <el-table-column label="地址" prop="address" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="typeMap[row.status]" size="small">{{ textMap[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="180" align="center">
            <template #default="{ row }">{{ row.createdAt }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template #default="{ row }">
              <el-button size="small" @click="showDetail(row.id)">详情</el-button>
              <el-button v-if="row.status==='paid'" type="primary" size="small" @click="ship(row.id)">发货</el-button>
              <span v-else-if="row.status==='shipped'" class="done-text">已发货</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination" v-if="total > pageSize">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page" @current-change="loadOrders" />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog title="订单详情" v-model="dialogVisible" width="650px" destroy-on-close>
      <div v-loading="detailLoading">
        <template v-if="detail">
          <div class="detail-header">
            <div>
              <span class="d-label">订单号</span>
              <div class="d-value">{{ detail.orderNo }}</div>
            </div>
            <el-tag :type="typeMap[detail.status]" size="large">{{ textMap[detail.status] }}</el-tag>
          </div>
          <div class="d-row"><span>用户</span><span>{{ detail.username }}</span></div>
          <div class="d-row"><span>收货地址</span><span>{{ detail.address }}</span></div>
          <div class="d-row"><span>下单时间</span><span>{{ detail.createdAt }}</span></div>

          <h4 class="d-section">商品明细</h4>
          <div class="d-item" v-for="item in detail.items" :key="item.id">
            <span class="di-name">{{ item.title }} × {{ item.quantity }}</span>
            <span class="di-price">&yen;{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>

          <div class="d-total">合计 <b>&yen;{{ detail.totalAmount }}</b></div>

          <div class="d-section" v-if="detail.review">
            <h4>用户评价</h4>
            <el-rate v-model="detail.review.rating" disabled show-score size="small" />
            <p class="d-review">{{ detail.review.content }}</p>
          </div>
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

const orders=ref([]), loading=ref(false), page=ref(1), pageSize=10, total=ref(0)
const dialogVisible=ref(false), detail=ref(null), detailLoading=ref(false)
const typeMap={pending:'info',paid:'warning',shipped:'primary',completed:'success'}
const textMap={pending:'待支付',paid:'已支付',shipped:'已发货',completed:'已完成'}

async function loadOrders() { loading.value=true; try { const r=await getAdminOrders({page:page.value,size:pageSize}); orders.value=r.data.records; total.value=r.data.total } finally { loading.value=false } }
async function ship(id) { await updateOrderStatus(id,'shipped'); ElMessage.success('已发货'); loadOrders() }

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
.admin-page { display: flex; gap: 24px; }
.admin-main { flex: 1; }
.admin-main h2 { font-size: 22px; font-weight: 700; margin-bottom: 16px; }
.table-card { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); overflow: hidden; }
.done-text { font-size: 13px; color: var(--text-secondary); }
.pagination { display: flex; justify-content: center; margin-top: 20px; }

.detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #f3f4f6; }
.d-label { font-size: 12px; color: var(--text-secondary); }
.d-value { font-size: 16px; font-weight: 600; margin-top: 2px; }
.d-row { display: flex; gap: 20px; padding: 6px 0; font-size: 14px; }
.d-row span:first-child { color: var(--text-secondary); width: 70px; flex-shrink: 0; }

.d-section { font-size: 15px; font-weight: 600; margin: 16px 0 10px; padding-top: 12px; border-top: 1px solid #f3f4f6; }
.d-item { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
.di-name { color: var(--text); }
.di-price { font-weight: 600; }

.d-total { text-align: right; font-size: 15px; padding: 12px 0; border-top: 1px solid var(--border); margin-top: 8px; }
.d-total b { font-size: 22px; color: var(--danger); margin-left: 8px; }

.d-review { margin-top: 8px; font-size: 14px; color: var(--text-secondary); }
</style>
