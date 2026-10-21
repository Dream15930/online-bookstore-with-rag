<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <div class="page-header">
        <h2>图书管理</h2>
        <el-button type="primary" @click="openAdd"><el-icon><Plus /></el-icon> 新增图书</el-button>
      </div>
      <div class="table-card">
        <el-table :data="books" v-loading="loading" border stripe>
          <el-table-column label="ID" prop="id" width="60" />
          <el-table-column label="书名" prop="title" min-width="180" />
          <el-table-column label="作者" prop="author" width="120" />
          <el-table-column label="分类" prop="categoryName" width="100" />
          <el-table-column label="价格" width="100" align="center">
            <template #default="{ row }"><b>&yen;{{ row.price }}</b></template>
          </el-table-column>
          <el-table-column label="库存" prop="stock" width="80" align="center" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'上架':'下架' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="{ row }">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" :type="row.status===1?'warning':'success'" @click="toggle(row.id)">{{ row.status===1?'下架':'上架' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination" v-if="total > pageSize">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page" @current-change="loadBooks" />
      </div>
    </div>

    <el-dialog :title="editing?'编辑图书':'新增图书'" v-model="dialogVisible" width="560px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="书名"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="作者"><el-input v-model="form.author" /></el-form-item>
        <el-form-item label="ISBN"><el-input v-model="form.isbn" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="form.coverUrl" placeholder="图片URL" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import { getAdminBooks, addBook, updateBook, toggleBookStatus } from '../../api/admin'
import { getCategories } from '../../api/book'
import { ElMessage } from 'element-plus'

const books=ref([]), categories=ref([]), loading=ref(false), saving=ref(false)
const page=ref(1), pageSize=10, total=ref(0)
const dialogVisible=ref(false), editing=ref(false)
const form=ref({})

async function loadBooks() { loading.value=true; try { const r=await getAdminBooks({page:page.value,size:pageSize}); books.value=r.data.records; total.value=r.data.total } finally { loading.value=false } }
function openAdd() { editing.value=false; form.value={title:'',author:'',isbn:'',categoryId:null,price:0,stock:0,coverUrl:'',description:''}; dialogVisible.value=true }
function openEdit(row) { editing.value=true; form.value={...row}; dialogVisible.value=true }
async function handleSave() { saving.value=true; try { editing.value?await updateBook(form.value.id,form.value):await addBook(form.value); ElMessage.success(editing.value?'更新成功':'添加成功'); dialogVisible.value=false; loadBooks() } finally { saving.value=false } }
async function toggle(id) { await toggleBookStatus(id); ElMessage.success('状态已更新'); loadBooks() }

onMounted(async () => { const r=await getCategories(); categories.value=r.data; loadBooks() })
</script>

<style scoped>
.admin-page { display: flex; gap: 24px; }
.admin-main { flex: 1; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 22px; font-weight: 700; margin: 0; }
.table-card { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); overflow: hidden; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
