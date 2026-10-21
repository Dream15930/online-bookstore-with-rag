<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-icon admin"><el-icon :size="32"><Lock /></el-icon></div>
        <h2>管理员登录</h2>
        <p>仅限系统管理员使用</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员账号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" @click="handleLogin" :loading="loading" size="large" class="submit-btn">
          管理员登录
        </el-button>
      </el-form>
      <div class="auth-footer">
        <router-link to="/">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '../../api/auth'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await adminLogin(form)
    userStore.setUser(res.data)
    ElMessage.success('管理员登录成功')
    router.push('/admin')
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding-top: 60px; }
.auth-card {
  width: 400px; background: var(--white); padding: 44px 40px;
  border-radius: var(--radius); border: 1px solid var(--border);
  box-shadow: var(--shadow-md);
}
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-icon {
  width: 64px; height: 64px; border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center; margin-bottom: 16px;
}
.auth-icon.admin { background: #fef3c7; color: #d97706; }
.auth-header h2 { font-size: 22px; font-weight: 700; }
.auth-header p { font-size: 14px; color: var(--text-secondary); margin-top: 6px; }
.submit-btn { width: 100%; height: 44px; font-size: 16px; border-radius: 10px; font-weight: 600; }
.auth-footer { text-align: center; margin-top: 20px; font-size: 14px; }
.auth-footer a { color: var(--text-secondary); }
</style>
