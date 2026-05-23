import { Link, useNavigate } from 'react-router-dom'

export default function EmployeeNavbar() {
  const navigate = useNavigate()
  const emp = JSON.parse(sessionStorage.getItem('employee') || '{}')

  const handleLogout = () => {
    sessionStorage.removeItem('employee')
    navigate('/employee/login')
  }

  const isActive = (path) => window.location.pathname === path ? 'active' : ''

  return (
    <nav className="portal-nav">
      <Link to="/" className="brand">Corp<span>Care</span></Link>
      <span className="nav-type" style={{ background: '#fef3c7', color: '#92400e' }}>EMPLOYEE</span>
      <Link to="/employee/dashboard" className={isActive('/employee/dashboard')}>Dashboard</Link>
      <Link to="/employee/vitals" className={isActive('/employee/vitals')}>My Vitals</Link>
      <Link to="/employee/book" className={isActive('/employee/book')}>Book</Link>
      <Link to="/employee/appointments" className={isActive('/employee/appointments')}>Appointments</Link>
      <div style={{ flex: 1 }} />
      <span style={{ fontSize: 13, color: 'var(--gray-500)', marginRight: 12 }}>{emp.fullName}</span>
      <button className="btn btn-sm btn-ghost" onClick={handleLogout}>Logout</button>
    </nav>
  )
}
