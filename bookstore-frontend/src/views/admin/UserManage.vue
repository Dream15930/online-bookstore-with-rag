<template>
  <div class="admin-page">
    <AdminSidebar />
    <div class="admin-main">
      <div class="page-head">
        <h2 class="page-title">用户管理</h2>
        <button v-if="isSuperAdmin" class="pill small" @click="openAddDialog">新增管理员</button>
      </div>

      <div class="admin-panel">
        <el-table :data="users" v-loading="loading">
          <el-table-column label="ID" prop="id" width="60" />
          <el-table-column label="用户名" prop="username" width="150" />
          <el-table-column label="角色" width="120" align="center">
            <template #default="{ row }">
              <span class="chip" :class="{ on: row.role !== 'member' }">
                {{ row.role === 'super_admin' ? '超级管理员' : row.role === 'admin' ? '管理员' : '会员' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="地址" prop="address" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="chip" :class="{ on: row.status === 1 }">{{ row.status === 1 ? '正常' : '停用' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" width="180" align="center">
            <template #default="{ row }">{{ row.createdAt }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <button v-if="canToggle(row)" class="link-btn" @click="toggle(row.id)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </button>
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
        <button class="link-btn" @click="dialogVisible = false">取消</button>
        <button class="pill" :disabled="saving" @click="handleCreate">{{ saving ? '创建中…' : '创建' }}</button>
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

const users = ref([]), loading = ref(false), saving = ref(false)
const dialogVisible = ref(false), formRef = ref(null)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function loadUsers() { loading.value = true; try { const r = await getAdminUsers(); users.value = r.data } finally { loading.value = false } }
async function toggle(id) { await toggleUserStatus(id); ElMessage.success('状态已更新'); loadUsers() }
function openAddDialog() { form.username = ''; form.password = ''; dialogVisible.value = true }
async function handleCreate() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try { await createAdmin(form); ElMessage.success('管理员创建成功'); dialogVisible.value = false; loadUsers() } finally { saving.value = false }
}

onMounted(loadUsers)
</script>

<style scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}
</style>
