<template>
  <div class="auth-page">
    <div class="auth-card">
      <header class="auth-header">
        <h2>创建账号</h2>
        <p>注册即可成为会员，开始购书</p>
      </header>

      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="form.address" placeholder="请输入默认收货地址（可选）" size="large" />
        </el-form-item>
        <button class="pill submit" type="button" :disabled="loading" @click="handleRegister">
          {{ loading ? '注册中…' : '注册' }}
        </button>
      </el-form>

      <div class="auth-footer">
        已有账号？<router-link to="/login" class="text-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '', address: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await register(form)
    userStore.setUser(res.data)
    ElMessage.success('注册成功，您已成为会员')
    router.push('/')
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  min-height: calc(100vh - var(--nav-h) - var(--footer-h));
  min-height: calc(100dvh - var(--nav-h) - var(--footer-h));
  padding: 80px 24px;
  background: var(--canvas-parchment);
}
.auth-card {
  width: 100%;
  max-width: 440px;
  align-self: center;
  padding: 40px;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
}
.auth-header {
  margin-bottom: 28px;
  text-align: center;
}
.auth-header h2 {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 600;
  line-height: 1.14;
  letter-spacing: 0.196px;
  color: var(--ink);
}
.auth-header p {
  margin-top: 8px;
  font-family: var(--font-text);
  font-size: 17px;
  letter-spacing: -0.374px;
  color: var(--ink-muted-80);
}

.submit {
  width: 100%;
  height: 48px;
  margin-top: 8px;
}

.auth-footer {
  margin-top: 24px;
  text-align: center;
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
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
</style>
