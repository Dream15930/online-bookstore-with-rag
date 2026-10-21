<template>
  <div class="orders-page">
    <h2>我的订单</h2>
    <template v-if="orders.length">
      <div class="order-card" v-for="order in orders" :key="order.id">
        <div class="order-head">
          <div class="order-no">订单号：{{ order.orderNo }}</div>
          <el-tag :type="statusType(order.status)" size="small">{{ statusText(order.status) }}</el-tag>
        </div>
        <div class="order-body">
          <div class="order-meta">
            <span>金额：<b>&yen;{{ order.totalAmount }}</b></span>
            <span>时间：{{ order.createdAt }}</span>
          </div>
          <el-button type="primary" plain size="small" @click="$router.push(`/order/${order.id}`)">查看详情</el-button>
        </div>
      </div>
    </template>
    <div class="empty" v-else>
      <el-icon :size="48"><Document /></el-icon>
      <p>暂无订单</p>
      <el-button type="primary" @click="$router.push('/')">去购书</el-button>
    </div>
    <div class="pagination" v-if="total > pageSize">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize"
        v-model:current-page="page" @current-change="loadOrders" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders } from '../api/order'

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)

function statusType(s) { return { pending:'info', paid:'warning', shipped:'primary', completed:'success' }[s]||'info' }
function statusText(s) { return { pending:'待支付', paid:'已支付', shipped:'已发货', completed:'已完成' }[s]||s }

async function loadOrders() {
  loading.value = true
  try {
    const res = await getMyOrders({ page: page.value, size: pageSize })
    orders.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page { max-width: 780px; margin: 0 auto; }
.orders-page h2 { font-size: 22px; font-weight: 700; margin-bottom: 20px; }

.order-card {
  background: var(--white); border-radius: var(--radius); border: 1px solid var(--border);
  padding: 18px 24px; margin-bottom: 12px; transition: box-shadow var(--transition);
}
.order-card:hover { box-shadow: var(--shadow); }
.order-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-no { font-size: 14px; color: var(--text-secondary); }

.order-body { display: flex; justify-content: space-between; align-items: center; }
.order-meta { display: flex; gap: 20px; font-size: 14px; color: var(--text-secondary); }
.order-meta b { color: var(--danger); margin-left: 4px; }

.empty { text-align: center; padding: 72px 20px; color: var(--text-secondary); background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); }
.empty p { margin: 12px 0 16px; font-size: 15px; }
.pagination { display: flex; justify-content: center; margin-top: 24px; }
</style>
