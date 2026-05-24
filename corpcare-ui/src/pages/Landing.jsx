import { Link } from 'react-router-dom'

export default function Landing() {
  return (
    <div className="landing">
      <nav className="landing-nav">
        <Link to="/" className="brand">Corp<span>Care</span></Link>
        <div className="nav-links">
          <Link to="/admin/login">Admin</Link>
          <Link to="/client">Client</Link>
          <Link to="/hospital">Hospital</Link>
          <Link to="/employee/login" className="login-btn">Employee Login</Link>
        </div>
      </nav>

      <section className="hero">
        <div className="hero-content">
          <div className="badge">⚡ Trusted by 600+ enterprises</div>
          <h1>Corporate Health.<br/><span>Simplified.</span></h1>
          <p>
            Complete B2B health management — corporate clients onboard employees,
            hospitals manage slots, and employees book their own appointments.
          </p>
          <div className="hero-buttons">
            <Link to="/client" className="btn-primary">Get Started →</Link>
            <Link to="/employee/login" className="btn-outline">Employee Login</Link>
          </div>
        </div>

        <div className="hero-visual">
          <div className="hero-stats">
            <div className="hero-stat">
              <div className="num">4.9★</div>
              <div className="lbl">Employee Rating</div>
            </div>
            <div className="hero-stat">
              <div className="num">97%</div>
              <div className="lbl">Client Retention</div>
            </div>
            <div className="hero-stat">
              <div className="num">600+</div>
              <div className="lbl">Enterprises</div>
            </div>
            <div className="hero-stat">
              <div className="num">10L+</div>
              <div className="lbl">Employees</div>
            </div>
          </div>
          <div style={{ marginTop: 24, display: 'flex', gap: 12 }}>
            <div style={{ flex: 1, background: 'white', borderRadius: 12, padding: 16, boxShadow: '0 1px 3px rgba(0,0,0,0.06)' }}>
              <div style={{ fontSize: 12, color: 'var(--gray-500)', fontWeight: 600, marginBottom: 4 }}>EMPLOYEE APP</div>
              <div style={{ fontSize: 13, color: 'var(--gray-700)' }}>📱 Self-service bookings, vitals, and health records</div>
            </div>
            <div style={{ flex: 1, background: 'white', borderRadius: 12, padding: 16, boxShadow: '0 1px 3px rgba(0,0,0,0.06)' }}>
              <div style={{ fontSize: 12, color: 'var(--gray-500)', fontWeight: 600, marginBottom: 4 }}>EMPLOYER DASHBOARD</div>
              <div style={{ fontSize: 13, color: 'var(--gray-700)' }}>📊 Manage workforce health, compliance, and audits</div>
            </div>
          </div>
        </div>
      </section>

      <section style={{ padding: '0 24px 60px', maxWidth: 1200, margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: 48 }}>
          <h2 style={{ fontSize: 32, fontWeight: 800, color: 'var(--gray-900)', letterSpacing: '-0.5px' }}>How It Works</h2>
          <p style={{ color: 'var(--gray-500)', fontSize: 16, marginTop: 8 }}>Four simple steps to manage employee health checkups</p>
        </div>
        <div className="steps-grid">
          {[
            { step: '01', title: 'Register Client', desc: 'Admin onboards corporate clients into the platform', color: 'var(--primary)' },
            { step: '02', title: 'Add Employees', desc: 'Client HR adds employees with their details & vitals', color: 'var(--green)' },
            { step: '03', title: 'Create Slots', desc: 'Hospitals define available appointment time slots', color: 'var(--secondary)' },
            { step: '04', title: 'Self-Book', desc: 'Employees log in and book their preferred slot instantly', color: '#f59e0b' },
          ].map((item, i) => (
            <div key={i} style={{ textAlign: 'center', padding: '32px 20px' }}>
              <div style={{ width: 56, height: 56, borderRadius: '50%', background: item.color, color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 20, fontWeight: 800, margin: '0 auto 16px', boxShadow: `0 4px 14px ${item.color}33` }}>{item.step}</div>
              <h3 style={{ fontSize: 16, fontWeight: 700, color: 'var(--gray-900)', marginBottom: 6 }}>{item.title}</h3>
              <p style={{ fontSize: 14, color: 'var(--gray-500)', lineHeight: 1.6 }}>{item.desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section style={{ padding: '0 24px 80px', maxWidth: 1200, margin: '0 auto' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: 20 }}>
          <Link to="/admin/login" className="portal-card" style={{ borderTop: '4px solid #7c3aed' }}>
            <div className="icon">⚙️</div>
            <h3>Admin Portal</h3>
            <p>Platform owner. Manage all clients, hospitals, and system-wide data.</p>
            <div className="tag" style={{ background: '#f3e8ff', color: '#6d28d9' }}>Enter →</div>
          </Link>
          <Link to="/client" className="portal-card" style={{ borderTop: '4px solid var(--primary)' }}>
            <div className="icon">🏢</div>
            <h3>Client Portal</h3>
            <p>Corporate companies. Manage employees, record vitals, and book appointments.</p>
            <div className="tag" style={{ background: 'var(--primary-light)', color: 'var(--primary)' }}>Enter →</div>
          </Link>
          <Link to="/hospital" className="portal-card" style={{ borderTop: '4px solid var(--green)' }}>
            <div className="icon">🏥</div>
            <h3>Hospital Portal</h3>
            <p>Partner hospitals. Manage appointment slots and view bookings.</p>
            <div className="tag" style={{ background: 'var(--green-light)', color: 'var(--green)' }}>Enter →</div>
          </Link>
          <Link to="/employee/login" className="portal-card" style={{ borderTop: '4px solid #f59e0b' }}>
            <div className="icon">👤</div>
            <h3>Employee Portal</h3>
            <p>Individual employees. Sign in with your email + code to book and manage health.</p>
            <div className="tag" style={{ background: '#fef3c7', color: '#92400e' }}>Sign In →</div>
          </Link>
        </div>
      </section>

      <footer style={{ borderTop: '1px solid var(--gray-200)', padding: '32px 48px', textAlign: 'center', color: 'var(--gray-500)', fontSize: 13 }}>
        <div style={{ fontWeight: 700, color: 'var(--gray-700)', marginBottom: 4 }}>CorpCare</div>
        <div>Corporate Employee Health Management Platform</div>
        <div style={{ marginTop: 8 }}>© {new Date().getFullYear()} CorpCare. All rights reserved.</div>
      </footer>
    </div>
  )
}
