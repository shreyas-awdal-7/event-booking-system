// Backend na mile (jaise sirf frontend deploy ho) tab dikhane ke liye demo events
const daysFromNow = (days) => {
  const d = new Date()
  d.setDate(d.getDate() + days)
  d.setHours(18, 0, 0, 0)
  return d.toISOString()
}

export const sampleEvents = [
  {
    id: 1, title: 'Sunburn Arena ft. DJ Snake', category: 'Music', venue: 'Mahalaxmi Lawns, Pune',
    eventDate: daysFromNow(10), price: 1499, totalSeats: 200, availableSeats: 146,
    description: "India's biggest EDM night with live performances and stunning visuals.",
    imageUrl: 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800',
  },
  {
    id: 2, title: 'Stand-Up Comedy Night', category: 'Comedy', venue: 'Phoenix Marketcity, Pune',
    eventDate: daysFromNow(5), price: 499, totalSeats: 120, availableSeats: 8,
    description: 'An evening of laughter with top stand-up comedians.',
    imageUrl: 'https://images.unsplash.com/photo-1585699324551-f6c309eedeca?w=800',
  },
  {
    id: 3, title: 'Tech Conference 2026', category: 'Conference', venue: 'JW Marriott, Bangalore',
    eventDate: daysFromNow(21), price: 2999, totalSeats: 300, availableSeats: 212,
    description: 'Talks on AI, cloud and full-stack engineering from industry leaders.',
    imageUrl: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800',
  },
  {
    id: 4, title: 'IPL Screening: MI vs CSK', category: 'Sports', venue: 'PVR Icon, Mumbai',
    eventDate: daysFromNow(3), price: 299, totalSeats: 80, availableSeats: 0,
    description: 'Watch the big match live on the big screen with fellow fans.',
    imageUrl: 'https://images.unsplash.com/photo-1531415074968-036ba1b575da?w=800',
  },
  {
    id: 5, title: 'Marathi Natak: Natsamrat', category: 'Theatre', venue: 'Balgandharva Rangmandir, Pune',
    eventDate: daysFromNow(14), price: 399, totalSeats: 150, availableSeats: 97,
    description: 'A classic Marathi play performed by a renowned theatre group.',
    imageUrl: 'https://images.unsplash.com/photo-1503095396549-807759245b35?w=800',
  },
  {
    id: 6, title: 'Full-Stack Bootcamp Workshop', category: 'Workshop', venue: 'Online / Pune',
    eventDate: daysFromNow(7), price: 0, totalSeats: 50, availableSeats: 31,
    description: 'Hands-on workshop: build and deploy a Spring Boot + React app.',
    imageUrl: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800',
  },
].sort((a, b) => new Date(a.eventDate) - new Date(b.eventDate))

export const sampleCategories = [...new Set(sampleEvents.map((e) => e.category))].sort()

export const filterSampleEvents = (q = '', category = '') =>
  sampleEvents.filter(
    (e) =>
      e.title.toLowerCase().includes(q.trim().toLowerCase()) &&
      (!category || e.category === category)
  )
