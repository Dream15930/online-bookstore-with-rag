<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <h2 class="page-title">评价管理</h2>

      <div class="admin-panel table-panel">
        <el-table :data="pagedReviews" v-loading="loading" empty-text="暂无评价">
          <el-table-column label="图书" prop="bookTitle" min-width="160" show-overflow-tooltip />
          <el-table-column label="用户" prop="username" width="120" />
          <el-table-column label="评分" width="160" align="center">
            <template #default="{ row }">
              <el-rate v-model="row.rating" disabled show-score size="small" />
            </template>
          </el-table-column>
          <el-table-column label="评价内容" prop="content" min-width="220" show-overflow-tooltip />
          <el-table-column label="时间" width="180" align="center">
            <template #default="{ row }">{{ row.createdAt }}</template>
          </el-table-column>
          <el-table-column label="回复状态" width="110" align="center">
            <template #default="{ row }">
              <span class="chip" :class="{ on: !!row.adminReply }">{{ row.adminReply ? '已回复' : '待回复' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center">
            <template #default="{ row }">
              <button class="link-btn" @click="openReply(row)">{{ row.adminReply ? '编辑' : '回复' }}</button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pager">
        <el-pagination v-if="reviews.length" layout="prev, pager, next" :total="reviews.length"
          :page-size="pageSize" v-model:current-page="page" />
      </div>
    </div>

    <el-dialog title="回复评价" v-model="dialogVisible" width="520px" destroy-on-close>
      <div class="review-preview">
        <div class="preview-row"><b>图书：</b>{{ curReview?.bookTitle }}</div>
        <div class="preview-row"><b>用户：</b>{{ curReview?.username }}</div>
        <div class="preview-row"><b>评价：</b>{{ curReview?.content }}</div>
      </div>
      <el-input v-model="replyText" type="textarea" :rows="3" placeholder="输入回复内容…" class="reply-input" />
      <template #footer>
        <button class="link-btn" @click="dialogVisible = false">取消</button>
        <button class="pill" :disabled="saving" @click="handleReply">{{ saving ? '提交中…' : '提交回复' }}</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import api from '../../api'
import { ElMessage } from 'element-plus'

const reviews = ref([])
const loading = ref(false)
// 后端该接口返回全量列表，这里做前端分页
const page = ref(1)
const pageSize = 10
const pagedReviews = computed(() =>
  reviews.value.slice((page.value - 1) * pageSize, page.value * pageSize)
)
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
/* reserve a full page of rows so the pager keeps one position across pages */
.table-panel {
  margin-top: 24px;
  min-height: 570px;
}
.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 24px;
  min-height: 32px;
}

.review-preview {
  padding: 16px;
  margin-bottom: 16px;
  background: var(--canvas-parchment);
  border-radius: var(--radius-md);
}
.preview-row {
  margin-bottom: 6px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.preview-row b { color: var(--ink); }
.reply-input { margin-top: 8px; }
</style>
