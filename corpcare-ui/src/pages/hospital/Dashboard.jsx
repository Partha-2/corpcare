import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'

export default function HospitalDashboard() {
  const [hospitals, setHospitals] = useState([])
  const [loading, setLoading] = useState(true)
  const [hospital, setHospital] = useState(null)
  const [stats, setStats] = useState({ total: 0, available: 0, booked: 0 })

  const loadStats = (id) => {
    api.get(`/hospitals/${id}/slots`).then(s => {
      const slots = s.data.data
      setStats({
        total: slots.length,
        available: slots.filter(sl => !sl.isBooked).length,
        booked: slots.filter(sl => sl.isBooked).length
      })
    })
  }

  useEffect(() => {
    api.get('/hospitals').then(r => {
      setHospitals(r.data.data)
      if (r.data.data.length > 0) {
        setHospital(r.data.data[0])
        loadStats(r.data.data[0].id)
      }
      setLoading(false)
    })
  }, [])

  const handleHospitalChange = (id) => {
    const h = hospitals.find(x => x.id === +id)
    setHospital(h)
    if (h) loadStats(h.id)
  }

  const pct = stats.total > 0 ? Math.round((stats.booked / stats.total) * 100) : 0

  if (loading) return <Loading text="Loading hospital..." />

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <div className="flex-between" style={{ alignItems: 'flex-start' }}>
          <div>
            <h1>{hospital ? hospital.hospitalName : 'Hospital Portal'}</h1>
            <p>{hospital ? `${hospital.city} · ${hospital.contactEmail}` : 'Manage your appointments and slots'}</p>
          </div>
          {hospitals.length > 1 && (
            <select value={hospital?.id || ''} onChange={e => handleHospitalChange(e.target.value)} style={{ padding: '8px 12px', border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-sm)', fontSize: 14, minWidth: 200 }}>
              {hospitals.map(h => <option key={h.id} value={h.id}>{h.hospitalName}</option>)}
            </select>
          )}
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="s-icon blue">📅</div>
          <div className="s-info">
            <div className="s-number">{stats.total}</div>
            <div className="s-label">Total Slots</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="s-icon green">✅</div>
          <div className="s-info">
            <div className="s-number">{stats.available}</div>
            <div className="s-label">Available</div>
          </div>
          <span className="s-trend up">{stats.available > 0 ? 'Open' : 'Full'}</span>
        </div>
        <div className="stat-card">
          <div className="s-icon red">📋</div>
          <div className="s-info">
            <div className="s-number">{stats.booked}</div>
            <div className="s-label">Booked</div>
          </div>
          <span className="s-trend down">{stats.booked > 0 ? `${pct}%` : 'None'}</span>
        </div>
      </div>

      <div className="flex" style={{ gap: 24 }}>
        <div className="card" style={{ flex: 1 }}>
          <div className="card-header">
            <h2>📊 Slot Utilization</h2>
          </div>
          <div style={{ height: 10, background: 'var(--gray-100)', borderRadius: 5, overflow: 'hidden' }}>
            <div style={{ height: '100%', width: `${pct}%`, background: 'linear-gradient(90deg, var(--primary), #7c3aed)', borderRadius: 5, transition: 'width 0.5s' }} />
          </div>
          <div className="flex-between mt-16">
            <span style={{ fontSize: 13, color: 'var(--gray-500)' }}>✅ {stats.available} Available</span>
            <span style={{ fontSize: 13, color: 'var(--gray-500)' }}>📋 {stats.booked} Booked ({pct}%)</span>
          </div>
        </div>

        <div className="card" style={{ flex: 1 }}>
          <div className="card-header">
            <h2>⚡ Quick Actions</h2>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <Link to="/hospital/slots" className="btn btn-block">📅 Manage Slots</Link>
            <Link to="/hospital/appointments" className="btn btn-block btn-ghost">📋 View Appointments</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
