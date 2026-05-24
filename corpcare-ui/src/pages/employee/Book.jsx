import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'
import { toast } from '../../components/Toast'

const SHIFTS = { MORNING_8_TO_4: '8AM-4PM', EVENING_4_TO_12: '4PM-12AM', NIGHT_12_TO_8: '12AM-8AM' }

export default function EmployeeBook() {
  const navigate = useNavigate()
  const employee = JSON.parse(sessionStorage.getItem('employee') || '{}')
  const [hospitals, setHospitals] = useState([])
  const [slots, setSlots] = useState([])
  const [slotLoading, setSlotLoading] = useState(false)
  const [selectedHospital, setSelectedHospital] = useState('')
  const [selectedSlot, setSelectedSlot] = useState('')
  const [booking, setBooking] = useState(false)

  useEffect(() => {
    if (!employee.id) { navigate('/employee/login'); return }
    api.get('/hospitals').then(r => setHospitals(r.data.data))
  }, [])

  const loadSlots = async (id) => {
    setSelectedHospital(id)
    setSelectedSlot('')
    setSlotLoading(true)
    const r = await api.get(`/hospitals/${id}/slots/available`)
    setSlots(r.data.data)
    setSlotLoading(false)
  }

  const handleBook = async () => {
    setBooking(true)
    try {
      const r = await api.post('/appointments', { employeeId: employee.id, slotId: +selectedSlot })
      toast('Appointment booked! Check WhatsApp for confirmation.')
      loadSlots(selectedHospital)
    } catch (err) {
      toast(err.response?.data?.message || 'Failed', 'error')
    } finally {
      setBooking(false)
    }
  }

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <h1>Book Appointment</h1>
        <p>Schedule your health checkup</p>
      </div>

      <div className="flex" style={{ gap: 24, alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: '0 0 400px' }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>📋 Book Now</h2>
          <div className="steps">
            <div className="step">
              <div className="step-num">1</div>
              <div className="step-content">
                <div className="step-label">Employee</div>
                <p style={{ fontSize: 14, fontWeight: 600 }}>{employee.fullName}</p>
              </div>
            </div>
            <div className="step">
              <div className="step-num">2</div>
              <div className="step-content">
                <div className="step-label">Hospital</div>
                <select value={selectedHospital} onChange={e => loadSlots(e.target.value)} style={{ width: '100%', padding: '10px 14px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14 }}>
                  <option value="">Choose hospital...</option>
                  {hospitals.map(h => <option key={h.id} value={h.id}>{h.hospitalName}</option>)}
                </select>
              </div>
            </div>
            <div className="step">
              <div className="step-num">3</div>
              <div className="step-content">
                <div className="step-label">Time Slot</div>
                {slotLoading ? (
                  <div style={{ padding: '10px 0', fontSize: 13, color: 'var(--gray-400)' }}>Loading slots...</div>
                ) : (
                  <select value={selectedSlot} onChange={e => setSelectedSlot(e.target.value)} disabled={!selectedHospital} style={{ width: '100%', padding: '10px 14px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14 }}>
                    <option value="">Choose slot...</option>
                    {slots.map(s => <option key={s.id} value={s.id}>{s.slotDate} — {SHIFTS[s.shiftType] || s.shiftType}</option>)}
                  </select>
                )}
                {selectedHospital && !slotLoading && slots.length === 0 && <p style={{ color: '#dc2626', fontSize: 13, marginTop: 6 }}>No slots available</p>}
              </div>
            </div>
          </div>
          <button className="btn-block" onClick={handleBook} disabled={!selectedSlot || booking}>
            {booking ? 'Booking...' : 'Confirm Booking'}
          </button>
        </div>

        <div className="card" style={{ flex: 1 }}>
          <div className="card-header"><h2>ℹ️ How It Works</h2></div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {[
              { icon: '👤', title: 'Your Identity', desc: 'Signed in as ' + employee.fullName },
              { icon: '🏥', title: 'Pick a Hospital', desc: 'Choose from partner hospitals' },
              { icon: '⏰', title: 'Select Slot', desc: 'One slot = one person — first come, first served' },
              { icon: '📞', title: 'Voice Confirmation', desc: employee.phone ? `You'll get a Bolna.ai call on ${employee.phone}` : 'Add phone to receive a confirmation call' },
              { icon: '💬', title: 'WhatsApp Message', desc: 'Slot details, location & timing sent automatically' }
            ].map((item, i) => (
              <div key={i} style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
                <span style={{ fontSize: 24 }}>{item.icon}</span>
                <div>
                  <strong style={{ fontSize: 14 }}>{item.title}</strong>
                  <p style={{ fontSize: 13, color: 'var(--gray-500)', marginTop: 2 }}>{item.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
