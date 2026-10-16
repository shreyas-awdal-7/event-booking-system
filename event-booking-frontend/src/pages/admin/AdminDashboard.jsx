import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import api, { formatDate, formatPrice, getErrorMessage } from '../../api/client'
import Loader from '../../components/Loader'

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [events, setEvents] = useState([])
  const [bookings, setBookings] = useState([])
  const [tab, setTab] = useState('events')

  const load = () =>
    Promise.all([api.get('/admin/stats'), api.get('/admin/events'), api.get('/admin/bookings')])
      .then(([s, e, b]) => {
        setStats(s.data)
        setEvents(e.data)
        setBookings(b.data)
      })
      .catch((err) => toast.error(getErrorMessage(err)))

  useEffect(() => {
    load()
  }, [])

  const deleteEvent = async (id) => {
    if (!window.confirm('Delete this event?')) return
    try {
      await api.delete(`/events/${id}`)
      toast.success('Event deleted')
      load()
    } catch (err) {
      toast.error(getErrorMessage(err))
    }
  }

  const cancelBooking = async (id) => {
    if (!window.confirm('Cancel this booking?')) return
    try {
      await api.post(`/bookings/${id}/cancel`)
      toast.success('Booking cancelled')
      load()
    } catch (err) {
      toast.error(getErrorMessage(err))
    }
  }

  if (!stats) return <Loader />

  const cards = [
    { label: 'Total Events', value: stats.totalEvents },
    { label: 'Users', value: stats.totalUsers },
    { label: 'Bookings', value: stats.totalBookings },
    { label: 'Confirmed', value: stats.confirmedBookings },
    { label: 'Revenue', value: `₹${Number(stats.totalRevenue).toLocaleString('en-IN')}` },
  ]

  const tabClass = (name) =>
    `rounded-lg px-4 py-2 text-sm font-medium ${tab === name ? 'bg-indigo-600 text-white' : 'bg-white text-slate-600 border border-slate-200'}`

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Admin Dashboard</h1>
        <Link to="/admin/events/new" className="rounded-xl bg-indigo-600 px-4 py-2 font-semibold text-white hover:bg-indigo-700">
          + New Event
        </Link>
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
        {cards.map((c) => (
          <div key={c.label} className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
            <p className="text-sm text-slate-500">{c.label}</p>
            <p className="mt-1 text-2xl font-bold">{c.value}</p>
          </div>
        ))}
      </div>

      <div className="flex gap-2">
        <button onClick={() => setTab('events')} className={tabClass('events')}>Events ({events.length})</button>
        <button onClick={() => setTab('bookings')} className={tabClass('bookings')}>Bookings ({bookings.length})</button>
      </div>

      <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
        {tab === 'events' ? (
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                <th className="p-3">Event</th>
                <th className="p-3">Date</th>
                <th className="p-3">Price</th>
                <th className="p-3">Seats</th>
                <th className="p-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {events.map((e) => (
                <tr key={e.id} className="border-t border-slate-100">
                  <td className="p-3">
                    <p className="font-medium">{e.title}</p>
                    <p className="text-xs text-slate-400">{e.category} · {e.venue}</p>
                  </td>
                  <td className="p-3 whitespace-nowrap">{formatDate(e.eventDate)}</td>
                  <td className="p-3">{formatPrice(e.price)}</td>
                  <td className="p-3">{e.totalSeats - e.availableSeats} / {e.totalSeats}</td>
                  <td className="p-3 text-right whitespace-nowrap">
                    <Link to={`/admin/events/${e.id}/edit`} className="mr-3 text-indigo-600 hover:underline">Edit</Link>
                    <button onClick={() => deleteEvent(e.id)} className="text-red-600 hover:underline">Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                <th className="p-3">Ref</th>
                <th className="p-3">User</th>
                <th className="p-3">Event</th>
                <th className="p-3">Seats</th>
                <th className="p-3">Amount</th>
                <th className="p-3">Status</th>
                <th className="p-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.id} className="border-t border-slate-100">
                  <td className="p-3 font-mono text-xs">{b.bookingRef}</td>
                  <td className="p-3">
                    <p>{b.userName}</p>
                    <p className="text-xs text-slate-400">{b.userEmail}</p>
                  </td>
                  <td className="p-3">{b.eventTitle}</td>
                  <td className="p-3">{b.seats}</td>
                  <td className="p-3">{formatPrice(b.totalAmount)}</td>
                  <td className="p-3 text-xs font-semibold">{b.status.replace('_', ' ')}</td>
                  <td className="p-3 text-right">
                    {b.status !== 'CANCELLED' && (
                      <button onClick={() => cancelBooking(b.id)} className="text-red-600 hover:underline">Cancel</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}