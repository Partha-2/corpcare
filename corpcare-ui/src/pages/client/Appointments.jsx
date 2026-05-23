import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'

const SHIFTS = {
  MORNING_8_TO_4: '8AM-4PM',
  EVENING_4_TO_12: '4PM-12AM',
  NIGHT_12_TO_8: '12AM-8AM'
}

export default function Appointments() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/clients').then(c => {
      if (c.data.data.length > 0) {
        api.get(`/clients/${c.data.data[0].id}/employees`).then(e => {
          const ids = e.data.data.map(emp => emp.id)
          Promise.all(ids.map(id =>
            api.get(`/employees/${id}/vitals`).then(() => null).catch(() => null)
          )).then(() => setLoading(false))
        })
      } else {
        setLoading(false)
      }
    })
  }, [])

  if (loading) return <div className="card">Loading...</div>

  return (
    <div>
      <div className="page-hdr">
        <h1>Appointments</h1>
        <p>Your booked health checkups</p>
      </div>

      <div className="card">
        <div className="card-header">
          <h2>📋 Appointment History</h2>
        </div>
        {bookings.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📅</div>
            <p>No appointments yet</p>
            <Link to="/client/book" className="btn mt-16">Book an Appointment</Link>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Employee</th><th>Date</th><th>Shift</th><th>Status</th></tr>
              </thead>
              <tbody>
                {bookings.map(b => (
                  <tr key={b.id}>
                    <td><strong>{b.employee?.fullName}</strong></td>
                    <td>{b.slot?.slotDate}</td>
                    <td>{SHIFTS[b.slot?.shiftType] || '-'}</td>
                    <td><span className="status-badge status-available">● Confirmed</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
