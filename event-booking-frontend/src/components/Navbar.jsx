import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const linkClass = ({ isActive }) =>
  `px-3 py-2 rounded-lg text-sm font-medium transition ${
    isActive ? 'bg-indigo-50 text-indigo-700' : 'text-slate-600 hover:text-indigo-600'
  }`

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/90 backdrop-blur">
      <nav className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-2 px-4 py-3">
        <Link to="/" className="text-xl font-bold text-indigo-600">
          🎟️ EventHub
        </Link>

        <div className="flex flex-wrap items-center gap-1">
          <NavLink to="/" end className={linkClass}>Events</NavLink>
          {user && <NavLink to="/my-bookings" className={linkClass}>My Bookings</NavLink>}
          {isAdmin && <NavLink to="/admin" className={linkClass}>Admin</NavLink>}

          {user ? (
            <div className="ml-2 flex items-center gap-3">
              <span className="hidden text-sm text-slate-500 sm:inline">Hi, {user.name.split(' ')[0]}</span>
              <button
                onClick={handleLogout}
                className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-medium hover:bg-slate-100"
              >
                Logout
              </button>
            </div>
          ) : (
            <div className="ml-2 flex items-center gap-2">
              <NavLink to="/login" className={linkClass}>Login</NavLink>
              <Link
                to="/register"
                className="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700"
              >
                Sign up
              </Link>
            </div>
          )}
        </div>
      </nav>
    </header>
  )
}