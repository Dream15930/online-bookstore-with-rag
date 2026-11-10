<template>
  <div class="profile-page">
    <h2 class="page-title">个人中心</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="个人信息" name="info">
        <div class="card">
          <el-form :model="profile" label-position="top">
            <el-form-item label="用户名">
              <el-input v-model="profile.username" size="large" />
            </el-form-item>
            <el-form-item label="新密码（留空不修改）">
              <el-input v-model="profile.password" type="password" size="large" show-password
                placeholder="留空则不修改密码" />
            </el-form-item>
            <button class="pill" type="button" :disabled="saving" @click="saveProfile">
              {{ saving ? '保存中…' : '保存修改' }}
            </button>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="收货地址" name="address">
        <div class="card">
          <div class="addr-head">
            <span class="sub-title">管理收货地址</span>
            <button class="pill small" @click="openAddAddr">添加地址</button>
          </div>

          <div class="addr-list" v-if="addresses.length">
            <div v-for="addr in addresses" :key="addr.id" class="addr-item" :class="{ on: addr.isDefault === 1 }">
              <div class="addr-text">
                <span class="badge" v-if="addr.isDefault === 1">默认</span>
                <span>{{ addr.address }}</span>
              </div>
              <div class="addr-actions">
                <button v-if="addr.isDefault !== 1" class="link-btn" @click="setDefault(addr.id)">设为默认</button>
                <button class="link-btn" @click="openEditAddr(addr)">编辑</button>
                <button class="link-btn" @click="deleteAddr(addr.id)">删除</button>
              </div>
            </div>
          </div>
          <div class="empty" v-else>暂无收货地址</div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="editingAddr ? '编辑地址' : '新增地址'" v-model="addrDialog" width="500px" destroy-on-close>
      <el-input v-model="addrForm.address" type="textarea" :rows="3" placeholder="请输入收货人、电话及详细地址" />
      <template #footer>
        <button class="link-btn" @click="addrDialog = false">取消</button>
        <button class="pill" :disabled="addrSaving" @click="saveAddr">
          {{ addrSaving ? '保存中…' : '保存' }}
        </button>
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
.profile-page {
  max-width: 760px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}
.card {
  margin-top: 24px;
  padding: 28px 32px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
}

.addr-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.sub-title {
  font-family: var(--font-text);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.addr-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px;
  margin-bottom: 10px;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-md);
}
.addr-item.on { border-color: var(--primary); }
.addr-text {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.badge {
  flex-shrink: 0;
  padding: 2px 10px;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-pill);
  font-family: var(--font-text);
  font-size: 12px;
  letter-spacing: -0.12px;
  color: var(--ink-muted-80);
}
.addr-actions { display: flex; gap: 16px; flex-shrink: 0; }

.empty {
  padding: 40px 0;
  text-align: center;
  font-family: var(--font-text);
  font-size: 17px;
  color: var(--ink-muted-80);
}

:deep(.el-tabs__item) {
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
}
:deep(.el-form-item__label) {
  padding-bottom: 6px;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
:deep(.el-input__wrapper) {
  padding: 4px 14px;
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--hairline) inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--primary-focus) inset;
}

@media (max-width: 833px) {
  .profile-page { padding: 32px 20px 64px; }
  .card { padding: 24px 20px; }
}
</style>
