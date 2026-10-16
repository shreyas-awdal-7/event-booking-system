import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useAuth } from '../context/AuthContext'
import { getErrorMessage } from '../api/client'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const user = await login(form.email, form.password)
      toast.success(`Welcome back, ${user.name.split(' ')[0]}!`)
      const from = location.state?.from?.pathname
      navigate(from || (user.role === 'ADMIN' ? '/admin' : '/'), { replace: true })
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const fillDemo = (email, password) => setForm({ email, password })

  return (
    <div className="mx-auto max-w-md rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
      <h1 className="text-2xl font-bold">Login</h1>
      <p className="mt-1 text-sm text-slate-500">Welcome back! Login to book your events.</p>

      <form onSubmit={handleSubmit} className="mt-6 space-y-4">
        <input
          type="email"
          required
          placeholder="Email"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          className="w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
        />
        <input
          type="password"
          required
          placeholder="Password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          className="w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500"
        />
        <button
          disabled={loading}
          className="w-full rounded-xl bg-indigo-600 py-3 font-semibold text-white hover:bg-indigo-700 disabled:bg-slate-300"
        >
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>

      <div className="mt-6 rounded-xl bg-slate-50 p-4 text-sm">
        <p className="font-medium text-slate-600">Demo accounts</p>
        <div className="mt-2 flex gap-2">
          <button onClick={() => fillDemo('demo@eventbooking.com', 'demo123')} className="rounded-lg border px-3 py-1 hover:bg-white">
            User
          </button>
          <button onClick={() => fillDemo('admin@eventbooking.com', 'admin123')} className="rounded-lg border px-3 py-1 hover:bg-white">
            Admin
          </button>
        </div>
      </div>

      <p className="mt-6 text-center text-sm text-slate-500">
        New here? <Link to="/register" className="font-medium text-indigo-600">Create an account</Link>
      </p>
    </div>
  )
}
