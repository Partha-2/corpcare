import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'

export default function AdminHospitals() {
  const [hospitals, setHospitals] = useState([])
  const [expanded, setExpanded] = useState({})
  const [form, setForm] = useState({ hospitalName: '', city: '', contactEmail: '' })
  const [msg, setMsg] = useState(null)

  useEffect(() => {
    api.get('/hospitals').then(r => setHospitals(r.data.data))
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
      setMsg({ type: 'success', text: `✓ ${form.hospitalName} registered` })
      setForm({ hospitalName: '', city: '', contactEmail: '' })
      const r = await api.get('/hospitals')
      setHospitals(r.data.data)
    } catch (err) {
      setMsg({ type: 'error', text: '✗ ' + (err.response?.data?.message || 'Failed') })
    }
  }

  return (
    <div>
      <div className="page-hdr">
        <h1>All Hospitals</h1>
        <p>{hospitals.length} partner hospitals registered</p>
      </div>
      {msg && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}

      <div className="flex" style={{ gap: 24, alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: '0 0 380px' }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>🏥 Register New Hospital</h2>
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

        <div className="card" style={{ flex: 1 }}>
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Hospital</th><th>City</th><th>Email</th><th></th></tr>
              </thead>
              <tbody>
                {hospitals.map(h => (
                  <>
                    <tr key={h.id} onClick={() => toggle(h.id)} style={{ cursor: 'pointer' }}>
                      <td><strong>{h.hospitalName}</strong></td>
                      <td>{h.city}</td>
                      <td>{h.contactEmail}</td>
                      <td>
                        <span className="btn btn-sm btn-ghost">
                          {expanded[h.id] ? '▲' : '▼'} Slots
                        </span>
                      </td>
                    </tr>
                    {expanded[h.id] && (
                      <>
                        <tr style={{ background: 'var(--gray-100)' }}>
                          <td colSpan={4} style={{ padding: '12px 16px' }}>
                            <form
                              onSubmit={async (e) => {
                                e.preventDefault()
                                const slotForm = e.target
                                const payload = {
                                  slotDate: slotForm.date.value,
                                  shiftType: slotForm.shift.value
                                }
                                try {
                                  await api.post(`/hospitals/${h.id}/slots`, payload)
                                  setMsg({ type: 'success', text: `✓ Slot created for ${h.hospitalName}` })
                                  slotForm.reset()
                                  const r = await api.get(`/hospitals/${h.id}/slots`)
                                  setExpanded(prev => ({ ...prev, [h.id]: r.data.data }))
                                } catch (err) {
                                  setMsg({ type: 'error', text: '✗ ' + (err.response?.data?.message || 'Failed') })
                                }
                              }}
                              style={{ display: 'flex', gap: 10, alignItems: 'flex-end' }}
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
                          </td>
                        </tr>
                        {expanded[h.id].map(s => (
                          <tr key={`slot-${s.id}`} style={{ background: 'var(--gray-50)' }}>
                            <td style={{ paddingLeft: 48 }} colSpan={4}>
                              📅 {s.slotDate} — {s.isBooked ? <span className="status-badge status-booked">Booked</span> : <span className="status-badge status-available">Available</span>}
                            </td>
                          </tr>
                        ))}
                      </>
                    )}
                  </>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}
