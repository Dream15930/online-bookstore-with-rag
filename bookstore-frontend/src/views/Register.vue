<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-icon"><el-icon :size="32"><UserFilled /></el-icon></div>
        <h2>创建账号</h2>
        <p>注册即可成为会员，开始购书</p>
      </div>
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
        <el-button type="primary" @click="handleRegister" :loading="loading" size="large" class="submit-btn">
          注册
        </el-button>
      </el-form>
      <div class="auth-footer">已有账号？<router-link to="/login">立即登录</router-link></div>
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
.auth-page { display: flex; justify-content: center; padding-top: 60px; }
.auth-card {
  width: 420px; background: var(--white); padding: 44px 40px;
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
