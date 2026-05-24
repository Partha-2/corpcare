import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../../api/axios'

export default function AdminLogin() {
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const r = await api.post('/admin/verify', { password })
      if (r.data.data?.authenticated) {
        sessionStorage.setItem('admin_auth', 'true')
        navigate('/admin')
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'linear-gradient(135deg, #1e1b4b, #312e81)', position: 'relative' }}>
      <Link to="/" style={{ position: 'absolute', top: 24, left: 24, color: 'rgba(255,255,255,0.7)', textDecoration: 'none', fontSize: 14, fontWeight: 500 }}>
        ← Back to Home
      </Link>
      <div className="card" style={{ width: 380, padding: 40, textAlign: 'center' }}>
        <div style={{ fontSize: 40, marginBottom: 12 }}>⚙️</div>
        <h2 style={{ fontSize: 22, fontWeight: 800, marginBottom: 4 }}>Admin Login</h2>
        <p style={{ color: 'var(--gray-500)', fontSize: 14, marginBottom: 28 }}>Enter the admin password to continue</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              required
              placeholder="Enter admin password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              autoFocus
            />
          </div>
          <button type="submit" className="btn-block" disabled={loading}>
            {loading ? 'Verifying...' : 'Sign In →'}
          </button>
        </form>
      </div>
    </div>
  )
}
