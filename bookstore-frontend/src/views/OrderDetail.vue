<template>
  <div class="order-detail" v-loading="loading">
    <nav class="crumbs">
      <router-link to="/orders" class="text-link">我的订单</router-link>
      <span class="sep">/</span>
      <span class="current">订单详情</span>
    </nav>

    <div class="card" v-if="order">
      <div class="head">
        <div>
          <div class="label">订单号</div>
          <div class="no">{{ order.orderNo }}</div>
        </div>
        <span class="status">{{ statusText(order.status) }}</span>
      </div>

      <div class="info">
        <div class="cell">
          <span class="label">收货地址</span>
          <span class="val">{{ order.address }}</span>
        </div>
        <div class="cell">
          <span class="label">下单时间</span>
          <span class="val">{{ order.createdAt }}</span>
        </div>
      </div>

      <div class="items">
        <h4 class="sub-title">商品明细</h4>
        <div class="item" v-for="item in order.items" :key="item.id">
          <span class="name">{{ item.title }} <em>x{{ item.quantity }}</em></span>
          <span class="price">&yen;{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </div>

      <div class="total">
        合计金额 <span class="num">&yen;{{ order.totalAmount }}</span>
      </div>

      <div class="action" v-if="order.status === 'shipped'">
        <button class="pill" @click="handleComplete" :disabled="completing">
          {{ completing ? '处理中…' : '确认收货' }}
        </button>
      </div>

      <div class="review" v-if="order.status === 'completed'">
        <template v-if="order.review">
          <h4 class="sub-title">我的评价</h4>
          <el-rate v-model="order.review.rating" disabled show-score />
          <p class="review-body">{{ order.review.content }}</p>
        </template>
        <template v-else>
          <h4 class="sub-title">评价本次购买</h4>
          <div class="rating-row">
            <span>评分</span>
            <el-rate v-model="rating" show-score />
          </div>
          <el-input v-model="reviewContent" type="textarea" :rows="3" placeholder="分享你的阅读感受…" class="review-input" />
          <button class="pill" @click="handleReview" :disabled="reviewing">
            {{ reviewing ? '提交中…' : '提交评价' }}
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail, completeOrder, reviewOrder } from '../api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const order = ref(null)
const loading = ref(false)
const completing = ref(false)
const reviewing = ref(false)
const rating = ref(5)
const reviewContent = ref('')

function statusText(s) {
  return { pending: '待支付', paid: '已支付', shipped: '已发货', completed: '已完成' }[s] || s
}

async function load() {
  loading.value = true
  try { const res = await getOrderDetail(route.params.id); order.value = res.data } finally { loading.value = false }
}

async function handleComplete() {
  completing.value = true
  try { await completeOrder(order.value.id); ElMessage.success('已确认收货'); load() } finally { completing.value = false }
}

async function handleReview() {
  if (!reviewContent.value.trim()) { ElMessage.warning('请输入评价内容'); return }
  reviewing.value = true
  try { await reviewOrder(order.value.id, { content: reviewContent.value, rating: rating.value }); ElMessage.success('评价成功'); load() } finally { reviewing.value = false }
}

onMounted(load)
</script>

<style scoped>
.order-detail {
  max-width: 780px;
  margin: 0 auto;
  padding: 32px 24px 80px;
}

.crumbs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.crumbs .current { color: var(--ink); }

.card {
  padding: 32px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--hairline);
}
.label {
  font-family: var(--font-text);
  font-size: 12px;
  letter-spacing: -0.12px;
  color: var(--ink-muted-80);
}
.no {
  margin-top: 4px;
  font-family: var(--font-text);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.374px;
  color: var(--ink);
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

.info {
  display: flex;
  gap: 48px;
  flex-wrap: wrap;
  padding: 20px 0;
  border-bottom: 1px solid var(--hairline);
}
.cell { display: flex; flex-direction: column; gap: 4px; }
.val {
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink);
}

.items { padding: 20px 0; }
.sub-title {
  margin-bottom: 12px;
  font-family: var(--font-text);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 0;
  border-top: 1px solid var(--hairline);
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.item em {
  font-style: normal;
  margin-left: 6px;
  color: var(--ink-muted-80);
}
.price { font-weight: 600; white-space: nowrap; }

.total {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid var(--hairline);
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink-muted-80);
}
.num {
  font-size: 26px;
  letter-spacing: -0.374px;
  color: var(--ink);
}

.action {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.review {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--hairline);
}
.rating-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.review-input { margin-bottom: 16px; }
.review-body {
  margin-top: 10px;
  font-family: var(--font-text);
  font-size: 17px;
  line-height: 1.47;
  letter-spacing: -0.374px;
  color: var(--ink);
}

:deep(.el-rate) {
  --el-rate-fill-color: var(--ink);
  --el-rate-void-color: var(--hairline);
  --el-rate-disabled-void-color: var(--hairline);
}

@media (max-width: 833px) {
  .order-detail { padding: 24px 20px 64px; }
  .card { padding: 24px 20px; }
  .info { gap: 24px; }
}
</style>
