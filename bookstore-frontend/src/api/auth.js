import api from './index'

export const login = (data) => api.post('/auth/login', data)
export const register = (data) => api.post('/auth/register', data)
export const adminLogin = (data) => api.post('/auth/admin/login', data)
export const getMe = () => api.get('/auth/me')
