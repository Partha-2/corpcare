import axios from 'axios'

const isBrowser = typeof window !== 'undefined'
const onVercel = isBrowser && window.location.hostname.includes('vercel.app')

const baseURL = import.meta.env.VITE_API_URL || (onVercel ? 'https://corpcare.onrender.com/api' : '/api')

const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' }
})

export default api
