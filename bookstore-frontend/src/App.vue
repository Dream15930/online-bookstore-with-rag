<template>
  <div id="app-layout">
    <Navbar />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <Footer v-if="!isAdmin" />
    <ChatWidget v-if="!isAdmin" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Footer from './components/Footer.vue'
import ChatWidget from './components/ChatWidget.vue'

const route = useRoute()
const isAdmin = computed(() => route.path.startsWith('/admin'))
</script>

<style>
/* ============================================================
   Apple-design-analysis — global system
   Photography-first, UI recedes. Edge-to-edge product tiles
   alternate light and dark; the colour change is the divider.
   One accent (Action Blue), one shadow (under product imagery).
   ============================================================ */
* { margin: 0; padding: 0; box-sizing: border-box; }

:root {
  /* brand & accent */
  --primary: #0066cc;
  --primary-focus: #0071e3;
  --primary-on-dark: #2997ff;
  --on-primary: #ffffff;

  /* ink & body */
  --ink: #1d1d1f;
  --body: #1d1d1f;
  --body-on-dark: #ffffff;
  --body-muted: #cccccc;
  --ink-muted-80: #333333;
  --ink-muted-48: #7a7a7a;

  /* surfaces */
  --canvas: #ffffff;
  --canvas-parchment: #f5f5f7;
  --surface-pearl: #fafafc;
  --surface-tile-1: #272729;
  --surface-tile-2: #2a2a2c;
  --surface-tile-3: #252527;
  --surface-black: #000000;
  --on-dark: #ffffff;

  /* hairlines */
  --divider-soft: #f0f0f0;
  --hairline: #e0e0e0;

  /* type */
  --font-display: 'SF Pro Display', 'Inter', system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
  --font-text: 'SF Pro Text', 'Inter', system-ui, -apple-system, BlinkMacSystemFont, sans-serif;

  /* radii */
  --radius-xs: 5px;
  --radius-sm: 8px;
  --radius-md: 11px;
  --radius-lg: 18px;
  --radius-pill: 9999px;

  /* spacing */
  --sp-xxs: 4px;
  --sp-xs: 8px;
  --sp-sm: 12px;
  --sp-md: 17px;
  --sp-lg: 24px;
  --sp-xl: 32px;
  --sp-xxl: 48px;
  --sp-section: 80px;

  /* the single depth cue: product imagery resting on a surface */
  --product-shadow: rgba(0, 0, 0, 0.22) 3px 5px 30px 0;
  --ring-soft: 0 0 0 1px rgba(0, 0, 0, 0.08);

  /* legacy aliases so not-yet-converted pages inherit the world */
  --primary-dark: #0055aa;
  --primary-light: #f5f5f7;
  --accent: #0066cc;
  --danger: #1d1d1f;
  --text: #1d1d1f;
  --text-secondary: #7a7a7a;
  --border: #e0e0e0;
  --bg: #ffffff;
  --white: #ffffff;
  --radius: 18px;
  --shadow-sm: none;
  --shadow: none;
  --shadow-md: none;
  --shadow-lg: none;
  --transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  /* Element Plus */
  --el-color-primary: #0066cc;
  --el-border-radius-base: 8px;
  --el-font-family: var(--font-text);
  --el-text-color-primary: #1d1d1f;
  --el-text-color-regular: #1d1d1f;
  --el-border-color: #e0e0e0;
  --el-border-color-light: #e0e0e0;
  --el-border-color-lighter: #f0f0f0;
  --el-fill-color-blank: #ffffff;

  /* chrome heights, used by full-screen sections */
  --nav-h: 56px;
  --subnav-h: 0px;
  --footer-h: 56px;
}

/* ─── Home: one page per wheel step, no visible scrollbar (desktop only) ─── */
@media (min-width: 834px) {
  html.home-snap {
    scrollbar-width: none;
    -ms-overflow-style: none;
  }
  html.home-snap::-webkit-scrollbar { width: 0; height: 0; display: none; }
  /* fixed, not sticky: the header leaves the flow, so the first page starts at
     document top and its padding-top alone clears the chrome */
  html.home-snap .site-header { position: fixed; top: 0; left: 0; right: 0; z-index: 60; }
  /* 页脚同样脱离文档流，否则末屏之后会多出一段可滚动的空隙 */
  html.home-snap .footer-band { position: fixed; bottom: 0; left: 0; right: 0; z-index: 40; }
}

/* ─── admin: same clean edge, but scrolling stays free (data tables need it) ─── */
html.admin-scroll {
  scrollbar-width: none;
  -ms-overflow-style: none;
}
html.admin-scroll::-webkit-scrollbar { width: 0; height: 0; display: none; }

/* 整站不显示页面滚动条（内容仍可滚动） */
html {
  scrollbar-width: none;
  -ms-overflow-style: none;
}
html::-webkit-scrollbar { width: 0; height: 0; display: none; }

body {
  font-family: var(--font-text);
  font-size: 17px;
  line-height: 1.47;
  background: var(--canvas);
  color: var(--ink);
  -webkit-font-smoothing: antialiased;
}

#app-layout { min-height: 100vh; }

/* full-bleed tiles lock at 1440; wider viewports absorb the rest as margin */
.main-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0;
  /* 内容不足一屏时把页脚推到视口底部 */
  min-height: calc(100vh - var(--nav-h) - var(--footer-h));
  min-height: calc(100dvh - var(--nav-h) - var(--footer-h));
}

.el-input__inner,
.el-textarea__inner {
  font-family: var(--font-text);
}

/* Element Plus surfaces that teleport out of scoped styles */
.el-dialog,
.el-message-box { border-radius: var(--radius-lg); }
.el-dialog__footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ─── shared primitives ─── */
.tile { padding: var(--sp-section) var(--sp-lg); }
.tile-light { background: var(--canvas); }
.tile-parchment { background: var(--canvas-parchment); }

.page-title {
  font-family: var(--font-display);
  font-size: 34px;
  font-weight: 600;
  line-height: 1.47;
  letter-spacing: -0.374px;
  color: var(--ink);
}

.pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  padding: 0 22px;
  font-family: var(--font-text);
  font-size: 17px;
  font-weight: 400;
  letter-spacing: -0.374px;
  color: var(--on-primary);
  background: var(--primary);
  border: none;
  border-radius: var(--radius-pill);
  cursor: pointer;
  white-space: nowrap;
}
.pill:hover:not(:disabled) { background: var(--primary-focus); }
.pill:active:not(:disabled) { transform: scale(0.95); }
.pill:disabled {
  background: var(--hairline);
  color: var(--ink-muted-48);
  cursor: not-allowed;
}
.pill.small {
  height: 36px;
  padding: 0 16px;
  font-size: 14px;
  letter-spacing: -0.224px;
}

.text-link { color: var(--primary); text-decoration: none; }
.text-link:hover { text-decoration: underline; }

.link-btn {
  font-family: var(--font-text);
  font-size: 14px;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
}
.link-btn:hover { color: var(--primary); text-decoration: underline; }

/* ─── admin shell ─── */
.admin-page {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  padding: 32px 24px 80px;
}
.admin-main { flex: 1; min-width: 0; }
.admin-panel {
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

/* ─── status chip (one accent marks the active/positive state) ─── */
.chip {
  display: inline-block;
  padding: 3px 12px;
  border: 1px solid var(--hairline);
  border-radius: var(--radius-pill);
  font-family: var(--font-text);
  font-size: 12px;
  letter-spacing: -0.12px;
  color: var(--ink-muted-80);
  white-space: nowrap;
}
.chip.on { border-color: var(--primary); color: var(--primary); }

/* ─── Element Plus tables: hairlines, no zebra, no cell borders ─── */
.el-table {
  --el-table-border-color: var(--hairline);
  --el-table-header-bg-color: var(--canvas);
  --el-table-header-text-color: var(--ink-muted-80);
  --el-table-text-color: var(--ink);
  --el-table-row-hover-bg-color: var(--canvas-parchment);
  font-family: var(--font-text);
  font-size: 15px;
}
.el-table th.el-table__cell { font-weight: 600; }
.el-table .cell { letter-spacing: -0.24px; }
</style>
