import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue') },
  { path: '/book/:id', name: 'BookDetail', component: () => import('../views/BookDetail.vue') },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  { path: '/cart', name: 'Cart', component: () => import('../views/Cart.vue'), meta: { auth: true } },
  { path: '/checkout', name: 'Checkout', component: () => import('../views/Checkout.vue'), meta: { auth: true } },
  { path: '/orders', name: 'Orders', component: () => import('../views/Orders.vue'), meta: { auth: true } },
  { path: '/profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
  { path: '/order/:id', name: 'OrderDetail', component: () => import('../views/OrderDetail.vue'), meta: { auth: true } },
  { path: '/admin', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/books', name: 'AdminBooks', component: () => import('../views/admin/BookManage.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/orders', name: 'AdminOrders', component: () => import('../views/admin/OrderManage.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/reviews', name: 'AdminReviews', component: () => import('../views/admin/ReviewManage.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/reports', name: 'AdminReports', component: () => import('../views/admin/Reports.vue'), meta: { auth: true, admin: true } },
  { path: '/admin/users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue'), meta: { auth: true, admin: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.meta.auth && !user) {
    return next('/login')
  }
  const isAdmin = user?.role === 'admin' || user?.role === 'super_admin'
  if (to.meta.admin && !isAdmin) {
    // 非管理员撞到后台：静默回首页，不暴露后台的存在
    return next('/')
  }
  next()
})

export default router
