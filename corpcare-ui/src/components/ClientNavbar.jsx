import { Link } from 'react-router-dom'

export default function ClientNavbar() {
  const isActive = (path) => {
    const current = window.location.pathname
    const match = current === path || (path !== '/client' && current.startsWith(path))
    return match ? 'active' : ''
  }

  return (
    <nav className="portal-nav">
      <Link to="/" className="brand">Corp<span>Care</span></Link>
      <span className="nav-type client">CLIENT</span>
      <Link to="/client" className={isActive('/client') + (isActive('/client') ? '' : '')}>Dashboard</Link>
      <Link to="/client/employees" className={isActive('/client/employees')}>Employees</Link>
      <Link to="/client/book" className={isActive('/client/book')}>Book</Link>
      <Link to="/client/appointments" className={isActive('/client/appointments')}>Appointments</Link>
    </nav>
  )
}
