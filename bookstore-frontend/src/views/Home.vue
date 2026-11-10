<template>
  <div class="home">
    <!-- ─── hero tile: the offer, the action, the artifacts ─── -->
    <section class="tile tile-light hero screen-hero" ref="heroEl">
      <h1 class="hero-title">发现你的下一本好书</h1>
      <p class="hero-tagline">海量精选图书 · 正版保障 · 即刻阅读</p>

      <form class="hero-search" @submit.prevent="search">
        <span class="search-field">
          <el-icon :size="15"><Search /></el-icon>
          <input v-model="keyword" class="search-input" placeholder="搜索书名、作者、关键词" />
        </span>
        <button class="pill" type="submit">搜索</button>
      </form>

      <div class="artifacts" v-if="featured.length">
        <img v-for="b in featured" :key="b.id" :src="b.coverUrl" :alt="b.title"
          class="artifact" @click="go(b)" />
      </div>
    </section>

    <!-- ─── catalog grid ─── -->
    <section class="tile tile-parchment catalog screen-catalog" ref="catalogEl">
      <div class="catalog-head">
        <h2>{{ heading }}</h2>
        <span class="catalog-count" v-if="total">共 {{ total }} 本</span>
      </div>

      <div class="chips">
        <button class="chip" :class="{ on: categoryId === null }" @click="selectCategory(null)">全部</button>
        <button v-for="c in categories" :key="c.id" class="chip"
          :class="{ on: categoryId === c.id }" @click="selectCategory(c.id)">{{ c.name }}</button>
      </div>

      <div class="grid" v-loading="loading">
        <article v-for="book in books" :key="book.id" class="card" @click="go(book)">
          <div class="card-media">
            <img v-if="book.coverUrl" :src="book.coverUrl" :alt="book.title" />
            <div v-else class="card-fallback">{{ book.title }}</div>
          </div>
          <h3 class="card-title">{{ book.title }}</h3>
          <p class="card-author">{{ book.author }}</p>
          <p class="card-price">&yen;{{ book.price }}</p>
          <span class="card-link">翻开看看</span>
        </article>
      </div>

      <div class="empty" v-if="!loading && !books.length">未找到相关图书</div>

      <div class="pager">
        <el-pagination v-if="total" layout="prev, pager, next" :total="total" :page-size="pageSize"
          v-model:current-page="page" @current-change="loadBooks" />
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getBooks, getCategories } from '../api/book'

const router = useRouter()

const keyword = ref('')
const categoryId = ref(null)
const books = ref([])
const categories = ref([])
const featured = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 4
const total = ref(0)
const heroEl = ref(null)
const catalogEl = ref(null)

const heading = computed(() => (keyword.value || categoryId.value) ? '搜索结果' : '全部图书')

function go(book) { router.push(`/book/${book.id}`) }

/* ── 整屏翻页：接管滚轮，用可控时长的缓出动画（CSS 吸附的时长由浏览器固定，改不了） ── */
const PAGE_MS = 900
let paging = false

function pageTops() {
  const tops = [heroEl, catalogEl]
    .map(r => (r.value ? Math.round(r.value.getBoundingClientRect().top + window.scrollY) : null))
    .filter(t => t !== null)
  const max = Math.round(document.documentElement.scrollHeight - window.innerHeight)
  // 文档末尾离最后一屏很近时不再单列一站——否则最后一屏后还能空滚一小段
  if (tops.length && max > tops[tops.length - 1] + 64) tops.push(max)
  return tops
}

function animateScrollTo(target, duration = PAGE_MS) {
  const start = window.scrollY
  const delta = target - start
  if (Math.abs(delta) < 2) { paging = false; return }
  const t0 = performance.now()
  const easeOut = p => 1 - Math.pow(1 - p, 4)
  paging = true
  const step = now => {
    const p = Math.min(1, (now - t0) / duration)
    window.scrollTo(0, start + delta * easeOut(p))
    if (p < 1) requestAnimationFrame(step)
    else paging = false
  }
  requestAnimationFrame(step)
}

/** 该方向上的下一个停靠点；到顶/到底返回 null，把滚动交还给浏览器 */
function nextStop(dir) {
  const tops = pageTops()
  const y = window.scrollY
  const eps = 8
  return dir > 0
    ? tops.find(t => t > y + eps)
    : [...tops].reverse().find(t => t < y - eps)
}

function onWheel(e) {
  if (window.innerWidth < 834) return
  if (paging) { e.preventDefault(); return }
  const target = nextStop(e.deltaY > 0 ? 1 : -1)
  if (target == null) return
  e.preventDefault()
  animateScrollTo(target)
}

const PAGE_KEYS = { ArrowDown: 1, PageDown: 1, ' ': 1, ArrowUp: -1, PageUp: -1 }
function onKeydown(e) {
  if (window.innerWidth < 834) return
  const dir = PAGE_KEYS[e.key]
  if (!dir) return
  if (paging) { e.preventDefault(); return }
  const target = nextStop(dir)
  if (target == null) return
  e.preventDefault()
  animateScrollTo(target)
}

/** 搜索/筛选后把结果那一屏带到眼前——结果不在搜索框那一屏 */
function search() {
  page.value = 1
  loadBooks()
  nextTick(() => {
    if (catalogEl.value) animateScrollTo(Math.round(catalogEl.value.getBoundingClientRect().top + window.scrollY))
  })
}

function selectCategory(id) { categoryId.value = id; search() }

async function loadBooks() {
  loading.value = true
  try {
    const res = await getBooks({ page: page.value, size: pageSize, keyword: keyword.value, categoryId: categoryId.value })
    books.value = res.data.records
    total.value = res.data.total
    if (!featured.value.length) {
      featured.value = res.data.records.filter(b => b.coverUrl).slice(0, 4)
    }
  } finally { loading.value = false }
}

onMounted(async () => {
  document.documentElement.classList.add('home-snap')
  window.addEventListener('wheel', onWheel, { passive: false })
  window.addEventListener('keydown', onKeydown)
  const catRes = await getCategories()
  categories.value = catRes.data
  loadBooks()
})

onUnmounted(() => {
  document.documentElement.classList.remove('home-snap')
  window.removeEventListener('wheel', onWheel)
  window.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
/* ─── hero ─── */
.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.hero-title {
  max-width: 760px;
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 600;
  line-height: 1.1;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.hero-tagline {
  margin-top: 12px;
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 400;
  line-height: 1.14;
  letter-spacing: 0.196px;
  color: var(--ink);
}
.hero-search {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  max-width: 560px;
  margin-top: 32px;
}
.search-field {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 20px;
  background: var(--canvas);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: var(--radius-pill);
  color: var(--ink-muted-80);
}
.search-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: none;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.search-input::placeholder { color: var(--ink-muted-80); }

.artifacts {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
  margin-top: 56px;
}
.artifact {
  width: 132px;
  height: 176px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  box-shadow: var(--product-shadow);
  cursor: pointer;
}

/* ─── catalog ─── */
.catalog-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  max-width: 1200px;
  margin: 0 auto 16px;
}
.catalog-head h2 {
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 600;
  line-height: 1.47;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.catalog-count {
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink-muted-80);
}
.chips {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  max-width: 1200px;
  margin: 0 auto 16px;
}
.chip {
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-pill);
  padding: 10px 16px;
  cursor: pointer;
}
.chip:hover { border-color: var(--primary); color: var(--primary); }
.chip.on {
  border: 2px solid var(--primary-focus);
  padding: 9px 15px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.card {
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
  cursor: pointer;
}
.card-media {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 180px;
  padding: 16px;
  background: var(--canvas-parchment);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.card-media img { width: 100%; height: 100%; object-fit: contain; }
.card-fallback {
  padding: 16px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
  text-align: center;
}
.card-title {
  margin-top: 20px;
  font-family: var(--font-text);
  font-size: 17px;
  font-weight: 600;
  line-height: 1.24;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.card-author {
  margin-top: 4px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.card-price {
  margin-top: 10px;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.card-link {
  margin-top: 14px;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--primary);
}
.card:hover .card-title { color: var(--primary); }
.card:hover .card-link { text-decoration: underline; }

.empty {
  padding: 40px 0;
  font-family: var(--font-text);
  font-size: 17px;
  color: var(--ink-muted-80);
  text-align: center;
}
/* always occupies its height, so switching categories never shifts the layout */
.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  min-height: 32px;
}

/* ─── full-screen sections: one screen per wheel step (desktop) ─── */
@media (min-width: 834px) {
  /* 一屏 = 整屏；导航与页脚都 fixed，覆盖上下，内容在两段等宽留白之间居中 */
  .screen-hero {
    min-height: 100vh;
    min-height: 100dvh;
    padding: calc(var(--nav-h) + 24px) var(--sp-lg) calc(var(--footer-h) + 24px);
    justify-content: center;
  }
  .screen-catalog {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-height: 100vh;
    min-height: 100dvh;
    padding: calc(var(--nav-h) + 24px) var(--sp-lg) calc(var(--footer-h) + 24px);
  }
}

/* ─── responsive ─── */
@media (max-width: 1068px) {
  .grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 833px) {
  .tile { padding: 48px 20px; }
  .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (max-width: 640px) {
  .grid { grid-template-columns: 1fr; }
  .hero-title { font-size: 34px; }
  .hero-tagline { font-size: 21px; }
  .catalog-head h2 { font-size: 28px; }
  .hero-search { flex-wrap: wrap; }
  .search-field { flex: 1 1 100%; }
  .pill { flex: 1; }
}
@media (max-width: 419px) {
  .hero-title { font-size: 28px; }
}
</style>
