<template>
  <div class="cart-page">
    <div class="head">
      <h2 class="page-title">我的购物车</h2>
      <span class="count" v-if="items.length">{{ items.length }} 种商品</span>
    </div>

    <div class="list" v-if="items.length" v-loading="loading">
      <div v-for="item in pagedItems" :key="item.id" class="row">
        <div class="thumb" @click="go(item.bookId)">
          <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.title" />
          <div v-else class="thumb-fallback">{{ item.title?.charAt(0) }}</div>
        </div>
        <div class="info" @click="go(item.bookId)">
          <div class="title">{{ item.title }}</div>
          <div class="author">{{ item.author }}</div>
        </div>
        <div class="price">&yen;{{ item.price }}</div>
        <div class="qty">
          <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" @change="updateQty(item)" />
        </div>
        <div class="subtotal">&yen;{{ (item.price * item.quantity).toFixed(2) }}</div>
        <button class="link-btn" @click="remove(item.id)">删除</button>
      </div>
    </div>

    <div class="empty" v-else-if="!loading">
      <el-icon :size="44"><ShoppingCart /></el-icon>
      <p>购物车是空的</p>
      <button class="pill" @click="$router.push('/')">去逛逛</button>
    </div>

    <div class="foot" v-if="items.length">
      <el-pagination v-if="items.length" layout="prev, pager, next" :total="items.length"
        :page-size="pageSize" v-model:current-page="page" />
      <div class="summary">
        <span class="sum-label">合计金额</span>
        <span class="sum-amount">&yen;{{ total.toFixed(2) }}</span>
        <button class="pill" @click="$router.push('/checkout')">去结算</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, updateCartQuantity, removeFromCart } from '../api/cart'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const items = ref([])
const loading = ref(false)
const total = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

const page = ref(1)
const pageSize = ref(5)
const ROW_PITCH = 92 // 一行含间距的估算高度
const CHROME_H = 330 // 导航 + 页脚 + 页面留白 + 标题 + 底部条

/** 按视口高度算每页行数，一页正好放满一屏——用翻页而不是内部滚动 */
function fitPageSize() {
  pageSize.value = Math.min(12, Math.max(3, Math.floor((window.innerHeight - CHROME_H) / ROW_PITCH)))
}

const pagedItems = computed(() =>
  items.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
)

function go(bookId) { router.push(`/book/${bookId}`) }

async function load() {
  loading.value = true
  try {
    const res = await getCart()
    items.value = res.data
    const maxPage = Math.max(1, Math.ceil(items.value.length / pageSize.value))
    if (page.value > maxPage) page.value = maxPage
  } finally { loading.value = false }
}

async function updateQty(row) {
  await updateCartQuantity(row.id, row.quantity)
  cartStore.fetchCount()
}

async function remove(id) {
  await removeFromCart(id)
  cartStore.fetchCount()
  load()
}

let resizeTimer = null
function onResize() {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    fitPageSize()
    const maxPage = Math.max(1, Math.ceil(items.value.length / pageSize.value))
    if (page.value > maxPage) page.value = maxPage
  }, 200)
}

onMounted(() => {
  fitPageSize()
  window.addEventListener('resize', onResize)
  load()
})

onUnmounted(() => {
  clearTimeout(resizeTimer)
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
/* 固定一屏：列表自适应，底部条（分页 + 合计）钉住 */
.cart-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 24px;
  height: calc(100dvh - var(--nav-h) - var(--footer-h));
  display: flex;
  flex-direction: column;
}
.head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}
.count {
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink-muted-80);
}

.list {
  flex: 1;
  min-height: 0;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.row {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  border-top: 1px solid var(--hairline);
}
.row:first-child { border-top: none; }

.thumb {
  width: 56px;
  height: 76px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: var(--canvas-parchment);
  border-radius: var(--radius-sm);
  cursor: pointer;
}
.thumb img { width: 100%; height: 100%; object-fit: contain; padding: 4px; }
.thumb-fallback {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 600;
  color: var(--ink-muted-80);
}

.info { flex: 1; min-width: 0; cursor: pointer; }
.title {
  font-family: var(--font-text);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.374px;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.author {
  margin-top: 4px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}

.price,
.subtotal {
  width: 96px;
  text-align: center;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.subtotal { font-weight: 600; }
.qty { width: 120px; }

.foot {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px 24px;
  flex-wrap: wrap;
  margin-top: 20px;
  min-height: 44px;
}
.summary {
  display: flex;
  align-items: center;
  gap: 20px;
}
.sum-label {
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink-muted-80);
}
.sum-amount {
  font-family: var(--font-text);
  font-size: 28px;
  letter-spacing: -0.374px;
  color: var(--ink);
}

.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 80px 20px;
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

@media (max-width: 833px) {
  .cart-page { padding: 32px 20px; }
  .row { flex-wrap: wrap; gap: 12px; padding: 16px 20px; }
  .price { display: none; }
  .qty { width: auto; }
  .subtotal { width: auto; margin-left: auto; }
  .foot { justify-content: center; }
}
</style>
