<template>
  <footer class="footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <span class="footer-logo">网上书店</span>
        <p>发现好书，畅享阅读</p>
      </div>
      <div class="footer-links">
        <div class="link-group">
          <h4>关于我们</h4>
          <span>公司介绍</span>
          <span>联系我们</span>
        </div>
        <div class="link-group">
          <h4>帮助中心</h4>
          <span>购物指南</span>
          <span>常见问题</span>
        </div>
        <div class="link-group">
          <h4>服务保障</h4>
          <span>正版承诺</span>
          <span>退换政策</span>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <span>&copy; 2024 网上书店 BookStore. All Rights Reserved.</span>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: #1f2937;
  color: #d1d5db;
  margin-top: 60px;
}
.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 48px 24px 32px;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 40px;
}
.footer-brand .footer-logo {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
}
.footer-brand p {
  margin-top: 8px;
  font-size: 14px;
  color: #9ca3af;
}
.footer-links {
  display: flex;
  gap: 48px;
}
.link-group h4 {
  color: #fff;
  font-size: 14px;
  margin-bottom: 12px;
}
.link-group span {
  display: block;
  font-size: 13px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: color 0.2s;
}
.link-group span:hover { color: #fff; }
.footer-bottom {
  border-top: 1px solid #374151;
  padding: 20px 24px;
  text-align: center;
  font-size: 13px;
  color: #6b7280;
}
</style>
