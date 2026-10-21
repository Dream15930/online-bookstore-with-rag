<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-icon"><el-icon :size="32"><Lock /></el-icon></div>
        <h2>欢迎回来</h2>
        <p>登录您的会员账号</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" @click="handleLogin" :loading="loading" size="large" class="submit-btn">
          登录
        </el-button>
      </el-form>
      <div class="auth-footer">还没有账号？<router-link to="/register">立即注册</router-link></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await login(form)
    userStore.setUser(res.data)
    cartStore.fetchCount()
    ElMessage.success('登录成功')
    router.push('/')
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  display: flex; justify-content: center; padding-top: 60px;
}
.auth-card {
  width: 400px; background: var(--white); padding: 44px 40px;
  border-radius: var(--radius); border: 1px solid var(--border);
  box-shadow: var(--shadow-md);
}
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-icon {
  width: 64px; height: 64px; border-radius: 50%; background: var(--primary-light);
  display: inline-flex; align-items: center; justify-content: center;
  color: var(--primary); margin-bottom: 16px;
}
.auth-header h2 { font-size: 22px; font-weight: 700; }
.auth-header p { font-size: 14px; color: var(--text-secondary); margin-top: 6px; }
.submit-btn { width: 100%; height: 44px; font-size: 16px; border-radius: 10px; font-weight: 600; }
.auth-footer { text-align: center; margin-top: 20px; font-size: 14px; color: var(--text-secondary); }
.auth-footer a { color: var(--primary); font-weight: 600; }
</style>
