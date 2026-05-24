import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'

export default function ClientDashboard() {
  const [stats, setStats] = useState({ clients: 0, employees: 0 })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/clients').then(c => {
      setStats(s => ({ ...s, clients: c.data.data.length }))
      if (c.data.data.length > 0) {
        api.get(`/clients/${c.data.data[0].id}/employees`).then(r => {
          setStats(s => ({ ...s, employees: r.data.data.length }))
          setLoading(false)
        })
      } else {
        setLoading(false)
      }
    })
  }, [])

  if (loading) return <Loading text="Loading dashboard..." />

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <h1>Client Dashboard</h1>
        <p>Manage your workforce health programs</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="s-icon blue">🏢</div>
          <div className="s-info">
            <div className="s-number">{stats.clients}</div>
            <div className="s-label">Companies</div>
          </div>
          <span className="s-trend up">Active</span>
        </div>
        <div className="stat-card">
          <div className="s-icon green">👥</div>
          <div className="s-info">
            <div className="s-number">{stats.employees}</div>
            <div className="s-label">Employees</div>
          </div>
          <span className="s-trend up">Enrolled</span>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h2>⚡ Quick Actions</h2>
        </div>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <Link to="/client/employees" className="btn">👥 Employees</Link>
          <Link to="/client/book" className="btn btn-green">📅 Book Appointment</Link>
          <Link to="/client/appointments" className="btn btn-ghost">📋 History</Link>
        </div>
      </div>
    </div>
  )
}
