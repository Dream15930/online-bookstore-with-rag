<template>
  <div class="order-detail" v-loading="loading">
    <div class="breadcrumb">
      <router-link to="/orders">我的订单</router-link>
      <span>/</span>
      <span>订单详情</span>
    </div>

    <div class="detail-card" v-if="order">
      <div class="order-status-header">
        <div>
          <div class="order-label">订单号</div>
          <div class="order-no">{{ order.orderNo }}</div>
        </div>
        <el-tag :type="statusType(order.status)" size="large">{{ statusText(order.status) }}</el-tag>
      </div>

      <div class="info-grid">
        <div class="info-cell">
          <span class="info-label">收货地址</span>
          <span class="info-val">{{ order.address }}</span>
        </div>
        <div class="info-cell">
          <span class="info-label">下单时间</span>
          <span class="info-val">{{ order.createdAt }}</span>
        </div>
      </div>

      <div class="items-section">
        <h4>商品明细</h4>
        <div class="order-item" v-for="item in order.items" :key="item.id">
          <span class="oi-name">{{ item.title }} <em>x{{ item.quantity }}</em></span>
          <span class="oi-price">&yen;{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </div>

      <div class="total-line">
        合计金额：<span class="total-num">&yen;{{ order.totalAmount }}</span>
      </div>

      <div class="action-row" v-if="order.status === 'shipped'">
        <el-button type="primary" size="large" @click="handleComplete" :loading="completing" class="confirm-btn">确认收货</el-button>
      </div>

      <div class="review-section" v-if="order.status === 'completed'">
        <template v-if="order.review">
          <div class="review-done">
            <h4><el-icon><StarFilled /></el-icon> 我的评价</h4>
            <el-rate v-model="order.review.rating" disabled show-score />
            <p>{{ order.review.content }}</p>
          </div>
        </template>
        <template v-else>
          <div class="review-form">
            <h4>评价本次购买</h4>
            <div class="rating-row">
              <span>评分</span>
              <el-rate v-model="rating" show-score />
            </div>
            <el-input v-model="reviewContent" type="textarea" :rows="3" placeholder="分享你的阅读感受..." class="review-text" />
            <el-button type="primary" @click="handleReview" :loading="reviewing">提交评价</el-button>
          </div>
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

function statusType(s) { return { pending:'info', paid:'warning', shipped:'primary', completed:'success' }[s]||'info' }
function statusText(s) { return { pending:'待支付', paid:'已支付', shipped:'已发货', completed:'已完成' }[s]||s }

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
.order-detail { max-width: 750px; margin: 0 auto; }
.breadcrumb { font-size: 13px; color: var(--text-secondary); margin-bottom: 20px; display: flex; gap: 8px; }
.breadcrumb a { color: var(--text-secondary); text-decoration: none; }
.breadcrumb a:hover { color: var(--primary); }

.detail-card { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); padding: 32px; }
.order-status-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #f3f4f6; }
.order-label { font-size: 12px; color: var(--text-secondary); }
.order-no { font-size: 16px; font-weight: 600; margin-top: 4px; }

.info-grid { display: flex; gap: 32px; margin-bottom: 24px; }
.info-cell { display: flex; flex-direction: column; gap: 4px; }
.info-label { font-size: 12px; color: var(--text-secondary); }
.info-val { font-size: 14px; }

.items-section { margin-bottom: 20px; }
.items-section h4 { font-size: 15px; font-weight: 600; margin-bottom: 12px; }
.order-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f9fafb; font-size: 14px; }
.oi-name em { font-style: normal; color: var(--text-secondary); margin-left: 4px; }
.oi-price { font-weight: 600; }

.total-line { text-align: right; font-size: 16px; padding: 16px 0; border-top: 2px solid var(--border); }
.total-num { font-size: 26px; font-weight: 800; color: var(--danger); margin-left: 8px; }

.action-row { text-align: center; margin-top: 20px; }
.confirm-btn { height: 44px; padding: 0 36px; font-size: 15px; border-radius: 10px; }

.review-section { margin-top: 28px; padding-top: 24px; border-top: 1px solid var(--border); }
.review-section h4 { font-size: 15px; font-weight: 600; margin-bottom: 12px; display: flex; align-items: center; gap: 6px; }
.rating-row { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.review-text { margin-bottom: 12px; }
.review-done p { color: var(--text-secondary); margin-top: 8px; }
</style>
