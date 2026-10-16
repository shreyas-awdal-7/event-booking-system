import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import api, { formatDate, formatPrice, getErrorMessage } from '../api/client'
import Loader from '../components/Loader'

const statusStyles = {
  PENDING_PAYMENT: 'bg-amber-50 text-amber-700',
  CONFIRMED: 'bg-emerald-50 text-emerald-700',
  CANCELLED: 'bg-slate-100 text-slate-500',
}

export default function MyBookings() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [methods, setMethods] = useState({})
  const [busyId, setBusyId] = useState(null)

  const load = () =>
    api
      .get('/bookings/my')
      .then((res) => setBookings(res.data))
      .catch((err) => toast.error(getErrorMessage(err)))
      .finally(() => setLoading(false))

  useEffect(() => {
    load()
  }, [])

  const pay = async (id) => {
    setBusyId(id)
    try {
      const { data } = await api.post(`/bookings/${id}/pay`, { method: methods[id] || 'UPI' })
      toast.success(`Payment successful! Txn: ${data.transactionId}`)
      load()
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  const cancel = async (id) => {
    if (!window.confirm('Cancel this booking?')) return
    setBusyId(id)
    try {
      await api.post(`/bookings/${id}/cancel`)
      toast.success('Booking cancelled')
      load()
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setBusyId(null)
    }
  }

  if (loading) return <Loader />

  return (
    <div>
      <h1 className="text-2xl font-bold">My Bookings</h1>

      {bookings.length === 0 ? (
        <div className="py-16 text-center text-slate-500">
          No bookings yet. <Link to="/" className="text-indigo-600">Browse events</Link>
        </div>
      ) : (
        <div className="mt-6 space-y-4">
          {bookings.map((b) => (
            <div key={b.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <Link to={`/events/${b.eventId}`} className="text-lg font-semibold hover:text-indigo-600">
                    {b.eventTitle}
                  </Link>
                  <p className="text-sm text-slate-500">📅 {formatDate(b.eventDate)} · 📍 {b.venue}</p>
                  <p className="mt-1 text-sm text-slate-500">
                    Ref <span className="font-mono">{b.bookingRef}</span> · {b.seats} seat(s) · {formatPrice(b.totalAmount)}
                  </p>
                  {b.transactionId && (
                    <p className="text-xs text-slate-400">Txn: {b.transactionId} ({b.paymentStatus})</p>
                  )}
                </div>
                <span className={`rounded-full px-3 py-1 text-xs font-semibold ${statusStyles[b.status]}`}>
                  {b.status.replace('_', ' ')}
                </span>
              </div>

              {b.status !== 'CANCELLED' && (
                <div className="mt-4 flex flex-wrap items-center gap-2 border-t border-slate-100 pt-4">
                  {b.status === 'PENDING_PAYMENT' && (
                    <>
                      <select
                        value={methods[b.id] || 'UPI'}
                        onChange={(e) => setMethods({ ...methods, [b.id]: e.target.value })}
                        className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
                      >
                        <option value="UPI">UPI</option>
                        <option value="CARD">Card</option>
                        <option value="NETBANKING">Net Banking</option>
                      </select>
                      <button
                        onClick={() => pay(b.id)}
                        disabled={busyId === b.id}
                        className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:bg-slate-300"
                      >
                        Pay {formatPrice(b.totalAmount)}
                      </button>
                      <span className="text-xs text-amber-600">Pay within 15 min or seats are released</span>
                    </>
                  )}
                  <button
                    onClick={() => cancel(b.id)}
                    disabled={busyId === b.id}
                    className="ml-auto rounded-lg border border-red-200 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50"
                  >
                    Cancel
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}