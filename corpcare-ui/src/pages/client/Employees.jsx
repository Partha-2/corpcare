import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'
import { toast } from '../../components/Toast'

export default function ClientEmployees() {
  const [clients, setClients] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedId, setSelectedId] = useState('')
  const [employees, setEmployees] = useState([])
  const [empLoading, setEmpLoading] = useState(false)
  const [selectedClient, setSelectedClient] = useState(null)
  const [form, setForm] = useState({ employeeCode: '', fullName: '', email: '', phone: '' })

  useEffect(() => {
    api.get('/clients').then(r => {
      setClients(r.data.data)
      if (r.data.data.length > 0) {
        setSelectedId(String(r.data.data[0].id))
        setSelectedClient(r.data.data[0])
        return load(r.data.data[0].id)
      }
      setLoading(false)
    })
  }, [])

  const load = async (id) => {
    setEmpLoading(true)
    const r = await api.get(`/clients/${id}/employees`)
    setEmployees(r.data.data)
    setEmpLoading(false)
    setLoading(false)
  }

  const handleClientChange = (id) => {
    setSelectedId(id)
    const c = clients.find(x => x.id === +id)
    setSelectedClient(c)
    load(id)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await api.post(`/clients/${selectedId}/employees`, form)
      toast(`${form.fullName} added`)
      setForm({ employeeCode: '', fullName: '', email: '', phone: '' })
      load(selectedId)
    } catch (err) {
      toast(err.response?.data?.message || 'Failed', 'error')
    }
  }

  if (loading) return <Loading text="Loading employees..." />

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <h1>Employees</h1>
        <p>Add and manage your workforce</p>
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <div className="flex-between">
          <div className="flex-center" style={{ gap: 12 }}>
            <strong style={{ fontSize: 14 }}>Company:</strong>
            <select value={selectedId} onChange={e => handleClientChange(e.target.value)} style={{ width: 300, padding: '8px 12px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14 }}>
              {clients.map(c => <option key={c.id} value={c.id}>{c.companyName}</option>)}
            </select>
          </div>
          {selectedClient && (
            <span style={{ fontSize: 13, color: 'var(--gray-500)' }}>
              {employees.length} / {selectedClient.maxEmployees} employees
            </span>
          )}
        </div>
      </div>

      <div className="flex" style={{ gap: 24, alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: '0 0 360px' }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>➕ Add Employee</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Employee Code</label>
              <input required placeholder="e.g. EMP001" value={form.employeeCode} onChange={e => setForm({...form, employeeCode: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Full Name</label>
              <input required placeholder="e.g. John Doe" value={form.fullName} onChange={e => setForm({...form, fullName: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input type="email" required placeholder="john@company.com" value={form.email} onChange={e => setForm({...form, email: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Phone</label>
              <input type="tel" placeholder="+919876543210" value={form.phone} onChange={e => setForm({...form, phone: e.target.value})} />
            </div>
            <button type="submit" className="btn-block">Add Employee</button>
          </form>
        </div>

        <div className="card" style={{ flex: 1 }}>
          <div className="card-header">
            <h2>👥 Employees <span className="badge">{employees.length}</span></h2>
          </div>
          {empLoading ? (
            <Loading text="" />
          ) : employees.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">👤</div>
              <p>No employees added yet</p>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr><th>Code</th><th>Name</th><th>Email</th><th>Phone</th><th>Status</th><th></th></tr>
                </thead>
                <tbody>
                  {employees.map(e => (
                    <tr key={e.id}>
                      <td><strong>{e.employeeCode}</strong></td>
                      <td>{e.fullName}</td>
                      <td style={{ color: 'var(--gray-500)' }}>{e.email}</td>
                      <td>{e.phone || '—'}</td>
                      <td><span className="status-badge status-available">Active</span></td>
                      <td><Link to={`/client/employees/${e.id}/vitals`} className="btn btn-sm btn-ghost">Vitals</Link></td>
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
