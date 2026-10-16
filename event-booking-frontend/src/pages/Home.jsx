import { useEffect, useState } from 'react'
import api, { getErrorMessage } from '../api/client'
import EventCard from '../components/EventCard'
import Loader from '../components/Loader'
import toast from 'react-hot-toast'

export default function Home() {
  const [events, setEvents] = useState([])
  const [categories, setCategories] = useState([])
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/events/categories').then((res) => setCategories(res.data)).catch(() => {})
  }, [])

  // Search pe 300ms debounce
  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true)
      api
        .get('/events', { params: { q: search || undefined, category: category || undefined } })
        .then((res) => setEvents(res.data))
        .catch((err) => toast.error(getErrorMessage(err)))
        .finally(() => setLoading(false))
    }, 300)
    return () => clearTimeout(timer)
  }, [search, category])

  return (
    <div className="space-y-8">
      <section className="rounded-3xl bg-gradient-to-r from-indigo-600 to-purple-600 px-6 py-12 text-white sm:px-10">
        <h1 className="text-3xl font-bold sm:text-4xl">Discover & book amazing events</h1>
        <p className="mt-2 text-indigo-100">Concerts, comedy, conferences and more. Book your seats in seconds.</p>

        <div className="mt-6 flex flex-col gap-3 sm:flex-row">
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search events..."
            className="flex-1 rounded-xl bg-white px-4 py-3 text-slate-800 placeholder-slate-400 outline-none ring-indigo-300 focus:ring-4"
          />
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="rounded-xl bg-white px-4 py-3 text-slate-800 outline-none"
          >
            <option value="">All categories</option>
            {categories.map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </select>
        </div>
      </section>

      {loading ? (
        <Loader />
      ) : events.length === 0 ? (
        <p className="py-16 text-center text-slate-500">No upcoming events found.</p>
      ) : (
        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {events.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      )}
    </div>
  )
}