<template>
  <footer class="footer-band">
    <div class="footer-inner">
      <nav class="links">
        <span class="grp">
          <span class="grp-title">支持</span>
          <button class="flink" @click="openChat">在线客服</button>
          <span class="ftext">常见问题</span>
          <span class="ftext">退换政策</span>
        </span>
        <span class="grp">
          <span class="grp-title">关于</span>
          <span class="ftext">网上书店</span>
          <span class="ftext">正版承诺</span>
        </span>
      </nav>
      <span class="legal">&copy; 2024 网上书店 BookStore. 保留所有权利。</span>
    </div>
  </footer>
</template>

<script setup>
import { useChatStore } from '../stores/chat'

const chatStore = useChatStore()
function openChat() {
  chatStore.open()
}
</script>

<style scoped>
/* 压缩成一条，钉在界面底端 */
.footer-band {
  position: sticky;
  bottom: 0;
  z-index: 40;
  background: var(--canvas-parchment);
  border-top: 1px solid var(--hairline);
}
.footer-inner {
  max-width: 1440px;
  min-height: 56px;
  margin: 0 auto;
  padding: 10px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px 32px;
  flex-wrap: wrap;
}

.links {
  display: flex;
  align-items: center;
  gap: 28px;
  flex-wrap: wrap;
}
.grp {
  display: flex;
  align-items: center;
  gap: 12px;
}
.grp-title {
  font-family: var(--font-text);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: -0.2px;
  color: var(--ink);
}
.ftext,
.flink {
  font-family: var(--font-text);
  font-size: 13px;
  letter-spacing: -0.2px;
  color: var(--ink-muted-80);
  background: none;
  border: none;
  padding: 0;
  white-space: nowrap;
}
.ftext { cursor: default; }
.flink { cursor: pointer; }
.flink:hover { color: var(--primary); }

.legal {
  font-family: var(--font-text);
  font-size: 12px;
  letter-spacing: -0.12px;
  color: var(--ink-muted-80);
  white-space: nowrap;
}
</style>
