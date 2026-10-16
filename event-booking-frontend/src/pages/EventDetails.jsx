import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import api, { formatDate, formatPrice, getErrorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'
import Loader from '../components/Loader'
import { sampleEvents } from '../api/sampleEvents'

export default function EventDetails() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [event, setEvent] = useState(null)
  const [seats, setSeats] = useState(1)
  const [booking, setBooking] = useState(false)

  useEffect(() => {
    api
      .get(`/events/${id}`)
      .then((res) => setEvent(res.data))
      .catch((err) => {
        // Backend na mile toh demo event dikhao
        const demo = sampleEvents.find((e) => String(e.id) === id)
        if (demo && !err.response?.status) {
          setEvent(demo)
          return
        }
        toast.error(getErrorMessage(err))
        navigate('/')
      })
  }, [id, navigate])

  if (!event) return <Loader />

  const maxSeats = Math.min(10, event.availableSeats)
  const soldOut = event.availableSeats === 0
  const total = Number(event.price) * seats

  const handleBook = async () => {
    if (!user) {
      toast('Please login to book tickets')
      navigate('/login', { state: { from: location } })
      return
    }
    setBooking(true)
    try {
      const { data } = await api.post('/bookings', { eventId: event.id, seats })
      toast.success(`Seats reserved! Ref: ${data.bookingRef}. Complete payment within 15 minutes.`)
      navigate('/my-bookings')
    } catch (err) {
      toast.error(getErrorMessage(err))
      // Seats badal gaye ho toh latest data lao
      api.get(`/events/${id}`).then((res) => setEvent(res.data))
    } finally {
      setBooking(false)
    }
  }

  return (
    <div className="grid gap-8 lg:grid-cols-3">
      <div className="space-y-6 lg:col-span-2">
        <div className="h-72 overflow-hidden rounded-3xl bg-slate-200 sm:h-96">
          {event.imageUrl && <img src={event.imageUrl} alt={event.title} className="h-full w-full object-cover" />}
        </div>
        <div>
          <span className="rounded-full bg-indigo-50 px-3 py-1 text-sm font-semibold text-indigo-700">
            {event.category}
          </span>
          <h1 className="mt-3 text-3xl font-bold">{event.title}</h1>
          <div className="mt-3 space-y-1 text-slate-600">
            <p>📅 {formatDate(event.eventDate)}</p>
            <p>📍 {event.venue}</p>
          </div>
          <p className="mt-6 leading-relaxed text-slate-700">{event.description}</p>
        </div>
      </div>

      <aside className="h-fit rounded-3xl border border-slate-200 bg-white p-6 shadow-sm lg:sticky lg:top-24">
        <p className="text-sm text-slate-500">Price per seat</p>
        <p className="text-3xl font-bold text-indigo-600">{formatPrice(event.price)}</p>

        <div className="mt-4 rounded-xl bg-slate-50 p-3 text-sm">
          <span className={soldOut ? 'text-red-600' : 'text-emerald-600'}>
            {soldOut ? 'Sold out' : `${event.availableSeats} of ${event.totalSeats} seats available`}
          </span>
        </div>

        {!soldOut && (
          <>
            <label className="mt-5 block text-sm font-medium">Number of seats</label>
            <div className="mt-2 flex items-center gap-3">
              <button
                onClick={() => setSeats((s) => Math.max(1, s - 1))}
                className="h-10 w-10 rounded-lg border border-slate-300 text-lg hover:bg-slate-100"
              >
                −
              </button>
              <span className="w-8 text-center text-lg font-semibold">{seats}</span>
              <button
                onClick={() => setSeats((s) => Math.min(maxSeats, s + 1))}
                className="h-10 w-10 rounded-lg border border-slate-300 text-lg hover:bg-slate-100"
              >
                +
              </button>
              <span className="text-xs text-slate-400">max {maxSeats}</span>
            </div>

            <div className="mt-5 flex justify-between border-t border-slate-200 pt-4 font-semibold">
              <span>Total</span>
              <span>{formatPrice(total)}</span>
            </div>
          </>
        )}

        <button
          onClick={handleBook}
          disabled={soldOut || booking}
          className="mt-5 w-full rounded-xl bg-indigo-600 py-3 font-semibold text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {soldOut ? 'Sold out' : booking ? 'Reserving...' : 'Book now'}
        </button>
      </aside>
    </div>
  )
}