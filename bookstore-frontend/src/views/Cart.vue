<template>
  <div class="cart-page">
    <div class="page-header">
      <h2>我的购物车</h2>
      <span class="item-count" v-if="items.length">{{ items.length }} 种商品</span>
    </div>

    <div class="cart-card" v-if="items.length" v-loading="loading">
      <div class="cart-list">
        <div v-for="item in items" :key="item.id" class="cart-item">
          <div class="item-img" @click="$router.push(`/book/${item.bookId}`)">
            <template v-if="item.coverUrl">
              <el-image :src="item.coverUrl" fit="cover" style="width:64px;height:88px;border-radius:4px">
                <template #error>
                  <div class="cart-thumb-css" :style="thumbStyle(item)">{{ item.title?.charAt(0) }}</div>
                </template>
              </el-image>
            </template>
            <div v-else class="cart-thumb-css" :style="thumbStyle(item)">{{ item.title?.charAt(0) }}</div>
          </div>
          <div class="item-info" @click="$router.push(`/book/${item.bookId}`)">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-author">{{ item.author }}</div>
          </div>
          <div class="item-price">&yen;{{ item.price }}</div>
          <div class="item-qty">
            <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" @change="updateQty(item)" />
          </div>
          <div class="item-subtotal">&yen;{{ (item.price * item.quantity).toFixed(2) }}</div>
          <div class="item-del">
            <el-button type="danger" plain size="small" @click="remove(item.id)">删除</el-button>
          </div>
        </div>
      </div>

      <div class="cart-summary">
        <div class="total-row">
          <span>合计金额</span>
          <span class="total-amount">&yen;{{ total.toFixed(2) }}</span>
        </div>
        <el-button type="primary" size="large" @click="$router.push('/checkout')" class="checkout-btn">去结算</el-button>
      </div>
    </div>

    <div class="empty-cart" v-else-if="!loading">
      <el-icon :size="56"><ShoppingCart /></el-icon>
      <p>购物车是空的</p>
      <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCart, updateCartQuantity, removeFromCart } from '../api/cart'
import { useCartStore } from '../stores/cart'

const cartStore = useCartStore()
const items = ref([])
const loading = ref(false)
const total = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

const thumbGradients = ['#3b5998','#7c3aed','#0d9488','#ea580c','#db2777','#0284c7','#b91c1c','#2563eb','#15803d','#c2410c']
function thumbStyle(item) {
  const idx = ((item.bookId || 1) % thumbGradients.length)
  return { background: thumbGradients[idx] }
}

async function load() {
  loading.value = true
  try {
    const res = await getCart()
    items.value = res.data
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

onMounted(load)
</script>

<style scoped>
.cart-page { max-width: 900px; margin: 0 auto; }
.page-header { display: flex; align-items: baseline; gap: 12px; margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; }
.item-count { font-size: 14px; color: var(--text-secondary); }

.cart-card { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); overflow: hidden; }

.cart-item {
  display: flex; align-items: center; gap: 16px; padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6; transition: background 0.2s;
}
.cart-item:hover { background: #fafafa; }

.item-img { width: 64px; height: 88px; cursor: pointer; flex-shrink: 0; }
.cart-thumb-css {
  width: 64px; height: 88px; border-radius: 4px; display: flex;
  align-items: center; justify-content: center;
  color: rgba(255,255,255,0.9); font-size: 32px; font-weight: 800;
}

.item-info { flex: 1; cursor: pointer; min-width: 0; }
.item-title { font-weight: 600; font-size: 15px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-author { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }

.item-price, .item-subtotal { font-weight: 600; font-size: 15px; width: 90px; text-align: center; }
.item-subtotal { color: var(--danger); }

.cart-summary {
  display: flex; justify-content: flex-end; align-items: center; gap: 28px;
  padding: 20px 24px; background: #fafafa;
}
.total-row { display: flex; align-items: baseline; gap: 12px; font-size: 15px; }
.total-amount { font-size: 28px; font-weight: 800; color: var(--danger); }
.checkout-btn { height: 48px; padding: 0 40px; font-size: 16px; border-radius: 10px; }

.empty-cart {
  text-align: center; padding: 80px 20px; color: var(--text-secondary);
  background: var(--white); border-radius: var(--radius); border: 1px solid var(--border);
}
.empty-cart p { margin: 16px 0 20px; font-size: 16px; }
</style>
