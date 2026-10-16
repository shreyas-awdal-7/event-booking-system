import { useEffect, useState } from 'react'
import api from '../api/client'
import { filterSampleEvents, sampleCategories } from '../api/sampleEvents'
import EventCard from '../components/EventCard'
import Loader from '../components/Loader'

const features = [
  { icon: '⚡', title: 'Instant booking', text: 'Reserve seats in seconds with live availability.' },
  { icon: '🔒', title: 'No double booking', text: 'Seats are locked safely, even when many users book at once.' },
  { icon: '💳', title: 'Easy payments', text: 'Pay with UPI, card or net banking and get instant confirmation.' },
]

export default function Home() {
  const [events, setEvents] = useState([])
  const [categories, setCategories] = useState([])
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [loading, setLoading] = useState(true)
  const [demoMode, setDemoMode] = useState(false)

  useEffect(() => {
    api
      .get('/events/categories')
      .then((res) => setCategories(res.data))
      .catch(() => setCategories(sampleCategories))
  }, [])

  // Search pe 300ms debounce. Backend na mile toh demo events dikhao.
  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(true)
      api
        .get('/events', { params: { q: search || undefined, category: category || undefined } })
        .then((res) => {
          setEvents(res.data)
          setDemoMode(false)
        })
        .catch(() => {
          setEvents(filterSampleEvents(search, category))
          setDemoMode(true)
        })
        .finally(() => setLoading(false))
    }, 300)
    return () => clearTimeout(timer)
  }, [search, category])

  return (
    <div className="space-y-12">
      <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-indigo-600 via-violet-600 to-fuchsia-600 px-6 py-14 text-white shadow-xl sm:px-12">
        <div className="pointer-events-none absolute -right-20 -top-20 h-72 w-72 rounded-full bg-white/10 blur-2xl" />
        <div className="pointer-events-none absolute -bottom-24 left-1/3 h-72 w-72 rounded-full bg-fuchsia-400/20 blur-3xl" />

        <div className="relative max-w-2xl">
          <span className="inline-block rounded-full bg-white/15 px-3 py-1 text-xs font-semibold tracking-wide backdrop-blur">
            🎉 Concerts · Comedy · Sports · Tech
          </span>
          <h1 className="mt-4 text-4xl font-extrabold leading-tight sm:text-5xl">
            Discover & book <span className="text-amber-300">amazing events</span>
          </h1>
          <p className="mt-3 text-lg text-indigo-100">
            Find events near you and grab your seats in seconds.
          </p>
        </div>

        <div className="relative mt-8 flex flex-col gap-3 rounded-2xl bg-white/10 p-2 backdrop-blur sm:flex-row">
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="🔍  Search events..."
            className="flex-1 rounded-xl bg-white px-4 py-3 text-slate-800 placeholder-slate-400 outline-none ring-amber-300 focus:ring-4"
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

      <section>
        <div className="mb-6 flex flex-wrap items-end justify-between gap-2">
          <div>
            <h2 className="text-2xl font-bold">Upcoming events</h2>
            <p className="text-sm text-slate-500">Handpicked experiences happening soon</p>
          </div>
          {demoMode && (
            <span className="rounded-full bg-amber-50 px-3 py-1 text-xs font-medium text-amber-700">
              Demo mode: showing sample events
            </span>
          )}
        </div>

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
      </section>

      <section className="grid gap-4 sm:grid-cols-3">
        {features.map((f) => (
          <div key={f.title} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="text-3xl">{f.icon}</div>
            <h3 className="mt-3 font-semibold">{f.title}</h3>
            <p className="mt-1 text-sm text-slate-500">{f.text}</p>
          </div>
        ))}
      </section>
    </div>
  )
}
