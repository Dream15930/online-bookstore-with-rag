<template>
  <header class="site-header">
    <div class="bar">
      <router-link to="/" class="brand">
        <span class="brand-mark">网上书店</span>
        <span class="brand-sub">BOOKSTORE</span>
      </router-link>

      <nav class="links">
        <router-link to="/" class="link">首页</router-link>

        <template v-if="userStore.isLoggedIn && !userStore.isAdmin">
          <router-link to="/cart" class="link">
            购物车<span class="badge" v-if="cartStore.count">{{ cartStore.count }}</span>
          </router-link>
          <router-link to="/orders" class="link">我的订单</router-link>
          <router-link to="/profile" class="link">我的</router-link>
        </template>

        <router-link to="/admin" v-if="userStore.isAdmin" class="link">管理后台</router-link>

        <template v-if="!userStore.isLoggedIn">
          <router-link to="/login" class="link">登录</router-link>
          <router-link to="/register" class="link">注册</router-link>
        </template>
        <template v-else>
          <span class="user">{{ userStore.user?.username }}</span>
          <button class="link logout" @click="handleLogout">退出</button>
        </template>
      </nav>

      <button class="cta" v-if="!userStore.isAdmin" @click="openChat">在线客服</button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { useChatStore } from '../stores/chat'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const isAdmin = computed(() => route.path.startsWith('/admin'))
const userStore = useUserStore()
const cartStore = useCartStore()
const chatStore = useChatStore()

function openChat() {
  chatStore.open()
}

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
/* 单条导航：品牌、站点链接、商店链接与客服都在同一行 */
.site-header {
  background: var(--surface-black);
  color: var(--on-dark);
}
.bar {
  max-width: 1440px;
  min-height: 56px;
  margin: 0 auto;
  padding: 8px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.brand {
  display: flex;
  align-items: baseline;
  gap: 8px;
  text-decoration: none;
  flex-shrink: 0;
}
.brand-mark {
  font-family: var(--font-display);
  font-size: 20px;
  font-weight: 900;
  line-height: 1;
  color: var(--on-dark);
}
.brand-sub {
  font-family: var(--font-ui);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 2px;
  color: var(--on-dark);
}

.links {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-left: auto;
  flex-wrap: wrap;
}
.link {
  font-family: var(--font-ui);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.3px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  padding: 6px 10px;
  background: none;
  border: none;
  cursor: pointer;
  white-space: nowrap;
}
.link:hover,
.link.router-link-exact-active { color: var(--on-dark); }
.user {
  font-family: var(--font-ui);
  font-size: 13px;
  font-weight: 700;
  color: var(--on-dark);
  padding: 6px 10px;
  margin-left: 4px;
  border-left: 1px solid rgba(255, 255, 255, 0.4);
}
.badge {
  display: inline-block;
  margin-left: 5px;
  min-width: 16px;
  padding: 0 4px;
  background: var(--primary);
  color: var(--on-primary);
  font-family: var(--font-ui);
  font-size: 11px;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
}

.cta {
  flex-shrink: 0;
  font-family: var(--font-ui);
  font-size: 13px;
  font-weight: 700;
  color: var(--on-primary);
  background: var(--primary);
  border: none;
  border-radius: var(--radius-pill);
  padding: 8px 16px;
  cursor: pointer;
  white-space: nowrap;
}
.cta:hover { background: var(--primary-focus); }
.cta:active { transform: scale(0.95); }

@media (max-width: 833px) {
  .bar { padding: 8px 16px; gap: 12px; }
  .links { gap: 0; }
}
</style>
