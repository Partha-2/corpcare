import { useState, useEffect } from 'react'
import api from '../../api/axios'
import Loading from '../../components/Loading'
import { toast } from '../../components/Toast'

export default function AdminHospitals() {
  const [hospitals, setHospitals] = useState([])
  const [loading, setLoading] = useState(true)
  const [expanded, setExpanded] = useState({})
  const [form, setForm] = useState({ hospitalName: '', city: '', contactEmail: '' })

  useEffect(() => {
    api.get('/hospitals').then(r => {
      setHospitals(r.data.data)
      setLoading(false)
    })
  }, [])

  const toggle = async (id) => {
    if (expanded[id]) {
      setExpanded(prev => ({ ...prev, [id]: null }))
      return
    }
    const r = await api.get(`/hospitals/${id}/slots`)
    setExpanded(prev => ({ ...prev, [id]: r.data.data }))
  }

  const handleAdd = async (e) => {
    e.preventDefault()
    try {
      await api.post('/hospitals', form)
      toast(`${form.hospitalName} registered`)
      setForm({ hospitalName: '', city: '', contactEmail: '' })
      const r = await api.get('/hospitals')
      setHospitals(r.data.data)
    } catch (err) {
      toast(err.response?.data?.message || 'Failed', 'error')
    }
  }

  if (loading) return <Loading text="Loading hospitals..." />

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <h1>All Hospitals</h1>
        <p>{hospitals.length} partner hospitals registered</p>
      </div>
      <div className="flex" style={{ gap: 24, alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: '0 0 380px', maxWidth: '100%' }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>Register New Hospital</h2>
          <form onSubmit={handleAdd}>
            <div className="form-group">
              <label>Hospital Name</label>
              <input required placeholder="e.g. Apollo Hospitals" value={form.hospitalName} onChange={e => setForm({...form, hospitalName: e.target.value})} />
            </div>
            <div className="form-group">
              <label>City</label>
              <input required placeholder="e.g. Bengaluru" value={form.city} onChange={e => setForm({...form, city: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Contact Email</label>
              <input type="email" required placeholder="contact@hospital.com" value={form.contactEmail} onChange={e => setForm({...form, contactEmail: e.target.value})} />
            </div>
            <button type="submit" className="btn-block">Register Hospital</button>
          </form>
        </div>

        <div className="card" style={{ flex: 1, minWidth: 0 }}>
          {hospitals.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🏥</div>
              <p>No hospitals yet. Register a partner hospital.</p>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Hospital</th><th>City</th><th>Email</th><th></th></tr>
                </thead>
                <tbody>
                  {hospitals.map(h => (
                    <tr key={h.id} onClick={() => toggle(h.id)} style={{ cursor: 'pointer' }}>
                      <td><strong>{h.hospitalName}</strong></td>
                      <td>{h.city}</td>
                      <td>{h.contactEmail}</td>
                      <td>
                        <span className="btn btn-sm btn-ghost" style={{ pointerEvents: 'none' }}>
                          {expanded[h.id] ? '▲' : '▼'} {expanded[h.id]?.length || 0}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {Object.entries(expanded).filter(([,v]) => v).map(([id, slots]) => {
        const hospital = hospitals.find(h => h.id === Number(id))
        if (!hospital) return null
        return (
          <div key={`expand-${id}`} className="card fade-in" style={{ marginTop: -16 }}>
            <div className="flex-between" style={{ marginBottom: 12 }}>
              <h3 style={{ fontSize: 15, fontWeight: 700 }}>📅 {hospital.hospitalName} — Slots ({slots.length})</h3>
            </div>
            <form
              onSubmit={async (e) => {
                e.preventDefault()
                const f = e.target
                const payload = { slotDate: f.date.value, shiftType: f.shift.value }
                try {
                  await api.post(`/hospitals/${hospital.id}/slots`, payload)
                  toast('Slot created')
                  f.reset()
                  const r = await api.get(`/hospitals/${hospital.id}/slots`)
                  setExpanded(prev => ({ ...prev, [hospital.id]: r.data.data }))
                } catch (err) {
                  toast(err.response?.data?.message || 'Failed', 'error')
                }
              }}
              style={{ display: 'flex', gap: 10, alignItems: 'flex-end', flexWrap: 'wrap', marginBottom: 12 }}
            >
              <div>
                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Date</label>
                <input name="date" type="date" required style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4 }} />
              </div>
              <div>
                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Shift</label>
                <select name="shift" required style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4 }}>
                  <option value="MORNING_8_TO_4">Morning (8AM-4PM)</option>
                  <option value="EVENING_4_TO_12">Evening (4PM-12AM)</option>
                  <option value="NIGHT_12_TO_8">Night (12AM-8AM)</option>
                </select>
              </div>
              <button type="submit" className="btn btn-sm">+ Create</button>
            </form>
            {slots.length === 0 ? (
              <p style={{ fontSize: 13, color: 'var(--gray-400)', padding: '8px 0' }}>No slots yet</p>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>Date</th><th>Shift</th><th>Status</th></tr>
                  </thead>
                  <tbody>
                    {slots.map(s => (
                      <tr key={s.id}>
                        <td>{s.slotDate}</td>
                        <td>{s.shiftType?.replace(/_/g, ' ')}</td>
                        <td>{s.isBooked ? <span className="status-badge status-booked">● Booked</span> : <span className="status-badge status-available">● Available</span>}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )
      })}
    </div>
  )
}
