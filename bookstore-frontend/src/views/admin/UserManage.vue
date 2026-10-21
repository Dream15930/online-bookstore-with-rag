<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <div class="page-header">
        <h2>用户管理</h2>
        <el-button v-if="isSuperAdmin" type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon> 新增管理员</el-button>
      </div>
      <div class="table-card">
        <el-table :data="users" v-loading="loading" border stripe>
          <el-table-column label="ID" prop="id" width="60" />
          <el-table-column label="用户名" prop="username" width="150" />
          <el-table-column label="角色" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.role==='super_admin'?'danger':row.role==='admin'?'warning':'primary'" size="small">
                {{ row.role==='super_admin'?'超级管理员':row.role==='admin'?'管理员':'会员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="地址" prop="address" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'正常':'停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" width="180" align="center">
            <template #default="{ row }">{{ row.createdAt }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button v-if="canToggle(row)" size="small" :type="row.status===1?'warning':'success'" @click="toggle(row.id)">
                {{ row.status===1?'停用':'启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog title="新增管理员" v-model="dialogVisible" width="420px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="saving">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import AdminSidebar from './AdminSidebar.vue'
import { getAdminUsers, toggleUserStatus, createAdmin } from '../../api/admin'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isSuperAdmin = computed(() => userStore.user?.role === 'super_admin')

function canToggle(row) {
  if (row.role === 'super_admin') return false
  if (row.role === 'admin' && !isSuperAdmin.value) return false
  return true
}

const users=ref([]), loading=ref(false), saving=ref(false)
const dialogVisible=ref(false), formRef=ref(null)
const form=reactive({username:'',password:''})
const rules={
  username:[{required:true,message:'请输入用户名',trigger:'blur'}],
  password:[{required:true,message:'请输入密码',trigger:'blur'}]
}

async function loadUsers() { loading.value=true; try { const r=await getAdminUsers(); users.value=r.data } finally { loading.value=false } }
async function toggle(id) { await toggleUserStatus(id); ElMessage.success('状态已更新'); loadUsers() }
function openAddDialog() { form.username=''; form.password=''; dialogVisible.value=true }
async function handleCreate() {
  const valid = await formRef.value.validate().catch(()=>false)
  if (!valid) return
  saving.value=true
  try { await createAdmin(form); ElMessage.success('管理员创建成功'); dialogVisible.value=false; loadUsers() } finally { saving.value=false }
}

onMounted(loadUsers)
</script>

<style scoped>
.admin-page { display: flex; gap: 24px; }
.admin-main { flex: 1; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 22px; font-weight: 700; margin: 0; }
.table-card { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); overflow: hidden; }
</style>
