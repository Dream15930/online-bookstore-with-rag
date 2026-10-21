<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <h2>评价管理</h2>
      <div class="table-card">
        <el-table :data="reviews" v-loading="loading" border stripe empty-text="暂无评价">
          <el-table-column label="图书" prop="bookTitle" min-width="160" show-overflow-tooltip />
          <el-table-column label="用户" prop="username" width="120" />
          <el-table-column label="评分" width="160" align="center">
            <template #default="{ row }">
              <el-rate v-model="row.rating" disabled show-score size="small" />
            </template>
          </el-table-column>
          <el-table-column label="评价内容" prop="content" min-width="220" show-overflow-tooltip />
          <el-table-column label="时间" width="160" align="center">
            <template #default="{ row }">{{ row.createdAt }}</template>
          </el-table-column>
          <el-table-column label="回复状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.adminReply ? 'success' : 'info'" size="small">{{ row.adminReply ? '已回复' : '待回复' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button size="small" @click="openReply(row)">{{ row.adminReply ? '编辑' : '回复' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog title="回复评价" v-model="dialogVisible" width="520px" destroy-on-close>
      <div class="review-preview">
        <div class="preview-row"><b>图书：</b>{{ curReview?.bookTitle }}</div>
        <div class="preview-row"><b>用户：</b>{{ curReview?.username }}</div>
        <div class="preview-row"><b>评价：</b>{{ curReview?.content }}</div>
      </div>
      <el-input v-model="replyText" type="textarea" :rows="3" placeholder="输入回复内容..." class="reply-input" />
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleReply" :loading="saving">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import api from '../../api'
import { ElMessage } from 'element-plus'

const reviews = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const curReview = ref(null)
const replyText = ref('')

async function loadReviews() {
  loading.value = true
  try {
    const res = await api.get('/admin/reviews')
    reviews.value = res.data
  } finally { loading.value = false }
}

function openReply(row) {
  curReview.value = row
  replyText.value = row.adminReply || ''
  dialogVisible.value = true
}

async function handleReply() {
  if (!replyText.value.trim()) { ElMessage.warning('请输入回复内容'); return }
  saving.value = true
  try {
    await api.put(`/admin/reviews/${curReview.value.id}/reply`, { reply: replyText.value })
    ElMessage.success('回复成功')
    dialogVisible.value = false
    loadReviews()
  } finally { saving.value = false }
}

onMounted(loadReviews)
</script>

<style scoped>
.admin-page { display: flex; gap: 24px; }
.admin-main { flex: 1; }
.admin-main h2 { font-size: 22px; font-weight: 700; margin-bottom: 16px; }
.table-card { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); overflow: hidden; }

.review-preview { background: #f9fafb; padding: 14px; border-radius: 8px; margin-bottom: 16px; }
.preview-row { font-size: 14px; margin-bottom: 6px; }
.preview-row b { color: var(--text); }
.reply-input { margin-top: 8px; }
</style>
