import api from './index'

export const getBooks = (params) => api.get('/books', { params })
export const getBookDetail = (id) => api.get(`/books/${id}`)
export const getCategories = () => api.get('/categories')
