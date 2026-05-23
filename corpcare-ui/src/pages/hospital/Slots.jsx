import { useState, useEffect } from 'react'
import api from '../../api/axios'

const SHIFTS = {
  MORNING_8_TO_4: { label: 'Morning (8AM - 4PM)', short: '8AM-4PM' },
  EVENING_4_TO_12: { label: 'Evening (4PM - 12AM)', short: '4PM-12AM' },
  NIGHT_12_TO_8: { label: 'Night (12AM - 8AM)', short: '12AM-8AM' }
}

export default function Slots() {
  const [hospitals, setHospitals] = useState([])
  const [hospital, setHospital] = useState(null)
  const [slots, setSlots] = useState([])
  const [form, setForm] = useState({ slotDate: '', shiftType: 'MORNING_8_TO_4' })
  const [msg, setMsg] = useState(null)
  const [view, setView] = useState('all')

  useEffect(() => {
    api.get('/hospitals').then(r => {
      setHospitals(r.data.data)
      if (r.data.data.length > 0) {
        setHospital(r.data.data[0])
        load(r.data.data[0].id)
      }
    })
  }, [])

  const handleHospitalChange = (id) => {
    const h = hospitals.find(x => x.id === +id)
    setHospital(h)
    setMsg(null)
    if (h) load(h.id)
  }

  const load = (id) => api.get(`/hospitals/${id}/slots`).then(r => setSlots(r.data.data))

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await api.post(`/hospitals/${hospital.id}/slots`, form)
      setMsg({ type: 'success', text: '✓ Slot created' })
      setForm({ slotDate: '', shiftType: 'MORNING_8_TO_4' })
      load(hospital.id)
    } catch (err) {
      setMsg({ type: 'error', text: '✗ ' + (err.response?.data?.message || 'Failed') })
    }
  }

  const displaySlots = view === 'available' ? slots.filter(s => !s.isBooked) : slots
  const availCount = slots.filter(s => !s.isBooked).length
  const bookedCount = slots.length - availCount

  return (
    <div>
      <div className="page-hdr">
        <div className="flex-between" style={{ alignItems: 'flex-start' }}>
          <div>
            <h1>Slots</h1>
            <p>Manage your appointment availability</p>
          </div>
          <select value={hospital?.id || ''} onChange={e => handleHospitalChange(e.target.value)} style={{ padding: '8px 12px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14, minWidth: 200 }}>
            {hospitals.map(h => <option key={h.id} value={h.id}>{h.hospitalName}</option>)}
          </select>
        </div>
      </div>
      {msg && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}

      <div className="stats-grid">
        <div className="stat-card">
          <div className="s-icon blue">📅</div>
          <div className="s-info">
            <div className="s-number">{slots.length}</div>
            <div className="s-label">Total</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="s-icon green">✅</div>
          <div className="s-info">
            <div className="s-number">{availCount}</div>
            <div className="s-label">Available</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="s-icon red">📋</div>
          <div className="s-info">
            <div className="s-number">{bookedCount}</div>
            <div className="s-label">Booked</div>
          </div>
        </div>
      </div>

      <div className="flex" style={{ gap: 24, alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: '0 0 360px' }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>➕ Create Slot</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Date</label>
              <input type="date" required value={form.slotDate} onChange={e => setForm({...form, slotDate: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Shift</label>
              <select value={form.shiftType} onChange={e => setForm({...form, shiftType: e.target.value})}>
                {Object.entries(SHIFTS).map(([k, v]) => <option key={k} value={k}>{v.label}</option>)}
              </select>
            </div>
            <button type="submit" className="btn-block">Create Slot</button>
          </form>
        </div>

        <div className="card" style={{ flex: 1 }}>
          <div className="flex-between mb-16">
            <h2 style={{ fontSize: 16, fontWeight: 700 }}>📋 Slots</h2>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className={`btn btn-sm ${view === 'all' ? '' : 'btn-ghost'}`} onClick={() => setView('all')}>All</button>
              <button className={`btn btn-sm ${view === 'available' ? '' : 'btn-ghost'}`} onClick={() => setView('available')}>Available</button>
            </div>
          </div>
          {displaySlots.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📅</div>
              <p>{view === 'available' ? 'No available slots' : 'No slots created yet'}</p>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Date</th><th>Shift</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {displaySlots.map(s => (
                    <tr key={s.id}>
                      <td><strong>{s.slotDate}</strong></td>
                      <td style={{ color: 'var(--gray-500)' }}>{SHIFTS[s.shiftType]?.short || s.shiftType}</td>
                      <td>
                        {s.isBooked
                          ? <span className="status-badge status-booked">● Booked</span>
                          : <span className="status-badge status-available">● Available</span>
                        }
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
