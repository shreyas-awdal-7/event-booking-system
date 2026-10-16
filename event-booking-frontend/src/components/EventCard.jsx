import { Link } from 'react-router-dom'
import { formatDate, formatPrice } from '../api/client'

export default function EventCard({ event }) {
  const soldOut = event.availableSeats === 0
  const fewLeft = !soldOut && event.availableSeats <= 10

  return (
    <Link
      to={`/events/${event.id}`}
      className="group overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-lg"
    >
      <div className="relative h-44 overflow-hidden bg-slate-200">
        {event.imageUrl && (
          <img
            src={event.imageUrl}
            alt={event.title}
            className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
          />
        )}
        <span className="absolute left-3 top-3 rounded-full bg-white/90 px-3 py-1 text-xs font-semibold text-indigo-700">
          {event.category}
        </span>
      </div>

      <div className="space-y-2 p-4">
        <h3 className="line-clamp-1 text-lg font-semibold">{event.title}</h3>
        <p className="text-sm text-slate-500">📅 {formatDate(event.eventDate)}</p>
        <p className="line-clamp-1 text-sm text-slate-500">📍 {event.venue}</p>

        <div className="flex items-center justify-between pt-2">
          <span className="text-lg font-bold text-indigo-600">{formatPrice(event.price)}</span>
          <span
            className={`text-xs font-medium ${
              soldOut ? 'text-red-600' : fewLeft ? 'text-amber-600' : 'text-emerald-600'
            }`}
          >
            {soldOut ? 'Sold out' : `${event.availableSeats} seats left`}
          </span>
        </div>
      </div>
    </Link>
  )
}