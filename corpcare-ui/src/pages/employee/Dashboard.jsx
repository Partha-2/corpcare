import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'

const SHIFTS = { MORNING_8_TO_4: '8AM-4PM', EVENING_4_TO_12: '4PM-12AM', NIGHT_12_TO_8: '12AM-8AM' }

export default function EmployeeDashboard() {
  const navigate = useNavigate()
  const employee = JSON.parse(sessionStorage.getItem('employee') || '{}')
  const [vitals, setVitals] = useState(null)
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!employee.id) { navigate('/employee/login'); return }
    Promise.all([
      api.get(`/employees/${employee.id}/vitals`).then(r => setVitals(r.data.data)).catch(() => {}),
      api.get(`/appointments/employee/${employee.id}`).then(r => setAppointments(r.data.data)).catch(() => {})
    ]).finally(() => setLoading(false))
  }, [])

  const handleLogout = () => {
    sessionStorage.removeItem('employee')
    navigate('/employee/login')
  }

  if (loading) return <Loading text="Loading dashboard..." />

  return (
    <div className="fade-in">
      <div className="flex-between mb-16" style={{ alignItems: 'flex-start' }}>
        <div className="page-hdr" style={{ marginBottom: 0 }}>
          <h1>Welcome, {employee.fullName}</h1>
          <p>{employee.employeeCode} · {employee.email}</p>
        </div>
        <button className="btn btn-ghost" onClick={handleLogout}>Sign Out</button>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="s-icon green">🩺</div>
          <div className="s-info">
            <div className="s-number">{vitals ? '✅' : '—'}</div>
            <div className="s-label">Health Vitals</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="s-icon blue">📅</div>
          <div className="s-info">
            <div className="s-number">{appointments.length}</div>
            <div className="s-label">Appointments</div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h2>⚡ Quick Actions</h2>
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <Link to="/employee/vitals" className="btn">🩺 My Vitals</Link>
          <Link to="/employee/book" className="btn btn-green">📅 Book Appointment</Link>
          <Link to="/employee/appointments" className="btn btn-ghost">📋 My Appointments</Link>
        </div>
      </div>

      {!employee.phone && (
        <div className="alert alert-warning" style={{ marginTop: 0, fontSize: 13 }}>
          ℹ️ No phone on file — booking still works. Add a phone via HR to get automated call & WhatsApp confirmations.
        </div>
      )}

      {appointments.length > 0 && (
        <div className="card">
          <div className="card-header">
            <h2>📅 My Appointments <span className="badge">{appointments.length}</span></h2>
            <Link to="/employee/appointments" style={{ fontSize: 13, color: 'var(--primary)', textDecoration: 'none' }}>View all →</Link>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Date</th><th>Shift</th><th>Status</th></tr>
              </thead>
              <tbody>
                {appointments.slice(0, 3).map(a => (
                  <tr key={a.id}>
                    <td><strong>{a.slot?.slotDate}</strong></td>
                    <td>{SHIFTS[a.slot?.shiftType] || '-'}</td>
                    <td><span className="status-badge status-available">● Confirmed</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {vitals && (
        <div className="card">
          <div className="card-header">
            <h2>📊 My Health Profile</h2>
          </div>
          <div className="vitals-grid">
            <div className="vital-item">
              <div className="v-label">Height</div>
              <div className="v-value">{vitals.height} <span style={{ fontSize: 14, fontWeight: 400, color: 'var(--gray-500)' }}>cm</span></div>
            </div>
            <div className="vital-item">
              <div className="v-label">Weight</div>
              <div className="v-value">{vitals.weight} <span style={{ fontSize: 14, fontWeight: 400, color: 'var(--gray-500)' }}>kg</span></div>
            </div>
            <div className="vital-item">
              <div className="v-label">Blood Pressure</div>
              <div className="v-value">{vitals.bloodPressure} <span style={{ fontSize: 14, fontWeight: 400, color: 'var(--gray-500)' }}>mmHg</span></div>
            </div>
            <div className="vital-item">
              <div className="v-label">Blood Sugar</div>
              <div className="v-value">{vitals.bloodSugar} <span style={{ fontSize: 14, fontWeight: 400, color: 'var(--gray-500)' }}>mg/dL</span></div>
            </div>
            <div className="vital-item">
              <div className="v-label">Blood Group</div>
              <div className="v-value">{vitals.bloodGroup?.replace('_', '+').replace('NEGATIVE', '-').replace('POSITIVE', '+')}</div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
