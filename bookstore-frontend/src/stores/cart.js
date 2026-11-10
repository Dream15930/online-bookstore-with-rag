import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCart } from '../api/cart'
import { useUserStore } from './user'

export const useCartStore = defineStore('cart', () => {
  const count = ref(0)

  async function fetchCount() {
    // 管理员没有购物车——后端对管理员返回 403（空响应体），会误报成「网络错误」
    if (useUserStore().isAdmin) {
      count.value = 0
      return
    }
    try {
      const res = await getCart()
      count.value = res.data.reduce((sum, item) => sum + item.quantity, 0)
    } catch {
      count.value = 0
    }
  }

  return { count, fetchCount }
})
