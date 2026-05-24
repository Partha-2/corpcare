import { Link, useNavigate } from 'react-router-dom'

export default function HospitalNavbar() {
  const navigate = useNavigate()
  const hospital = JSON.parse(sessionStorage.getItem('hospital') || '{}')

  const handleLogout = () => {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('hospital')
    navigate('/hospital/login')
  }

  const isActive = (path) => window.location.pathname === path || (path !== '/hospital' && window.location.pathname.startsWith(path)) ? 'active' : ''

  return (
    <nav className="portal-nav">
      <Link to="/" className="brand">Corp<span>Care</span></Link>
      <span className="nav-type" style={{ background: '#dbeafe', color: '#1e40af' }}>HOSPITAL</span>
      <Link to="/hospital" className={isActive('/hospital')}>Dashboard</Link>
      <Link to="/hospital/slots" className={isActive('/hospital/slots')}>Slots</Link>
      <Link to="/hospital/appointments" className={isActive('/hospital/appointments')}>Appointments</Link>
      <div style={{ flex: 1 }} />
      <span style={{ fontSize: 13, color: 'var(--gray-500)', marginRight: 12 }}>{hospital.hospitalName}</span>
      <button className="btn btn-sm btn-ghost" onClick={handleLogout}>Logout</button>
    </nav>
  )
}
