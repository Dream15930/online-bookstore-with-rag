import api from './index'

export const getCart = () => api.get('/cart')
export const addToCart = (bookId) => api.post('/cart', { bookId })
export const updateCartQuantity = (id, quantity) => api.put(`/cart/${id}`, { quantity })
export const removeFromCart = (id) => api.delete(`/cart/${id}`)
