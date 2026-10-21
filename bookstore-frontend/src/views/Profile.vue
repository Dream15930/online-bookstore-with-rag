<template>
  <div class="profile-page">
    <h2>个人中心</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="个人信息" name="info">
        <div class="form-card">
          <el-form :model="profile" label-width="80px" label-position="top">
            <el-form-item label="用户名">
              <el-input v-model="profile.username" size="large" />
            </el-form-item>
            <el-form-item label="新密码（留空不修改）">
              <el-input v-model="profile.password" type="password" size="large" show-password placeholder="留空则不修改密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="收货地址" name="address">
        <div class="form-card">
          <div class="addr-header">
            <span>管理收货地址</span>
            <el-button type="primary" size="small" @click="openAddAddr"><el-icon><Plus /></el-icon> 添加地址</el-button>
          </div>

          <div class="addr-list" v-if="addresses.length">
            <div v-for="addr in addresses" :key="addr.id" class="addr-item" :class="{ default: addr.isDefault === 1 }">
              <div class="addr-text">
                <el-tag v-if="addr.isDefault === 1" type="danger" size="small" class="default-tag">默认</el-tag>
                <span>{{ addr.address }}</span>
              </div>
              <div class="addr-actions">
                <el-button v-if="addr.isDefault !== 1" size="small" @click="setDefault(addr.id)">设为默认</el-button>
                <el-button size="small" @click="openEditAddr(addr)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteAddr(addr.id)">删除</el-button>
              </div>
            </div>
          </div>
          <div class="empty" v-else>暂无收货地址</div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Address Dialog -->
    <el-dialog :title="editingAddr ? '编辑地址' : '新增地址'" v-model="addrDialog" width="500px" destroy-on-close>
      <el-input v-model="addrForm.address" type="textarea" :rows="3" placeholder="请输入收货人、电话及详细地址" />
      <template #footer>
        <el-button @click="addrDialog=false">取消</el-button>
        <el-button type="primary" @click="saveAddr" :loading="addrSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { getMe } from '../api/auth'

const userStore = useUserStore()
const activeTab = ref('info')
const saving = ref(false)

// Profile
const profile = reactive({ username: '', password: '' })
async function loadProfile() {
  const res = await getMe()
  profile.username = res.data.username
  profile.password = ''
}
async function saveProfile() {
  saving.value = true
  try {
    await api.put('/user/profile', { username: profile.username, password: profile.password || '' })
    ElMessage.success('保存成功')
    userStore.fetchUser()
  } finally { saving.value = false }
}

// Addresses
const addresses = ref([])
const addrDialog = ref(false)
const addrSaving = ref(false)
const editingAddr = ref(null)
const addrForm = reactive({ address: '' })

async function loadAddresses() {
  const res = await api.get('/user/addresses')
  addresses.value = res.data
}
function openAddAddr() { editingAddr.value = null; addrForm.address = ''; addrDialog.value = true }
function openEditAddr(addr) { editingAddr.value = addr; addrForm.address = addr.address; addrDialog.value = true }
async function saveAddr() {
  if (!addrForm.address.trim()) { ElMessage.warning('请输入地址'); return }
  addrSaving.value = true
  try {
    if (editingAddr.value) {
      await api.put(`/user/addresses/${editingAddr.value.id}`, { address: addrForm.address })
    } else {
      await api.post('/user/addresses', { address: addrForm.address })
    }
    ElMessage.success('保存成功')
    addrDialog.value = false
    loadAddresses()
  } finally { addrSaving.value = false }
}
async function deleteAddr(id) {
  await ElMessageBox.confirm('确定删除此地址？', '提示', { type: 'warning' })
  await api.delete(`/user/addresses/${id}`)
  ElMessage.success('已删除')
  loadAddresses()
}
async function setDefault(id) {
  await api.put(`/user/addresses/${id}/default`)
  ElMessage.success('已设为默认地址')
  loadAddresses()
}

onMounted(() => { loadProfile(); loadAddresses() })
</script>

<style scoped>
.profile-page { max-width: 720px; margin: 0 auto; }
.profile-page h2 { font-size: 22px; font-weight: 700; margin-bottom: 20px; }
.form-card { background: var(--white); border-radius: var(--radius); border: 1px solid var(--border); padding: 28px 32px; }

.addr-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.addr-item { display: flex; justify-content: space-between; align-items: center; padding: 16px; border: 1px solid var(--border); border-radius: 8px; margin-bottom: 10px; transition: border-color 0.2s; }
.addr-item.default { border-color: var(--danger); background: #fef2f2; }
.addr-text { display: flex; align-items: center; gap: 8px; flex: 1; font-size: 14px; }
.default-tag { flex-shrink: 0; }
.addr-actions { display: flex; gap: 6px; flex-shrink: 0; }
.empty { text-align: center; color: var(--text-secondary); padding: 40px 0; }
</style>
