import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../../api/axios'

export default function EmployeeLogin() {
  const [form, setForm] = useState({ email: '', employeeCode: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const r = await api.post('/employees/verify', form)
      sessionStorage.setItem('employee', JSON.stringify(r.data.data))
      navigate('/employee/dashboard')
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'linear-gradient(135deg, #0f172a, #1e3a5f)', position: 'relative' }}>
      <Link to="/" style={{ position: 'absolute', top: 24, left: 24, color: 'rgba(255,255,255,0.7)', textDecoration: 'none', fontSize: 14, fontWeight: 500 }}>
        ← Back to Home
      </Link>
      <div className="card" style={{ width: 400, padding: 40 }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ fontSize: 40, marginBottom: 8 }}>👤</div>
          <h2 style={{ fontSize: 22, fontWeight: 800, marginBottom: 4 }}>Employee Sign In</h2>
          <p style={{ color: 'var(--gray-500)', fontSize: 14 }}>Use the credentials provided by your employer</p>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email Address</label>
            <input type="email" required placeholder="rohit@vkohli.fit" value={form.email} onChange={e => setForm({...form, email: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Employee Code</label>
            <input required placeholder="VK001" value={form.employeeCode} onChange={e => setForm({...form, employeeCode: e.target.value})} />
          </div>
          <button type="submit" className="btn-block" disabled={loading}>
            {loading ? 'Verifying...' : 'Sign In →'}
          </button>
        </form>
      </div>
    </div>
  )
}
