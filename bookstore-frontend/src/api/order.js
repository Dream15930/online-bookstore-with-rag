import api from './index'

export const placeOrder = (data) => api.post('/orders', data)
export const getMyOrders = (params) => api.get('/orders', { params })
export const getOrderDetail = (id) => api.get(`/orders/${id}`)
export const completeOrder = (id) => api.put(`/orders/${id}/complete`)
export const reviewOrder = (id, data) => api.post(`/orders/${id}/review`, data)
