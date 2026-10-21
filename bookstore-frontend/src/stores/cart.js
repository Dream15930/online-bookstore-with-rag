import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCart } from '../api/cart'

export const useCartStore = defineStore('cart', () => {
  const count = ref(0)

  async function fetchCount() {
    try {
      const res = await getCart()
      count.value = res.data.reduce((sum, item) => sum + item.quantity, 0)
    } catch {
      count.value = 0
    }
  }

  return { count, fetchCount }
})
