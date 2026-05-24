import axios from 'axios'

const isBrowser = typeof window !== 'undefined'
const onVercel = isBrowser && window.location.hostname.includes('vercel.app')

const baseURL = import.meta.env.VITE_API_URL || (onVercel ? 'https://corpcare.onrender.com/api' : '/api')

const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  r => r,
  err => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      sessionStorage.clear()
      const path = window.location.pathname
      if (path.startsWith('/admin')) window.location.href = '/admin/login'
      else if (path.startsWith('/client')) window.location.href = '/client/login'
      else if (path.startsWith('/hospital')) window.location.href = '/hospital/login'
      else if (path.startsWith('/employee')) window.location.href = '/employee/login'
    }
    return Promise.reject(err)
  }
)

export default api
