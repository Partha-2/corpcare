import { Link, useNavigate } from 'react-router-dom'

export default function ClientNavbar() {
  const navigate = useNavigate()
  const client = JSON.parse(sessionStorage.getItem('client') || '{}')

  const handleLogout = () => {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('client')
    navigate('/client/login')
  }

  const isActive = (path) => window.location.pathname === path || (path !== '/client' && window.location.pathname.startsWith(path)) ? 'active' : ''

  return (
    <nav className="portal-nav">
      <Link to="/" className="brand">Corp<span>Care</span></Link>
      <span className="nav-type" style={{ background: '#d1fae5', color: '#065f46' }}>CLIENT</span>
      <Link to="/client" className={isActive('/client')}>Dashboard</Link>
      <Link to="/client/employees" className={isActive('/client/employees')}>Employees</Link>
      <Link to="/client/book" className={isActive('/client/book')}>Book</Link>
      <Link to="/client/appointments" className={isActive('/client/appointments')}>Appointments</Link>
      <div style={{ flex: 1 }} />
      <span style={{ fontSize: 13, color: 'var(--gray-500)', marginRight: 12 }}>{client.companyName}</span>
      <button className="btn btn-sm btn-ghost" onClick={handleLogout}>Logout</button>
    </nav>
  )
}
