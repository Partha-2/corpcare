import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'
import { toast } from '../../components/Toast'

const BLOOD_GROUPS = ['A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','O_POSITIVE','O_NEGATIVE','AB_POSITIVE','AB_NEGATIVE']
const BG_LABELS = { A_POSITIVE: 'A+', A_NEGATIVE: 'A-', B_POSITIVE: 'B+', B_NEGATIVE: 'B-', O_POSITIVE: 'O+', O_NEGATIVE: 'O-', AB_POSITIVE: 'AB+', AB_NEGATIVE: 'AB-' }

export default function EmployeeVitals() {
  const { employeeId } = useParams()
  const [employee, setEmployee] = useState(null)
  const [vitals, setVitals] = useState(null)
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState({ height: '', weight: '', bloodPressure: '', bloodSugar: '', bloodGroup: '' })

  useEffect(() => {
    Promise.all([
      api.get(`/employees/${employeeId}`).then(r => setEmployee(r.data.data)).catch(() => {}),
      api.get(`/employees/${employeeId}/vitals`).then(r => {
        const v = r.data.data
        setVitals(v)
        setForm({ height: v.height, weight: v.weight, bloodPressure: v.bloodPressure, bloodSugar: v.bloodSugar, bloodGroup: v.bloodGroup })
      }).catch(() => {})
    ]).finally(() => setLoading(false))
  }, [employeeId])

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      const r = await api.post(`/employees/${employeeId}/vitals`, {
        ...form, height: +form.height, weight: +form.weight, bloodSugar: +form.bloodSugar
      })
      setVitals(r.data.data)
      toast('Vitals saved successfully')
    } catch (err) {
      toast(err.response?.data?.message || 'Failed', 'error')
    }
  }

  if (loading) return <Loading text="Loading vitals..." />

  return (
    <div className="fade-in">
      <Link to="/client/employees" style={{ color: 'var(--primary)', fontSize: 14, fontWeight: 500, textDecoration: 'none', marginBottom: 16, display: 'inline-block' }}>← Back to Employees</Link>
      <div className="page-hdr">
        <h1>Health Vitals</h1>
        <p>{employee ? `${employee.fullName} · ${employee.employeeCode} · ${employee.email}` : `Employee #${employeeId}`}</p>
      </div>

      <div className="flex" style={{ gap: 24, alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: '0 0 400px' }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>🩺 {vitals ? 'Update Vitals' : 'Record Vitals'}</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Height (cm)</label>
              <input type="number" step="0.1" placeholder="e.g. 175" value={form.height} onChange={e => setForm({...form, height: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Weight (kg)</label>
              <input type="number" step="0.1" placeholder="e.g. 72" value={form.weight} onChange={e => setForm({...form, weight: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Blood Pressure</label>
              <input placeholder="e.g. 120/80" value={form.bloodPressure} onChange={e => setForm({...form, bloodPressure: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Blood Sugar (mg/dL)</label>
              <input type="number" step="0.1" placeholder="e.g. 95" value={form.bloodSugar} onChange={e => setForm({...form, bloodSugar: e.target.value})} required />
            </div>
            <div className="form-group">
              <label>Blood Group</label>
              <select value={form.bloodGroup} onChange={e => setForm({...form, bloodGroup: e.target.value})} required>
                <option value="">Select...</option>
                {BLOOD_GROUPS.map(bg => <option key={bg} value={bg}>{BG_LABELS[bg]}</option>)}
              </select>
            </div>
            <button type="submit" className="btn-block">{vitals ? 'Update Vitals' : 'Save Vitals'}</button>
          </form>
        </div>

        <div style={{ flex: 1 }}>
          {vitals ? (
            <div className="card">
              <div className="card-header">
                <h2>📊 Current Health Profile</h2>
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
                  <div className="v-value">{BG_LABELS[vitals.bloodGroup]}</div>
                </div>
              </div>
            </div>
          ) : (
            <div className="card">
              <div className="card-header">
                <h2>ℹ️ No Records</h2>
              </div>
              <p style={{ color: 'var(--gray-500)', fontSize: 14 }}>No health vitals recorded yet. Use the form to add vitals for this employee.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
