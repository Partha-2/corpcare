import { Link } from 'react-router-dom'

export default function HospitalNavbar() {
  const isActive = (path) => {
    const current = window.location.pathname
    const match = current === path || (path !== '/hospital' && current.startsWith(path))
    return match ? 'active' : ''
  }

  return (
    <nav className="portal-nav">
      <Link to="/" className="brand">Corp<span>Care</span></Link>
      <span className="nav-type hospital">HOSPITAL</span>
      <Link to="/hospital" className={isActive('/hospital')}>Dashboard</Link>
      <Link to="/hospital/slots" className={isActive('/hospital/slots')}>Slots</Link>
      <Link to="/hospital/appointments" className={isActive('/hospital/appointments')}>Appointments</Link>
    </nav>
  )
}
