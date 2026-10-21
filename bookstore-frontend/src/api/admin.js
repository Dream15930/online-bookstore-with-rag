import api from './index'

export const getDashboard = () => api.get('/admin/dashboard')
export const getAdminBooks = (params) => api.get('/admin/books', { params })
export const addBook = (data) => api.post('/admin/books', data)
export const updateBook = (id, data) => api.put(`/admin/books/${id}`, data)
export const toggleBookStatus = (id) => api.put(`/admin/books/${id}/status`)
export const getAdminOrders = (params) => api.get('/admin/orders', { params })
export const updateOrderStatus = (id, status) => api.put(`/admin/orders/${id}/status`, { status })
export const getAdminOrderDetail = (id) => api.get(`/admin/orders/${id}`)
export const getAdminUsers = () => api.get('/admin/users')
export const toggleUserStatus = (id) => api.put(`/admin/users/${id}/status`)
export const createAdmin = (data) => api.post('/admin/users/admin', data)
