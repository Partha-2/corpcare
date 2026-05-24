import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      api.get('/clients'),
      api.get('/hospitals')
    ]).then(([c, h]) => {
      const clients = c.data.data
      const hospitals = h.data.data
      let employees = 0
      let slots = 0

      const promises = []
      clients.forEach(cl => {
        promises.push(
          api.get(`/clients/${cl.id}/employees`).then(r => { employees += r.data.data.length })
        )
      })
      hospitals.forEach(ho => {
        promises.push(
          api.get(`/hospitals/${ho.id}/slots`).then(r => { slots += r.data.data.length })
        )
      })

      Promise.all(promises).then(() => {
        setStats({ clients: clients.length, hospitals: hospitals.length, employees, slots })
        setLoading(false)
      })
    })
  }, [])

  if (loading) return <Loading text="Loading dashboard..." />

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <h1>Admin Dashboard</h1>
        <p>CorpCare — Platform Overview</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="s-icon blue">🏢</div>
          <div className="s-info">
            <div className="s-number">{stats.clients}</div>
            <div className="s-label">Corporate Clients</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="s-icon green">👥</div>
          <div className="s-info">
            <div className="s-number">{stats.employees}</div>
            <div className="s-label">Total Employees</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="s-icon purple">🏥</div>
          <div className="s-info">
            <div className="s-number">{stats.hospitals}</div>
            <div className="s-label">Partner Hospitals</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="s-icon red">📅</div>
          <div className="s-info">
            <div className="s-number">{stats.slots}</div>
            <div className="s-label">Total Slots</div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h2>⚡ Quick Actions</h2>
        </div>
        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
          <Link to="/admin/clients" className="btn">➕ New Client</Link>
          <Link to="/admin/hospitals" className="btn btn-green">🏥 New Hospital</Link>
          <Link to="/admin/clients" className="btn btn-ghost">📋 All Clients</Link>
          <Link to="/admin/hospitals" className="btn btn-ghost">📋 All Hospitals</Link>
        </div>
      </div>
    </div>
  )
}
