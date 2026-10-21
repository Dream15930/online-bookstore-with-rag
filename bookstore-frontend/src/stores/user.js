import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMe } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isLoggedIn = ref(!!user.value)
  const isAdmin = ref(user.value?.role === 'admin' || user.value?.role === 'super_admin')

  function setUser(data) {
    user.value = data
    isLoggedIn.value = true
    isAdmin.value = data.role === 'admin' || data.role === 'super_admin'
    localStorage.setItem('user', JSON.stringify(data))
  }

  function logout() {
    user.value = null
    isLoggedIn.value = false
    isAdmin.value = false
    localStorage.removeItem('user')
  }

  async function fetchUser() {
    try {
      const res = await getMe()
      setUser({ ...res.data, token: user.value?.token })
    } catch {
      logout()
    }
  }

  return { user, isLoggedIn, isAdmin, setUser, logout, fetchUser }
})
