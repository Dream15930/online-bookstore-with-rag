<template>
  <div class="orders-page">
    <h2 class="page-title">我的订单</h2>

    <div class="list" v-if="orders.length">
      <article class="order" v-for="order in orders" :key="order.id">
        <div class="order-head">
          <span class="order-no">订单号 {{ order.orderNo }}</span>
          <span class="status">{{ statusText(order.status) }}</span>
        </div>
        <div class="order-body">
          <div class="meta">
            <span>金额 <b>&yen;{{ order.totalAmount }}</b></span>
            <span>时间 {{ order.createdAt }}</span>
          </div>
          <router-link :to="`/order/${order.id}`" class="text-link">查看详情</router-link>
        </div>
      </article>
    </div>

    <div class="empty" v-else>
      <el-icon :size="44"><Document /></el-icon>
      <p>暂无订单</p>
      <button class="pill" @click="$router.push('/')">去购书</button>
    </div>

    <div class="pager">
      <el-pagination v-if="total" layout="prev, pager, next" :total="total" :page-size="pageSize"
        v-model:current-page="page" @current-change="loadOrders" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getMyOrders } from '../api/order'

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(5)
const total = ref(0)

const CARD_PITCH = 140 // 一张订单卡含间距的估算高度
const CHROME_H = 270   // 导航 + 页面留白 + 标题 + 分页

/** 按视口高度算每页条数，让一页正好放满一屏——用翻页而不是内部滚动 */
function fitPageSize() {
  const avail = window.innerHeight - CHROME_H
  pageSize.value = Math.min(10, Math.max(3, Math.floor(avail / CARD_PITCH)))
}

function statusText(s) {
  return { pending: '待支付', paid: '已支付', shipped: '已发货', completed: '已完成' }[s] || s
}

async function loadOrders() {
  loading.value = true
  try {
    const res = await getMyOrders({ page: page.value, size: pageSize.value })
    orders.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

let resizeTimer = null
function onResize() {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    const before = pageSize.value
    fitPageSize()
    if (pageSize.value !== before) { page.value = 1; loadOrders() }
  }, 200)
}

onMounted(() => {
  fitPageSize()
  window.addEventListener('resize', onResize)
  loadOrders()
})

onUnmounted(() => {
  clearTimeout(resizeTimer)
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
/* 固定一屏：列表自适应，超出时内部滚动，分页钉在底部 */
.orders-page {
  max-width: 820px;
  margin: 0 auto;
  padding: 40px 24px;
  height: calc(100dvh - var(--nav-h) - var(--footer-h));
  display: flex;
  flex-direction: column;
}
.list {
  flex: 1;
  min-height: 0;
  margin-top: 24px;
}

.order {
  margin-bottom: 16px;
  padding: 20px 24px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
}
.order:last-child { margin-bottom: 0; }
.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--hairline);
}
.order-no {
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.status {
  flex-shrink: 0;
  padding: 3px 12px;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-pill);
  font-family: var(--font-text);
  font-size: 12px;
  letter-spacing: -0.12px;
  color: var(--ink);
}
.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  padding-top: 14px;
}
.meta {
  display: flex;
  gap: 24px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.meta b {
  margin-left: 4px;
  font-weight: 600;
  color: var(--ink);
}

.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-top: 24px;
  padding: 72px 20px;
  text-align: center;
  color: var(--ink-muted-80);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
}
.empty p {
  margin: 16px 0 24px;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
}

.pager {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
  min-height: 32px;
}

@media (max-width: 833px) {
  .orders-page { padding: 32px 20px; }
}
</style>
