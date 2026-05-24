import { useState, useEffect } from 'react'
import api from '../../api/axios'
import Loading from '../../components/Loading'
import { toast } from '../../components/Toast'

export default function AdminClients() {
  const [clients, setClients] = useState([])
  const [loading, setLoading] = useState(true)
  const [expanded, setExpanded] = useState({})
  const [form, setForm] = useState({ companyName: '', contactEmail: '', contactPhone: '', maxEmployees: 100 })

  useEffect(() => {
    api.get('/clients').then(r => {
      setClients(r.data.data)
      setLoading(false)
    })
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
      toast(`${form.companyName} registered`)
      setForm({ companyName: '', contactEmail: '', contactPhone: '', maxEmployees: 100 })
      const r = await api.get('/clients')
      setClients(r.data.data)
    } catch (err) {
      toast(err.response?.data?.message || 'Failed', 'error')
    }
  }

  if (loading) return <Loading text="Loading clients..." />

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <h1>All Clients</h1>
        <p>{clients.length} corporate clients registered</p>
      </div>

      <div className="flex" style={{ gap: 24, alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: '0 0 380px', maxWidth: '100%' }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>Register New Client</h2>
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

        <div className="card" style={{ flex: 1, minWidth: 0 }}>
          {clients.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🏢</div>
              <p>No clients yet. Register your first corporate client.</p>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Company</th><th>Email</th><th>Phone</th><th>Max</th><th></th></tr>
                </thead>
                <tbody>
                  {clients.map(c => (
                    <tr key={c.id} onClick={() => toggle(c.id)} style={{ cursor: 'pointer' }}>
                      <td><strong>{c.companyName}</strong></td>
                      <td>{c.contactEmail}</td>
                      <td>{c.contactPhone}</td>
                      <td>{c.maxEmployees}</td>
                      <td>
                        <span className="btn btn-sm btn-ghost" style={{ pointerEvents: 'none' }}>
                          {expanded[c.id] ? '▲' : '▼'} {expanded[c.id]?.length || 0}
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

      {Object.entries(expanded).filter(([,v]) => v).map(([id, emps]) => {
        const client = clients.find(c => c.id === Number(id))
        if (!client) return null
        return (
          <div key={`expand-${id}`} className="card fade-in" style={{ marginTop: -16 }}>
            <div className="flex-between" style={{ marginBottom: 12 }}>
              <h3 style={{ fontSize: 15, fontWeight: 700 }}>👥 {client.companyName} — Employees ({emps.length})</h3>
            </div>
            <form
              onSubmit={async (e) => {
                e.preventDefault()
                const f = e.target
                const payload = {
                  employeeCode: f.code.value,
                  fullName: f.name.value,
                  email: f.email.value,
                  phone: f.phone.value
                }
                try {
                  await api.post(`/clients/${client.id}/employees`, payload)
                  toast(`${payload.fullName} added`)
                  f.reset()
                  const r = await api.get(`/clients/${client.id}/employees`)
                  setExpanded(prev => ({ ...prev, [client.id]: r.data.data }))
                } catch (err) {
                  toast(err.response?.data?.message || 'Failed', 'error')
                }
              }}
              style={{ display: 'flex', gap: 10, alignItems: 'flex-end', flexWrap: 'wrap', marginBottom: 12 }}
            >
              <div>
                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Code</label>
                <input name="code" required placeholder="EMP001" style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4, width: 90 }} />
              </div>
              <div>
                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Name</label>
                <input name="name" required placeholder="Full name" style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4, width: 150 }} />
              </div>
              <div>
                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Email</label>
                <input name="email" type="email" required placeholder="email@co.com" style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4, width: 170 }} />
              </div>
              <div>
                <label style={{ fontSize: 11, color: 'var(--gray-500)', display: 'block', marginBottom: 2 }}>Phone</label>
                <input name="phone" placeholder="+919999999999" style={{ padding: '6px 10px', fontSize: 13, border: '1px solid var(--gray-200)', borderRadius: 4, width: 130 }} />
              </div>
              <button type="submit" className="btn btn-sm">+ Add</button>
            </form>
            {emps.length === 0 ? (
              <p style={{ fontSize: 13, color: 'var(--gray-400)', padding: '8px 0' }}>No employees yet</p>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>Code</th><th>Name</th><th>Email</th><th>Phone</th></tr>
                  </thead>
                  <tbody>
                    {emps.map(emp => (
                      <tr key={emp.id}>
                        <td><span className="badge">{emp.employeeCode}</span></td>
                        <td>{emp.fullName}</td>
                        <td>{emp.email}</td>
                        <td>{emp.phone || '—'}</td>
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
