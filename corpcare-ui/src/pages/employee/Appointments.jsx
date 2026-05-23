import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../../api/axios'

const SHIFTS = { MORNING_8_TO_4: '8AM-4PM', EVENING_4_TO_12: '4PM-12AM', NIGHT_12_TO_8: '12AM-8AM' }

export default function EmployeeAppointments() {
  const navigate = useNavigate()
  const employee = JSON.parse(sessionStorage.getItem('employee') || '{}')
  const [bookings, setBookings] = useState([])
  const [msg, setMsg] = useState(null)

  const load = () => {
    if (!employee.id) { navigate('/employee/login'); return }
    api.get(`/appointments/employee/${employee.id}`).then(r => setBookings(r.data.data)).catch(() => {})
  }

  useEffect(load, [])

  const handleCancel = async (id) => {
    try {
      await api.put(`/appointments/${id}/cancel`)
      setMsg({ type: 'success', text: '✓ Appointment cancelled' })
      load()
    } catch (err) {
      setMsg({ type: 'error', text: '✗ ' + (err.response?.data?.message || 'Failed') })
    }
  }

  return (
    <div>
      <div className="page-hdr">
        <h1>My Appointments</h1>
        <p>Your health checkup history</p>
      </div>
      {msg && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}
      <div className="card">
        <div className="card-header"><h2>📋 Appointments</h2></div>
        {bookings.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📅</div>
            <p>No appointments yet</p>
            <Link to="/employee/book" className="btn mt-16">Book Now</Link>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Date</th><th>Shift</th><th>Hospital</th><th>Status</th><th></th></tr>
              </thead>
              <tbody>
                {bookings.map(b => (
                  <tr key={b.id}>
                    <td><strong>{b.slot?.slotDate}</strong></td>
                    <td>{SHIFTS[b.slot?.shiftType] || '-'}</td>
                    <td style={{ color: 'var(--gray-500)' }}>{b.slot?.hospital?.hospitalName}</td>
                    <td><span className="status-badge status-available">● Confirmed</span></td>
                    <td>
                      <button className="btn btn-sm btn-danger" onClick={() => handleCancel(b.id)}>Cancel</button>
                    </td>
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
