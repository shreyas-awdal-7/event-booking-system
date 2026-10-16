import axios from 'axios'

const API_URL = import.meta.env.VITE_API_URL
const api = axios.create({ baseURL: API_URL ? `${API_URL}/api` : '/api' })

// Har request ke saath JWT token bhejo
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Backend na mile toh server JSON ki jagah index.html bhej deta hai. Use error maano, crash mat hone do.
// Token expire ho jaye toh logout karke login page pe bhejo
api.interceptors.response.use(
  (res) => {
    if (typeof res.data === 'string' && res.data.trimStart().startsWith('<')) {
      return Promise.reject({ response: { data: { message: 'Backend server is not reachable. Please try again later.' } } })
    }
    return res
  },
  (err) => {
    if (err.response?.status === 401 && localStorage.getItem('token')) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export const getErrorMessage = (err) => {
  const data = err.response?.data
  if (data?.errors) return Object.values(data.errors).join(', ')
  return data?.message || 'Something went wrong'
}

export const formatPrice = (price) =>
  Number(price) === 0 ? 'Free' : `₹${Number(price).toLocaleString('en-IN')}`

export const formatDate = (date) =>
  new Date(date).toLocaleString('en-IN', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric',
    hour: 'numeric', minute: '2-digit',
  })

export default api