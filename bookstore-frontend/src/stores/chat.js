import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const visible = ref(false)
  const open = () => { visible.value = true }
  const close = () => { visible.value = false }
  const toggle = () => { visible.value = !visible.value }
  return { visible, open, close, toggle }
})
