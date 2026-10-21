<template>
  <header class="header">
    <div class="header-inner">
      <router-link to="/" class="brand">
        <span class="brand-icon"><el-icon><Notebook /></el-icon></span>
        <span class="brand-text">BookStore</span>
      </router-link>

      <nav class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>

        <template v-if="userStore.isLoggedIn && !userStore.isAdmin">
          <router-link to="/cart" class="nav-link">
            购物车
            <span class="badge" v-if="cartStore.count">{{ cartStore.count }}</span>
          </router-link>
          <router-link to="/orders" class="nav-link">我的订单</router-link>
          <router-link to="/profile" class="nav-link">我的</router-link>
        </template>

        <router-link to="/admin" v-if="userStore.isAdmin" class="nav-link admin-link">
          管理后台
        </router-link>

        <template v-if="!userStore.isLoggedIn">
          <router-link to="/login" class="nav-link nav-btn-outline">会员登录</router-link>
          <router-link to="/register" class="nav-link nav-btn-primary">注册</router-link>
          <router-link to="/admin/login" class="nav-link admin-login-link">管理员</router-link>
        </template>
        <template v-else>
          <div class="user-menu">
            <span class="user-name">
              <el-icon><User /></el-icon>
              {{ userStore.user?.username }}
            </span>
            <span class="logout-link" @click="handleLogout">退出</span>
          </div>
        </template>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.header {
  background: var(--white);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(12px);
  background: rgba(255,255,255,0.95);
}
.header-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--primary);
}
.brand-icon { font-size: 26px; }
.brand-text {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--text);
}
.nav-links {
  display: flex;
  align-items: center;
  gap: 6px;
}
.nav-link {
  text-decoration: none;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all var(--transition);
  position: relative;
}
.nav-link:hover, .nav-link.router-link-active { color: var(--primary); background: var(--primary-light); }
.nav-link.router-link-active { color: var(--primary-dark); font-weight: 600; }

.nav-btn-outline {
  border: 1.5px solid var(--border);
  color: var(--text);
}
.nav-btn-outline:hover { border-color: var(--primary); background: var(--primary-light); }
.nav-btn-primary {
  background: var(--primary);
  color: #fff !important;
  padding: 8px 20px;
}
.nav-btn-primary:hover { background: var(--primary-dark); }
.admin-link { color: var(--accent); }
.admin-link:hover { background: #fef3c7; color: #d97706; }
.admin-login-link { color: var(--text-secondary); font-size: 13px; }
.admin-login-link:hover { color: var(--accent); background: #fef3c7; }

.badge {
  background: var(--danger);
  color: #fff;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  margin-left: 4px;
  font-weight: 600;
}
.user-menu {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 8px;
  padding-left: 16px;
  border-left: 1px solid var(--border);
}
.user-name {
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text);
}
.logout-link {
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}
.logout-link:hover { color: var(--danger); }
</style>
