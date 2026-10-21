<template>
  <div class="home">
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">发现你的<span>下一本好书</span></h1>
        <p class="hero-sub">海量精选图书 · 正版保障 · 即刻阅读</p>
      </div>
      <div class="hero-search">
        <el-input v-model="keyword" placeholder="搜索书名、作者、关键词..." size="large" clearable
          @keyup.enter="search" class="hero-input">
          <template #prefix><el-icon :size="20"><Search /></el-icon></template>
        </el-input>
        <el-select v-model="categoryId" placeholder="全部分类" size="large" clearable @change="search" class="hero-select">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" size="large" @click="search" class="search-btn">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
      </div>
    </section>

    <section class="books-section">
      <div class="section-header">
        <h2>{{ keyword || categoryId ? '搜索结果' : '热门图书' }}</h2>
        <span class="result-count" v-if="total">共 {{ total }} 本</span>
      </div>

      <div class="book-grid" v-loading="loading">
        <div v-for="book in books" :key="book.id" class="book-card" @click="$router.push(`/book/${book.id}`)">
          <div class="card-cover-wrapper">
            <template v-if="book.coverUrl">
              <el-image :src="book.coverUrl" fit="cover" class="card-cover">
                <template #error>
                  <div class="card-cover-css" :style="coverStyle(book)">
                    <span class="cover-cat">{{ book.categoryName }}</span>
                    <span class="cover-title">{{ book.title }}</span>
                    <span class="cover-author">{{ book.author }}</span>
                  </div>
                </template>
              </el-image>
            </template>
            <div v-else class="card-cover-css" :style="coverStyle(book)">
              <span class="cover-cat">{{ book.categoryName }}</span>
              <span class="cover-title">{{ book.title }}</span>
              <span class="cover-author">{{ book.author }}</span>
            </div>
            <div class="card-overlay">
              <span class="view-detail">查看详情</span>
            </div>
          </div>
          <div class="card-info">
            <div class="card-category" v-if="book.categoryName">{{ book.categoryName }}</div>
            <div class="card-title" :title="book.title">{{ book.title }}</div>
            <div class="card-author">{{ book.author }}</div>
            <div class="card-footer">
              <span class="card-price">&yen;{{ book.price }}</span>
              <span class="card-stock" v-if="book.stock > 0">库存 {{ book.stock }}</span>
              <span class="card-stock out" v-else>已售罄</span>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!loading && !books.length">
        <el-icon :size="48"><Search /></el-icon>
        <p>未找到相关图书</p>
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize"
          v-model:current-page="page" @current-change="loadBooks" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBooks, getCategories } from '../api/book'

const keyword = ref('')
const categoryId = ref(null)
const books = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 12
const total = ref(0)

function search() { page.value = 1; loadBooks() }

const gradients = [
  'linear-gradient(135deg, #1e3a5f 0%, #3b5998 100%)',
  'linear-gradient(135deg, #1d4ed8 0%, #7c3aed 100%)',
  'linear-gradient(135deg, #059669 0%, #0d9488 100%)',
  'linear-gradient(135deg, #dc2626 0%, #ea580c 100%)',
  'linear-gradient(135deg, #7c3aed 0%, #db2777 100%)',
  'linear-gradient(135deg, #0f766e 0%, #0284c7 100%)',
  'linear-gradient(135deg, #b45309 0%, #b91c1c 100%)',
  'linear-gradient(135deg, #4338ca 0%, #2563eb 100%)',
  'linear-gradient(135deg, #166534 0%, #15803d 100%)',
  'linear-gradient(135deg, #9a3412 0%, #c2410c 100%)',
]
function coverStyle(book) {
  const idx = (book.id || 1) % gradients.length
  return { background: gradients[idx] }
}

async function loadBooks() {
  loading.value = true
  try {
    const res = await getBooks({ page: page.value, size: pageSize, keyword: keyword.value, categoryId: categoryId.value })
    books.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(async () => {
  const catRes = await getCategories()
  categories.value = catRes.data
  loadBooks()
})
</script>

<style scoped>
/* Hero */
.hero {
  background: linear-gradient(135deg, #1e3a5f 0%, #3b5998 40%, #2563eb 100%);
  border-radius: var(--radius);
  padding: 48px 40px;
  margin-bottom: 36px;
  color: #fff;
  position: relative;
  overflow: hidden;
}
.hero::after {
  content: '';
  position: absolute;
  right: -40px;
  top: -40px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
}
.hero-content { position: relative; z-index: 1; }
.hero-title { font-size: 34px; font-weight: 800; letter-spacing: -1px; }
.hero-title span { color: #fbbf24; }
.hero-sub { margin-top: 10px; font-size: 16px; opacity: 0.8; }
.hero-search {
  display: flex;
  gap: 12px;
  margin-top: 28px;
  position: relative;
  z-index: 1;
}
.hero-input { flex: 1; }
.hero-select { width: 150px; flex-shrink: 0; }
.search-btn { padding: 0 28px; font-weight: 600; }

/* Books */
.books-section { margin-top: 20px; }
.section-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}
.section-header h2 { font-size: 22px; font-weight: 700; }
.result-count { font-size: 14px; color: var(--text-secondary); }

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
}
.book-card {
  background: var(--white);
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border);
  transition: all var(--transition);
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.card-cover-wrapper { position: relative; overflow: hidden; }
.card-cover-css {
  width: 100%; height: 280px; padding: 24px 20px;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  text-align: center; gap: 10px;
  color: rgba(255,255,255,0.9);
}
.cover-cat {
  font-size: 11px; text-transform: uppercase; letter-spacing: 2px;
  border: 1px solid rgba(255,255,255,0.4); padding: 2px 12px; border-radius: 3px;
}
.cover-title {
  font-size: 20px; font-weight: 800; line-height: 1.3;
  display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical;
  overflow: hidden; max-width: 100%;
}
.cover-author {
  font-size: 12px; opacity: 0.7; font-style: italic;
}

.card-overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity var(--transition);
}
.book-card:hover .card-overlay { opacity: 1; }
.view-detail {
  color: #fff; font-weight: 600; font-size: 14px;
  border: 2px solid #fff; padding: 8px 24px; border-radius: 24px;
}

.card-info { padding: 14px 16px 16px; }
.card-category {
  font-size: 11px; text-transform: uppercase; letter-spacing: 0.5px;
  color: var(--primary); background: var(--primary-light);
  display: inline-block; padding: 2px 10px; border-radius: 4px; margin-bottom: 8px;
}
.card-title {
  font-size: 15px; font-weight: 600; line-height: 1.4;
  overflow: hidden; text-overflow: ellipsis; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical;
}
.card-author { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }
.card-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 12px; padding-top: 10px; border-top: 1px solid var(--border);
}
.card-price { font-size: 20px; color: var(--danger); font-weight: 800; }
.card-stock { font-size: 12px; color: var(--text-secondary); }
.card-stock.out { color: var(--danger); }

.empty-state { text-align: center; padding: 60px 0; color: var(--text-secondary); }
.empty-state p { margin-top: 12px; }

.pagination { display: flex; justify-content: center; margin-top: 40px; }
</style>
