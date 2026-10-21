<template>
  <div class="detail-page" v-loading="loading">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span>/</span>
      <span>{{ book?.categoryName || '图书详情' }}</span>
      <span>/</span>
      <span class="current">{{ book?.title }}</span>
    </div>

    <div class="detail-card" v-if="book">
      <div class="detail-cover-col">
        <template v-if="book.coverUrl">
          <el-image :src="book.coverUrl" fit="cover" class="cover-img">
            <template #error>
              <div class="detail-cover-css" :style="coverStyle">
                <span class="dc-cat">{{ book.categoryName }}</span>
                <span class="dc-title">{{ book.title }}</span>
                <span class="dc-author">{{ book.author }}</span>
              </div>
            </template>
          </el-image>
        </template>
        <div v-else class="detail-cover-css" :style="coverStyle">
          <span class="dc-cat">{{ book.categoryName }}</span>
          <span class="dc-title">{{ book.title }}</span>
          <span class="dc-author">{{ book.author }}</span>
        </div>
      </div>

      <div class="detail-info-col">
        <div class="detail-badge" v-if="book.stock > 0">在售</div>
        <div class="detail-badge soldout" v-else>售罄</div>
        <h1 class="detail-title">{{ book.title }}</h1>
        <div class="detail-row">
          <span class="label">作者</span>
          <span class="value">{{ book.author }}</span>
        </div>
        <div class="detail-row">
          <span class="label">ISBN</span>
          <span class="value">{{ book.isbn || '暂无' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">分类</span>
          <span class="value cat-tag">{{ book.categoryName }}</span>
        </div>
        <div class="detail-row">
          <span class="label">库存</span>
          <span class="value" :class="{ 'low-stock': book.stock < 10 }">{{ book.stock }} 本</span>
        </div>

        <div class="price-box">
          <span class="price-symbol">&yen;</span>
          <span class="price-number">{{ book.price }}</span>
        </div>

        <div class="detail-actions">
          <el-button type="primary" size="large" :disabled="!userStore.isLoggedIn || userStore.isAdmin || book.stock <= 0"
            @click="addCart" class="add-cart-btn">
            <el-icon><ShoppingCart /></el-icon> 加入购物车
          </el-button>
          <p v-if="!userStore.isLoggedIn" class="login-tip">
            请先 <router-link to="/login">登录</router-link> 成为会员后购买
          </p>
        </div>
      </div>
    </div>

    <div class="detail-section" v-if="book?.description">
      <h3 class="section-title">图书简介</h3>
      <div class="desc-content">{{ book.description }}</div>
    </div>

    <div class="detail-section">
      <h3 class="section-title">用户评价（{{ reviews.length }}）</h3>
      <div class="review-list" v-if="reviews.length">
        <div class="review-item" v-for="r in reviews" :key="r.id">
          <div class="review-head">
            <span class="review-user">{{ r.username }}</span>
            <el-rate v-model="r.rating" disabled show-score size="small" />
            <span class="review-date">{{ r.createdAt }}</span>
          </div>
          <div class="review-body">{{ r.content }}</div>
          <div class="review-reply" v-if="r.adminReply">
            <span class="reply-label">管理员回复：</span>
            <span>{{ r.adminReply }}</span>
          </div>
        </div>
      </div>
      <div class="no-review" v-else>暂无评价，购买后快来评价吧</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getBookDetail } from '../api/book'
import { addToCart } from '../api/cart'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import api from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const book = ref(null)
const reviews = ref([])
const loading = ref(false)

const gradients = ['#1e3a5f,#3b5998','#1d4ed8,#7c3aed','#059669,#0d9488','#dc2626,#ea580c','#7c3aed,#db2777','#0f766e,#0284c7','#b45309,#b91c1c','#4338ca,#2563eb','#166534,#15803d','#9a3412,#c2410c']
const coverStyle = computed(() => {
  const idx = ((book.value?.id || 1) % gradients.length)
  return { background: `linear-gradient(135deg, ${gradients[idx]})` }
})

async function addCart() {
  try {
    await addToCart(book.value.id)
    cartStore.fetchCount()
    ElMessage.success('已加入购物车')
  } catch {}
}

async function loadReviews() {
  try {
    const res = await api.get(`/books/${route.params.id}/reviews`)
    reviews.value = res.data
  } catch {}
}

onMounted(async () => {
  loading.value = true
  try {
    const [bk, _] = await Promise.all([getBookDetail(route.params.id), loadReviews()])
    book.value = bk.data
  } finally { loading.value = false }
})
</script>

<style scoped>
.detail-page { max-width: 960px; margin: 0 auto; }
.breadcrumb {
  font-size: 13px; color: var(--text-secondary); margin-bottom: 24px;
  display: flex; gap: 8px; align-items: center;
}
.breadcrumb a { color: var(--text-secondary); text-decoration: none; }
.breadcrumb a:hover { color: var(--primary); }
.breadcrumb .current { color: var(--text); font-weight: 500; }

.detail-card {
  background: var(--white); border-radius: var(--radius);
  padding: 36px; display: flex; gap: 44px;
  border: 1px solid var(--border);
}

.cover-img { width: 300px; height: 400px; border-radius: var(--radius-sm); flex-shrink: 0; }
.cover-fb {
  width: 300px; height: 400px; border-radius: var(--radius-sm);
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; gap: 8px; background: linear-gradient(135deg, #e0e7ff, #dbeafe);
  color: #6366f1; font-size: 32px; font-weight: 700;
}
.detail-cover-css {
  width: 300px; height: 400px; border-radius: var(--radius-sm); flex-shrink: 0;
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; text-align: center; gap: 14px;
  color: rgba(255,255,255,0.92); padding: 32px 24px;
}
.dc-cat {
  font-size: 12px; letter-spacing: 3px; text-transform: uppercase;
  border: 1px solid rgba(255,255,255,0.45); padding: 3px 16px; border-radius: 3px;
}
.dc-title { font-size: 26px; font-weight: 800; line-height: 1.3; }
.dc-author { font-size: 14px; opacity: 0.75; font-style: italic; }

.detail-info-col { flex: 1; }
.detail-badge {
  display: inline-block; font-size: 12px; font-weight: 600;
  padding: 4px 12px; border-radius: 4px; background: #ecfdf5; color: #059669;
  margin-bottom: 8px;
}
.detail-badge.soldout { background: #fef2f2; color: #dc2626; }

.detail-title { font-size: 26px; font-weight: 700; margin-bottom: 20px; line-height: 1.3; }

.detail-row {
  display: flex; gap: 20px; padding: 8px 0;
  font-size: 14px; border-bottom: 1px solid #f3f4f6;
}
.detail-row .label { color: var(--text-secondary); width: 48px; flex-shrink: 0; }
.cat-tag {
  background: var(--primary-light); color: var(--primary);
  padding: 1px 10px; border-radius: 4px; font-size: 12px; font-weight: 500;
}
.low-stock { color: var(--danger); font-weight: 600; }

.price-box { margin: 24px 0; }
.price-symbol { font-size: 20px; color: var(--danger); font-weight: 600; }
.price-number { font-size: 38px; color: var(--danger); font-weight: 800; letter-spacing: -1px; }

.detail-actions { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.add-cart-btn { padding: 14px 32px; font-size: 16px; height: auto; border-radius: 10px; }
.login-tip { font-size: 13px; color: var(--text-secondary); }
.login-tip a { color: var(--primary); font-weight: 600; }

.detail-section { margin-top: 32px; background: var(--white); border-radius: var(--radius); padding: 32px 36px; border: 1px solid var(--border); }
.section-title { font-size: 18px; font-weight: 700; margin-bottom: 16px; }
.desc-content { color: var(--text-secondary); line-height: 2; font-size: 15px; }

.review-item { padding: 18px 0; border-bottom: 1px solid #f3f4f6; }
.review-item:last-child { border-bottom: none; }
.review-head { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.review-user { font-weight: 600; font-size: 14px; }
.review-date { font-size: 12px; color: var(--text-secondary); margin-left: auto; }
.review-body { font-size: 14px; color: var(--text); line-height: 1.6; }
.review-reply { margin-top: 10px; padding: 10px 14px; background: #f0fdf4; border-radius: 6px; font-size: 13px; color: #166534; border-left: 3px solid #22c55e; }
.reply-label { font-weight: 600; }
.no-review { text-align: center; color: var(--text-secondary); padding: 20px 0; font-size: 14px; }

</style>
