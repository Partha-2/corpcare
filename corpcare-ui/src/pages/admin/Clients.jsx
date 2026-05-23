import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'

export default function AdminClients() {
  const [clients, setClients] = useState([])
  const [expanded, setExpanded] = useState({})
  const [form, setForm] = useState({ companyName: '', contactEmail: '', contactPhone: '', maxEmployees: 100 })
  const [msg, setMsg] = useState(null)

  useEffect(() => {
    api.get('/clients').then(r => setClients(r.data.data))
  }, [])

  const toggle = async (id) => {
    if (expanded[id]) {
      setExpanded(prev => ({ ...prev, [id]: null }))
      return
    }
    const r = await api.get(`/clients/${id}/employees`)
    setExpanded(prev => ({ ...prev, [id]: r.data.data }))
  }

  const handleAdd = async (e) => {
    e.preventDefault()
    try {
      await api.post('/clients', form)
      setMsg({ type: 'success', text: `✓ ${form.companyName} registered` })
      setForm({ companyName: '', contactEmail: '', contactPhone: '', maxEmployees: 100 })
      const r = await api.get('/clients')
      setClients(r.data.data)
    } catch (err) {
      setMsg({ type: 'error', text: '✗ ' + (err.response?.data?.message || 'Failed') })
    }
  }

  return (
    <div>
      <div className="page-hdr">
        <h1>All Clients</h1>
        <p>{clients.length} corporate clients registered</p>
      </div>
      {msg && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}

      <div className="flex" style={{ gap: 24, alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: '0 0 380px' }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>➕ Register New Client</h2>
          <form onSubmit={handleAdd}>
            <div className="form-group">
              <label>Company Name</label>
              <input required placeholder="e.g. Acme Corp" value={form.companyName} onChange={e => setForm({...form, companyName: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Contact Email</label>
              <input type="email" required placeholder="hr@acme.com" value={form.contactEmail} onChange={e => setForm({...form, contactEmail: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Contact Phone</label>
              <input required placeholder="+919900000000" value={form.contactPhone} onChange={e => setForm({...form, contactPhone: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Max Employees</label>
              <input type="number" min={1} max={100} value={form.maxEmployees} onChange={e => setForm({...form, maxEmployees: +e.target.value})} />
            </div>
            <button type="submit" className="btn-block">Register Client</button>
          </form>
        </div>

        <div className="card" style={{ flex: 1 }}>
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Company</th><th>Email</th><th>Phone</th><th>Max Employees</th><th></th></tr>
              </thead>
              <tbody>
                {clients.map(c => (
                  <>
                    <tr key={c.id} onClick={() => toggle(c.id)} style={{ cursor: 'pointer' }}>
                      <td><strong>{c.companyName}</strong></td>
                      <td>{c.contactEmail}</td>
                      <td>{c.contactPhone}</td>
                      <td>{c.maxEmployees}</td>
                      <td>
                        <span className="btn btn-sm btn-ghost">
                          {expanded[c.id] ? '▲' : '▼'} Employees
                        </span>
                      </td>
                    </tr>
                    {expanded[c.id] && (
                      <>
                        <tr style={{ background: 'var(--gray-100)' }}>
                          <td colSpan={5} style={{ padding: '12px 16px' }}>
                            <form
                              onSubmit={async (e) => {
                                e.preventDefault()
                                const empForm = e.target
                                const payload = {
                                  employeeCode: empForm.code.value,
                                  fullName: empForm.name.value,
                                  email: empForm.email.value,
                                  phone: empForm.phone.value
                                }
                                try {
                                  await api.post(`/clients/${c.id}/employees`, payload)
                                  setMsg({ type: 'success', text: `✓ ${payload.fullName} added` })
                                  empForm.reset()
                                  const r = await api.get(`/clients/${c.id}/employees`)
                                  setExpanded(prev => ({ ...prev, [c.id]: r.data.data }))
                                } catch (err) {
                                  setMsg({ type: 'error', text: '✗ ' + (err.response?.data?.message || 'Failed') })
                                }
                              }}
                              style={{ display: 'flex', gap: 10, alignItems: 'flex-end' }}
                            >
                              <div>
                                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Code</label>
                                <input name="code" required placeholder="EMP001" style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4, width: 100 }} />
                              </div>
                              <div>
                                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Name</label>
                                <input name="name" required placeholder="Full name" style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4, width: 160 }} />
                              </div>
                              <div>
                                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Email</label>
                                <input name="email" type="email" required placeholder="email@co.com" style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4, width: 180 }} />
                              </div>
                              <div>
                                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Phone</label>
                                <input name="phone" placeholder="+919999999999" style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4, width: 140 }} />
                              </div>
                              <button type="submit" className="btn btn-sm">+ Add</button>
                            </form>
                          </td>
                        </tr>
                        {expanded[c.id].map(emp => (
                          <tr key={`emp-${emp.id}`} style={{ background: 'var(--gray-50)' }}>
                            <td style={{ paddingLeft: 48 }} colSpan={5}>
                              👤 <strong>{emp.fullName}</strong> — {emp.employeeCode} — {emp.email}
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
