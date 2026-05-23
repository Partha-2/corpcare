import { useState, useEffect } from 'react'
import api from '../../api/axios'

const SHIFTS = {
  MORNING_8_TO_4: '8AM-4PM',
  EVENING_4_TO_12: '4PM-12AM',
  NIGHT_12_TO_8: '12AM-8AM'
}

export default function BookAppointment() {
  const [employees, setEmployees] = useState([])
  const [hospitals, setHospitals] = useState([])
  const [availableSlots, setAvailableSlots] = useState([])
  const [selectedEmployee, setSelectedEmployee] = useState('')
  const [selectedHospital, setSelectedHospital] = useState('')
  const [selectedSlot, setSelectedSlot] = useState('')
  const [msg, setMsg] = useState(null)
  const [booked, setBooked] = useState(null)

  useEffect(() => {
    Promise.all([
      api.get('/clients'),
      api.get('/hospitals')
    ]).then(([c, h]) => {
      if (c.data.data.length > 0) {
        api.get(`/clients/${c.data.data[0].id}/employees`).then(r => setEmployees(r.data.data))
      }
      setHospitals(h.data.data)
    })
  }, [])

  const loadSlots = async (hospitalId) => {
    setSelectedHospital(hospitalId)
    setSelectedSlot('')
    const r = await api.get(`/hospitals/${hospitalId}/slots/available`)
    setAvailableSlots(r.data.data)
  }

  const handleBook = async () => {
    try {
      const r = await api.post('/appointments', { employeeId: +selectedEmployee, slotId: +selectedSlot })
      setMsg({ type: 'success', text: '✓ Appointment booked!' })
      setBooked(r.data.data)
      loadSlots(selectedHospital)
    } catch (err) {
      setMsg({ type: 'error', text: '✗ ' + (err.response?.data?.message || 'Booking failed') })
    }
  }

  const selectedEmp = employees.find(e => e.id === +selectedEmployee)
  const selectedHosp = hospitals.find(h => h.id === +selectedHospital)
  const selectedSlotData = availableSlots.find(s => s.id === +selectedSlot)

  return (
    <div>
      <div className="page-hdr">
        <h1>Book Appointment</h1>
        <p>Schedule a health checkup for an employee</p>
      </div>
      {msg && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}

      {booked && (
        <div className="alert alert-success">
          ✅ <strong>Confirmed!</strong> {booked.slot?.slotDate} ({SHIFTS[booked.slot?.shiftType] || ''})
        </div>
      )}

      <div className="flex" style={{ gap: 24, alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: '0 0 440px' }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 8 }}>📋 Booking Form</h2>
          <div className="steps">
            <div className="step">
              <div className="step-num">1</div>
              <div className="step-content">
                <div className="step-label">Employee</div>
                <select value={selectedEmployee} onChange={e => setSelectedEmployee(e.target.value)} style={{ width: '100%', padding: '10px 14px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14 }}>
                  <option value="">Choose employee...</option>
                  {employees.map(e => <option key={e.id} value={e.id}>{e.fullName} ({e.employeeCode})</option>)}
                </select>
              </div>
            </div>
            <div className="step">
              <div className="step-num">2</div>
              <div className="step-content">
                <div className="step-label">Hospital</div>
                <select value={selectedHospital} onChange={e => loadSlots(e.target.value)} disabled={!selectedEmployee} style={{ width: '100%', padding: '10px 14px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14 }}>
                  <option value="">Choose hospital...</option>
                  {hospitals.map(h => <option key={h.id} value={h.id}>{h.hospitalName} — {h.city}</option>)}
                </select>
              </div>
            </div>
            <div className="step">
              <div className="step-num">3</div>
              <div className="step-content">
                <div className="step-label">Time Slot</div>
                <select value={selectedSlot} onChange={e => setSelectedSlot(e.target.value)} disabled={!selectedHospital} style={{ width: '100%', padding: '10px 14px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14 }}>
                  <option value="">Choose slot...</option>
                  {availableSlots.map(s => <option key={s.id} value={s.id}>{s.slotDate} — {SHIFTS[s.shiftType] || s.shiftType}</option>)}
                </select>
                {selectedHospital && availableSlots.length === 0 && (
                  <p style={{ color: '#dc2626', fontSize: 13, marginTop: 6 }}>No slots available</p>
                )}
              </div>
            </div>
          </div>
          <button className="btn-block" onClick={handleBook} disabled={!selectedSlot} style={{ marginTop: 8 }}>
            {selectedSlot ? '✓ Confirm Booking' : 'Complete all steps'}
          </button>
        </div>

        <div className="card" style={{ flex: 1 }}>
          <div className="card-header">
            <h2>📋 Booking Summary</h2>
          </div>
          {!selectedEmployee ? (
            <div className="empty-state">
              <div className="empty-icon">📅</div>
              <p>Select an employee to begin</p>
            </div>
          ) : (
            <table>
              <tbody>
                <tr><td style={{ fontWeight: 600, width: 140 }}>Employee</td><td>{selectedEmp?.fullName || '-'} ({selectedEmp?.employeeCode || '-'})</td></tr>
                <tr><td style={{ fontWeight: 600 }}>Hospital</td><td>{selectedHosp?.hospitalName || 'Not selected'}</td></tr>
                <tr><td style={{ fontWeight: 600 }}>Date & Time</td><td>{selectedSlotData ? `${selectedSlotData.slotDate} — ${SHIFTS[selectedSlotData.shiftType] || selectedSlotData.shiftType}` : 'Not selected'}</td></tr>
                <tr><td style={{ fontWeight: 600 }}>Status</td><td>{selectedSlot ? <span className="status-badge status-available">Ready to book</span> : <span style={{ color: 'var(--gray-400)' }}>Pending</span>}</td></tr>
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  )
}
