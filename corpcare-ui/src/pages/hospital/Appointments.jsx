import { useState, useEffect } from 'react'
import api from '../../api/axios'
import Loading from '../../components/Loading'
import { toast } from '../../components/Toast'

const SHIFTS = { MORNING_8_TO_4: '8AM-4PM', EVENING_4_TO_12: '4PM-12AM', NIGHT_12_TO_8: '12AM-8AM' }

export default function HospitalAppointments() {
  const [hospitals, setHospitals] = useState([])
  const [loading, setLoading] = useState(true)
  const [hospital, setHospital] = useState(null)
  const [appointments, setAppointments] = useState([])

  const loadAppts = (id) => {
    api.get(`/appointments/hospital/${id}`).then(r => setAppointments(r.data.data))
  }

  useEffect(() => {
    api.get('/hospitals').then(r => {
      setHospitals(r.data.data)
      if (r.data.data.length > 0) {
        setHospital(r.data.data[0])
        loadAppts(r.data.data[0].id)
      }
      setLoading(false)
    })
  }, [])

  const handleHospitalChange = (id) => {
    const h = hospitals.find(x => x.id === +id)
    setHospital(h)
    if (h) loadAppts(h.id)
  }

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this appointment?')) return
    try {
      await api.put(`/appointments/${id}/cancel`)
      toast('Appointment cancelled')
      loadAppts(hospital.id)
    } catch (err) {
      toast(err.response?.data?.message || 'Failed', 'error')
    }
  }

  if (loading) return <Loading text="Loading appointments..." />

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <div className="flex-between" style={{ alignItems: 'flex-start' }}>
          <div>
            <h1>Appointments</h1>
            <p>Manage booked appointments</p>
          </div>
          <select value={hospital?.id || ''} onChange={e => handleHospitalChange(e.target.value)} style={{ padding: '8px 12px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14, minWidth: 200 }}>
            {hospitals.map(h => <option key={h.id} value={h.id}>{h.hospitalName}</option>)}
          </select>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h2>📋 Booked Appointments <span className="badge">{appointments.length}</span></h2>
        </div>
        {appointments.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📅</div>
            <p>No bookings yet</p>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Date</th><th>Shift</th><th>Employee</th><th>Email</th><th></th></tr>
              </thead>
              <tbody>
                {appointments.map(a => (
                  <tr key={a.id}>
                    <td><strong>{a.slot?.slotDate}</strong></td>
                    <td style={{ color: 'var(--gray-500)' }}>{SHIFTS[a.slot?.shiftType] || a.slot?.shiftType}</td>
                    <td>{a.employee?.fullName}</td>
                    <td style={{ color: 'var(--gray-500)' }}>{a.employee?.email}</td>
                    <td>
                      <button className="btn btn-sm btn-danger" onClick={() => handleCancel(a.id)}>Cancel</button>
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
