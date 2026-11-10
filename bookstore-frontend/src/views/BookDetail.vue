<template>
  <div class="detail" v-loading="loading">
    <nav class="crumbs">
      <router-link to="/" class="crumb-link">首页</router-link>
      <span class="crumb-sep">/</span>
      <span class="crumb-text">{{ book?.categoryName || '图书详情' }}</span>
      <span class="crumb-sep">/</span>
      <span class="crumb-current">{{ book?.title }}</span>
    </nav>

    <!-- product tile -->
    <section class="tile tile-light product" v-if="book">
      <div class="product-media">
        <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" class="product-cover" />
        <div v-else class="cover-fallback">
          <span class="fallback-cat">{{ book.categoryName }}</span>
          <span class="fallback-title">{{ book.title }}</span>
          <span class="fallback-author">{{ book.author }}</span>
        </div>
      </div>

      <div class="product-info">
        <h1 class="product-title">{{ book.title }}</h1>
        <p class="product-author">{{ book.author }}</p>
        <p class="product-price">&yen;{{ book.price }}</p>

        <dl class="spec">
          <div class="spec-row"><dt>ISBN</dt><dd>{{ book.isbn || '暂无' }}</dd></div>
          <div class="spec-row"><dt>分类</dt><dd>{{ book.categoryName }}</dd></div>
          <div class="spec-row">
            <dt>库存</dt>
            <dd :class="{ low: book.stock < 10 }">{{ book.stock }} 本</dd>
          </div>
        </dl>

        <div class="actions">
          <button class="pill" :disabled="!canBuy" @click="addCart">加入购物车</button>
          <p v-if="!userStore.isLoggedIn" class="note">
            请先 <router-link to="/login" class="text-link">登录</router-link> 成为会员后购买
          </p>
          <p v-else-if="book.stock <= 0" class="note">已售罄</p>
        </div>
      </div>
    </section>

    <!-- description tile -->
    <section class="tile tile-parchment" v-if="book?.description">
      <div class="prose">
        <h2 class="section-title">图书简介</h2>
        <p class="section-body">{{ book.description }}</p>
      </div>
    </section>

    <!-- reviews tile -->
    <section class="tile tile-light">
      <div class="prose">
        <h2 class="section-title">用户评价（{{ reviews.length }}）</h2>

        <div class="reviews" v-if="reviews.length">
          <article class="review" v-for="r in reviews" :key="r.id">
            <div class="review-head">
              <span class="review-user">{{ r.username }}</span>
              <el-rate v-model="r.rating" disabled size="small" />
              <span class="review-date">{{ r.createdAt }}</span>
            </div>
            <p class="review-body">{{ r.content }}</p>
            <p class="review-reply" v-if="r.adminReply">
              <span class="reply-label">管理员回复</span>{{ r.adminReply }}
            </p>
          </article>
        </div>
        <p class="no-review" v-else>暂无评价，购买后快来评价吧</p>
      </div>
    </section>
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

const canBuy = computed(() =>
  userStore.isLoggedIn && !userStore.isAdmin && (book.value?.stock ?? 0) > 0
)

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
    const [bk] = await Promise.all([getBookDetail(route.params.id), loadReviews()])
    book.value = bk.data
  } finally { loading.value = false }
})
</script>

<style scoped>
/* ─── breadcrumb ─── */
.crumbs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 24px 0;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.crumb-link {
  color: var(--primary);
  text-decoration: none;
}
.crumb-link:hover { text-decoration: underline; }
.crumb-current { color: var(--ink); }

/* ─── tiles ─── */
.tile { padding: var(--sp-section) var(--sp-lg); }
.tile-light { background: var(--canvas); }
.tile-parchment { background: var(--canvas-parchment); }

/* ─── product ─── */
.product {
  display: flex;
  align-items: center;
  gap: 64px;
  max-width: 1200px;
  margin: 0 auto;
}
.product-media {
  flex: 0 0 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.product-cover {
  width: 100%;
  max-width: 360px;
  border-radius: var(--radius-sm);
  box-shadow: var(--product-shadow);
}
.cover-fallback {
  width: 300px;
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px;
  background: var(--canvas-parchment);
  border-radius: var(--radius-sm);
  box-shadow: var(--product-shadow);
  text-align: center;
}
.fallback-cat {
  font-family: var(--font-text);
  font-size: 12px;
  letter-spacing: -0.12px;
  color: var(--ink-muted-80);
}
.fallback-title {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 600;
  line-height: 1.14;
  letter-spacing: 0.196px;
  color: var(--ink);
}
.fallback-author {
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}

.product-info { flex: 1; min-width: 0; }
.product-title {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.product-author {
  margin-top: 8px;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink-muted-80);
}
.product-price {
  margin-top: 20px;
  font-family: var(--font-text);
  font-size: 24px;
  letter-spacing: -0.374px;
  color: var(--ink);
}

.spec { margin-top: 28px; }
.spec-row {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid var(--hairline);
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
}
.spec-row:last-child { border-bottom: 1px solid var(--hairline); }
.spec-row dt {
  width: 72px;
  flex-shrink: 0;
  color: var(--ink-muted-80);
}
.spec-row dd { color: var(--ink); }
.spec-row dd.low { font-weight: 600; }

.actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-top: 32px;
}
.note {
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.text-link {
  color: var(--primary);
  text-decoration: none;
}
.text-link:hover { text-decoration: underline; }

/* ─── prose sections ─── */
.prose { max-width: 980px; margin: 0 auto; }
.section-title {
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 600;
  line-height: 1.47;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.section-body {
  margin-top: 16px;
  font-family: var(--font-text);
  font-size: 17px;
  line-height: 1.47;
  letter-spacing: -0.374px;
  color: var(--ink);
}

/* ─── reviews ─── */
.reviews { margin-top: 24px; }
.review {
  padding: 24px 0;
  border-top: 1px solid var(--hairline);
}
.review:first-child { border-top: none; }
.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.review-user {
  font-family: var(--font-text);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.review-date {
  margin-left: auto;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.review-body {
  margin-top: 8px;
  font-family: var(--font-text);
  font-size: 17px;
  line-height: 1.47;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.review-reply {
  margin-top: 12px;
  padding: 12px 16px;
  background: var(--canvas-parchment);
  border-radius: var(--radius-md);
  font-family: var(--font-text);
  font-size: 15px;
  letter-spacing: -0.24px;
  color: var(--ink-muted-80);
}
.reply-label {
  margin-right: 6px;
  font-weight: 600;
  color: var(--ink);
}
.no-review {
  padding: 24px 0;
  font-family: var(--font-text);
  font-size: 17px;
  color: var(--ink-muted-80);
}

.review-head :deep(.el-rate) {
  --el-rate-fill-color: var(--ink);
  --el-rate-void-color: var(--hairline);
  --el-rate-disabled-void-color: var(--hairline);
}

/* ─── responsive ─── */
@media (max-width: 833px) {
  .tile { padding: 48px 20px; }
  .crumbs { padding: 16px 20px 0; }
  .product {
    flex-direction: column;
    align-items: stretch;
    gap: 32px;
  }
  .product-media { flex: none; }
  .product-cover { margin: 0 auto; max-width: 260px; }
  .cover-fallback { margin: 0 auto; }
  .product-title { font-size: 34px; }
  .section-title { font-size: 28px; }
}
</style>
