import { Link } from 'react-router-dom'

const features = [
  { icon: '🏢', title: 'Client Management', desc: 'Onboard corporate clients, set employee limits, and manage workforce health programs from a single dashboard.' },
  { icon: '👥', title: 'Employee Onboarding', desc: 'Bulk-add employees with unique codes. Track vitals, appointments, and health history for each team member.' },
  { icon: '🏥', title: 'Hospital Integration', desc: 'Partner hospitals create slots, define shifts, and manage bookings in real-time with full visibility.' },
  { icon: '📅', title: 'Smart Booking', desc: 'Employees self-book slots with one click. First-come-first-served — one slot, one person, zero conflicts.' },
  { icon: '📞', title: 'Voice Confirmation', desc: 'Powered by Bolna.ai — automated voice calls confirm every booking in the employee\'s preferred language.' },
  { icon: '💬', title: 'WhatsApp Notifications', desc: 'Twilio-powered WhatsApp messages deliver slot details, location, and timing instantly after booking.' },
]

const steps = [
  { num: '01', title: 'Register Client', desc: 'Admin onboards corporate clients with employee limits', color: '#2563eb' },
  { num: '02', title: 'Add Employees', desc: 'HR adds employees with codes, phone & health vitals', color: '#059669' },
  { num: '03', title: 'Create Slots', desc: 'Hospitals define available dates and shift timings', color: '#6366f1' },
  { num: '04', title: 'Self-Book', desc: 'Employees log in, pick a slot, and get confirmed instantly', color: '#d97706' },
]

const portals = [
  { to: '/admin/login', icon: '⚙️', title: 'Admin Portal', desc: 'Platform owner. Manage all clients, hospitals, and system-wide data.', bg: '#f3e8ff', color: '#6d28d9', top: '#7c3aed' },
  { to: '/client/login', icon: '🏢', title: 'Client Portal', desc: 'Corporate companies. Manage employees, record vitals, and book appointments.', bg: '#eff6ff', color: '#2563eb', top: '#2563eb' },
  { to: '/hospital/login', icon: '🏥', title: 'Hospital Portal', desc: 'Partner hospitals. Manage appointment slots and view bookings in real-time.', bg: '#ecfdf5', color: '#059669', top: '#059669' },
  { to: '/employee/login', icon: '👤', title: 'Employee Portal', desc: 'Sign in with your email + code to book appointments and manage your health.', bg: '#fef3c7', color: '#92400e', top: '#d97706' },
  { to: '/smart-split', icon: '📄', title: 'Smart PDF Split', desc: 'Upload multi-page medical PDFs. Auto-detect and split into lab, eye, and chest reports using AI.', bg: '#fce7f3', color: '#be185d', top: '#be185d' },
  { to: '/analyze', icon: '🔬', title: 'Medical Analyzer', desc: 'Upload medical reports. Extract values, compare with reference ranges, and highlight HIGH / LOW / NORMAL results.', bg: '#f0fdf4', color: '#16a34a', top: '#22c55e' },
]

const testimonials = [
  { quote: 'CorpCare streamlined our annual health checkups for 2,000+ employees. The WhatsApp confirmation is a game-changer.', name: 'Rajesh Kumar', role: 'HR Director, TechCorp India', stars: 5 },
  { quote: 'Setup took 10 minutes. Our patients love the self-booking — no more phone tag for appointments.', name: 'Dr. Priya Sharma', role: 'Medical Director, Apollo Health', stars: 5 },
  { quote: 'The voice call confirmation eliminated no-shows completely. Integration was seamless.', name: 'Amit Patel', role: 'COO, MediAssist Services', stars: 5 },
]

export default function Landing() {
  return (
    <div className="landing">
      {/* NAV */}
      <nav className="landing-nav">
        <Link to="/" className="brand">Corp<span>Care</span></Link>
        <div className="nav-links">
          <Link to="/admin/login">Admin</Link>
          <Link to="/client/login">Client</Link>
          <Link to="/hospital/login">Hospital</Link>
          <Link to="/employee/login" className="login-btn">Employee Login</Link>
        </div>
      </nav>

      {/* HERO */}
      <section className="hero">
        <div className="hero-content">
          <div className="badge">
            <span className="pulse-dot" /> Trusted by 600+ enterprises
          </div>
          <h1>Corporate Health.<br/><span>Simplified.</span></h1>
          <p>
            Complete B2B health management platform — from employee onboarding and
            vitals tracking to slot booking, voice calls, and WhatsApp confirmations.
          </p>
          <div className="hero-buttons">
            <Link to="/client/login" className="btn-primary">Get Started →</Link>
            <Link to="/employee/login" className="btn-outline">Employee Login</Link>
          </div>
          <div className="hero-metrics">
            <div className="hm-item">
              <span className="hm-num">10K+</span>
              <span className="hm-lbl">Bookings</span>
            </div>
            <div className="hm-divider" />
            <div className="hm-item">
              <span className="hm-num">600+</span>
              <span className="hm-lbl">Enterprises</span>
            </div>
            <div className="hm-divider" />
            <div className="hm-item">
              <span className="hm-num">4.9★</span>
              <span className="hm-lbl">Rating</span>
            </div>
          </div>
        </div>
        <div className="hero-visual">
          <div className="hero-illustration">
            <div className="h-card h-card-1">
              <div className="h-card-icon">🏢</div>
              <div><strong>Acme Corp</strong><span>128 employees</span></div>
              <span className="h-badge">Active</span>
            </div>
            <div className="h-card h-card-2">
              <div className="h-card-icon">🏥</div>
              <div><strong>Apollo Hospital</strong><span>Slots available</span></div>
              <span className="h-badge h-badge-green">8 AM - 4 PM</span>
            </div>
            <div className="h-card h-card-3">
              <div className="h-card-icon">👤</div>
              <div><strong>Rohit Sharma</strong><span>Booking confirmed</span></div>
              <span className="h-badge h-badge-blue">✓ WhatsApp</span>
            </div>
            <div className="h-glow" />
          </div>
        </div>
      </section>

      {/* FEATURES */}
      <section className="section">
        <div className="section-hdr">
          <span className="section-tag">Platform Features</span>
          <h2>Everything you need to manage<br/>employee health at scale</h2>
          <p>End-to-end solution for corporate health checkups — from onboarding to confirmation.</p>
        </div>
        <div className="features-grid">
          {features.map((f, i) => (
            <div key={i} className="feature-card">
              <div className="f-icon">{f.icon}</div>
              <h3>{f.title}</h3>
              <p>{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* STATS */}
      <section className="section stats-section">
        <div className="stats-container">
          <div className="stat-block">
            <span className="stat-num">10,000+</span>
            <span className="stat-label">Appointments Booked</span>
          </div>
          <div className="stat-block">
            <span className="stat-num">600+</span>
            <span className="stat-label">Corporate Clients</span>
          </div>
          <div className="stat-block">
            <span className="stat-num">50+</span>
            <span className="stat-label">Partner Hospitals</span>
          </div>
          <div className="stat-block">
            <span className="stat-num">99.9%</span>
            <span className="stat-label">Uptime</span>
          </div>
        </div>
      </section>

      {/* HOW IT WORKS */}
      <section className="section">
        <div className="section-hdr">
          <span className="section-tag">How It Works</span>
          <h2>Four simple steps to <span className="gradient-text">complete health management</span></h2>
          <p>From onboarding to appointment — the entire workflow in one platform.</p>
        </div>
        <div className="steps-grid">
          {steps.map((s, i) => (
            <div key={i} className="step-card">
              <div className="step-num-wrap" style={{ background: s.color }}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  {i === 0 && <><rect x="3" y="3" width="18" height="18" rx="2" /><line x1="12" y1="8" x2="12" y2="16" /><line x1="8" y1="12" x2="16" y2="12" /></>}
                  {i === 1 && <><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></>}
                  {i === 2 && <><rect x="3" y="4" width="18" height="18" rx="2" /><line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2" x2="8" y2="6" /><line x1="3" y1="10" x2="21" y2="10" /></>}
                  {i === 3 && <><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" /></>}
                </svg>
              </div>
              <h3>{s.title}</h3>
              <p>{s.desc}</p>
              <span className="step-arrow">→</span>
            </div>
          ))}
        </div>
      </section>

      {/* INTEGRATIONS */}
      <section className="section integrations-section">
        <div className="section-hdr">
          <span className="section-tag">Powered By</span>
          <h2>Enterprise-grade technology stack</h2>
        </div>
        <div className="integrations-grid">
          <div className="integration-card">
            <div className="int-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2"><path d="M22 4a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h1l2-3h14l2 3h1z"/></svg>
            </div>
            <h4>Twilio</h4>
            <p>WhatsApp Business API for instant booking confirmations</p>
          </div>
          <div className="integration-card">
            <div className="int-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#6366f1" strokeWidth="2"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
            </div>
            <h4>Bolna.ai</h4>
            <p>AI-powered voice calls for automated appointment confirmations</p>
          </div>
          <div className="integration-card">
            <div className="int-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#059669" strokeWidth="2"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>
            </div>
            <h4>MySQL / H2</h4>
            <p>Dual database support — H2 for dev, MySQL for production</p>
          </div>
          <div className="integration-card">
            <div className="int-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#d97706" strokeWidth="2"><rect x="2" y="2" width="20" height="8" rx="2" ry="2"/><rect x="2" y="14" width="20" height="8" rx="2" ry="2"/><line x1="6" y1="6" x2="6.01" y2="6"/><line x1="6" y1="18" x2="6.01" y2="18"/></svg>
            </div>
            <h4>Render + Vercel</h4>
            <p>Cloud-native deployment with auto-scaling and zero downtime</p>
          </div>
        </div>
      </section>

      {/* TESTIMONIALS */}
      <section className="section">
        <div className="section-hdr">
          <span className="section-tag">Testimonials</span>
          <h2>Trusted by healthcare leaders</h2>
        </div>
        <div className="testimonials-grid">
          {testimonials.map((t, i) => (
            <div key={i} className="testimonial-card">
              <div className="stars">{'★'.repeat(t.stars)}</div>
              <p className="t-quote">"{t.quote}"</p>
              <div className="t-author">
                <div className="t-avatar">{t.name.charAt(0)}</div>
                <div>
                  <div className="t-name">{t.name}</div>
                  <div className="t-role">{t.role}</div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* PORTALS */}
      <section className="section" style={{ paddingBottom: 0 }}>
        <div className="section-hdr">
          <span className="section-tag">Access Portals</span>
          <h2>Choose your portal</h2>
        </div>
      </section>
      <section className="section portals-section">
        {portals.map((p, i) => (
            <Link key={i} to={p.to} className="portal-card" style={{ borderTop: `4px solid ${p.top}` }}>
            <div className="p-icon" style={{ background: p.bg }}><span>{p.icon}</span></div>
            <h3>{p.title}</h3>
            <p>{p.desc}</p>
            <span className="p-tag" style={{ background: p.bg, color: p.color }}>Enter Portal →</span>
          </Link>
        ))}
      </section>

      {/* CTA */}
      <section className="cta-section">
        <div className="cta-content">
          <h2>Ready to simplify employee health management?</h2>
          <p>Join 600+ enterprises managing health checkups at scale.</p>
          <div className="cta-buttons">
            <Link to="/client/login" className="btn-primary btn-cta">Get Started Free →</Link>
            <Link to="/employee/login" className="btn-outline btn-cta-outline">Employee Login</Link>
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="footer">
        <div className="footer-grid">
          <div className="footer-brand">
            <Link to="/" className="brand" style={{ fontSize: 20 }}>Corp<span>Care</span></Link>
            <p>Corporate Employee Health Management Platform. Simplifying health checkups for enterprises since 2024.</p>
          </div>
          <div className="footer-col">
            <h4>Platform</h4>
            <Link to="/client/login">Client Portal</Link>
            <Link to="/hospital/login">Hospital Portal</Link>
            <Link to="/employee/login">Employee Portal</Link>
            <Link to="/admin/login">Admin</Link>
          </div>
          <div className="footer-col">
            <h4>Company</h4>
            <a href="#about">About Us</a>
            <a href="#careers">Careers</a>
            <a href="#privacy">Privacy Policy</a>
            <a href="#terms">Terms of Service</a>
          </div>
          <div className="footer-col">
            <h4>Support</h4>
            <a href="mailto:support@corpcare.com">support@corpcare.com</a>
            <a href="#docs">Documentation</a>
            <a href="#status">System Status</a>
            <a href="#contact">Contact Us</a>
          </div>
        </div>
        <div className="footer-bottom">
          <span>© {new Date().getFullYear()} CorpCare. All rights reserved.</span>
          <span>Made with ❤️ for enterprise health</span>
        </div>
      </footer>
    </div>
  )
}
