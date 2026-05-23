import { Link } from 'react-router-dom'

export default function AdminNavbar() {
  const isActive = (path) => window.location.pathname.startsWith(path) ? 'active' : ''
  return (
    <nav className="portal-nav dark">
      <Link to="/" className="brand">Corp<span>Care</span></Link>
      <span className="nav-type" style={{ background: 'rgba(255,255,255,0.15)', color: '#c7d2fe' }}>ADMIN</span>
      <Link to="/admin" className={isActive('/admin')}>Dashboard</Link>
      <Link to="/admin/clients" className={isActive('/admin/clients')}>Clients</Link>
      <Link to="/admin/hospitals" className={isActive('/admin/hospitals')}>Hospitals</Link>
    </nav>
  )
}
