import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import api, { getErrorMessage } from '../../api/client'

const emptyForm = {
  title: '', description: '', venue: '', category: '',
  eventDate: '', price: '', totalSeats: '', imageUrl: '',
}

export default function EventForm() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!isEdit) return
    api
      .get(`/events/${id}`)
      .then(({ data }) =>
        setForm({
          ...data,
          description: data.description || '',
          imageUrl: data.imageUrl || '',
          eventDate: data.eventDate.slice(0, 16), // datetime-local format
        })
      )
      .catch((err) => toast.error(getErrorMessage(err)))
  }, [id, isEdit])

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    const payload = {
      title: form.title,
      description: form.description,
      venue: form.venue,
      category: form.category,
      eventDate: form.eventDate,
      price: Number(form.price),
      totalSeats: Number(form.totalSeats),
      imageUrl: form.imageUrl || null,
    }
    try {
      if (isEdit) await api.put(`/events/${id}`, payload)
      else await api.post('/events', payload)
      toast.success(isEdit ? 'Event updated' : 'Event created')
      navigate('/admin')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const input = 'w-full rounded-xl border border-slate-300 px-4 py-3 outline-none focus:border-indigo-500'

  return (
    <div className="mx-auto max-w-2xl rounded-3xl border border-slate-200 bg-white p-8 shadow-sm">
      <h1 className="text-2xl font-bold">{isEdit ? 'Edit Event' : 'Create Event'}</h1>

      <form onSubmit={handleSubmit} className="mt-6 grid gap-4 sm:grid-cols-2">
        <input className={`${input} sm:col-span-2`} placeholder="Title" required value={form.title} onChange={set('title')} />
        <textarea className={`${input} sm:col-span-2`} rows={4} placeholder="Description" value={form.description} onChange={set('description')} />
        <input className={input} placeholder="Venue" required value={form.venue} onChange={set('venue')} />
        <input className={input} placeholder="Category (e.g. Music)" required value={form.category} onChange={set('category')} />
        <input className={input} type="datetime-local" required value={form.eventDate} onChange={set('eventDate')} />
        <input className={input} type="number" min="0" step="0.01" placeholder="Price (₹)" required value={form.price} onChange={set('price')} />
        <input className={input} type="number" min="1" placeholder="Total seats" required value={form.totalSeats} onChange={set('totalSeats')} />
        <input className={input} placeholder="Image URL (optional)" value={form.imageUrl} onChange={set('imageUrl')} />

        <div className="flex gap-3 sm:col-span-2">
          <button
            disabled={saving}
            className="rounded-xl bg-indigo-600 px-6 py-3 font-semibold text-white hover:bg-indigo-700 disabled:bg-slate-300"
          >
            {saving ? 'Saving...' : isEdit ? 'Update Event' : 'Create Event'}
          </button>
          <button type="button" onClick={() => navigate('/admin')} className="rounded-xl border border-slate-300 px-6 py-3 font-medium">
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}