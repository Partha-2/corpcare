import { useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'

const STATUS_COLORS = {
  HIGH: { bg: '#fef2f2', text: '#dc2626', dot: '#ef4444' },
  LOW: { bg: '#eff6ff', text: '#2563eb', dot: '#3b82f6' },
  NORMAL: { bg: '#f0fdf4', text: '#16a34a', dot: '#22c55e' },
  'N/A': { bg: '#f9fafb', text: '#6b7280', dot: '#9ca3af' },
}

export default function MedicalAnalyzer() {
  const [file, setFile] = useState(null)
  const [patientId, setPatientId] = useState('')
  const [name, setName] = useState('')
  const [gender, setGender] = useState('MALE')
  const [age, setAge] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')

  const baseURL = import.meta.env.VITE_API_URL || '/api'

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!file) return
    setLoading(true)
    setError('')
    setResult(null)

    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('patientId', patientId)
      formData.append('name', name)
      formData.append('gender', gender)
      formData.append('age', age)
      const r = await axios.post(`${baseURL}/patient/analyze`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      setResult(r.data.data)
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Analysis failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="landing">
      <nav className="landing-nav">
        <Link to="/" className="brand">Corp<span>Care</span></Link>
        <div className="nav-links">
          <Link to="/">Home</Link>
          <Link to="/smart-split">Smart Split</Link>
        </div>
      </nav>

      <section className="hero" style={{ paddingBottom: 40 }}>
        <div className="hero-content" style={{ maxWidth: 700, margin: '0 auto', textAlign: 'center' }}>
          <div className="badge">
            <span className="pulse-dot" /> Medical PDF Analyzer
          </div>
          <h1>Upload a Medical PDF.<br/><span>Get Instant Analysis.</span></h1>
          <p>
            Upload a medical report (blood, ECG, urine, X-ray, thyroid). Our system extracts values,
            compares against reference ranges, and highlights HIGH / LOW / NORMAL results.
          </p>

          <form onSubmit={handleSubmit} style={{ marginTop: 32, textAlign: 'left' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 14, maxWidth: 500, margin: '0 auto' }}>
              <input type="file" accept=".pdf" onChange={e => setFile(e.target.files[0])}
                style={{ padding: '10px 14px', border: '2px dashed var(--border)', borderRadius: 8,
                  background: 'var(--bg-secondary)', fontSize: 14 }} required />
              {file && <p style={{ fontSize: 13, color: 'var(--gray-500)', marginTop: -8 }}>{file.name}</p>}

              <div style={{ display: 'flex', gap: 12 }}>
                <input type="text" placeholder="Patient ID (e.g. 68382)" value={patientId}
                  onChange={e => setPatientId(e.target.value)}
                  style={{ flex: 1, padding: '10px 14px', border: '1px solid var(--border)',
                    borderRadius: 8, fontSize: 14 }} required />
                <input type="text" placeholder="Full Name" value={name}
                  onChange={e => setName(e.target.value)}
                  style={{ flex: 2, padding: '10px 14px', border: '1px solid var(--border)',
                    borderRadius: 8, fontSize: 14 }} required />
              </div>

              <div style={{ display: 'flex', gap: 12 }}>
                <select value={gender} onChange={e => setGender(e.target.value)}
                  style={{ flex: 1, padding: '10px 14px', border: '1px solid var(--border)',
                    borderRadius: 8, fontSize: 14, background: 'var(--bg)' }}>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
                <input type="number" placeholder="Age (1-120)" value={age}
                  onChange={e => setAge(e.target.value)} min={1} max={120}
                  style={{ flex: 1, padding: '10px 14px', border: '1px solid var(--border)',
                    borderRadius: 8, fontSize: 14 }} required />
              </div>

              <button type="submit" className="btn-primary" style={{ padding: '12px 28px', fontSize: 16 }}
                disabled={loading || !file}>
                {loading ? '🔄 Analyzing...' : '🔬 Analyze Report'}
              </button>
            </div>
          </form>

          {error && <div className="alert alert-error" style={{ marginTop: 16 }}>{error}</div>}
        </div>
      </section>

      {result && (
        <section className="section" style={{ paddingTop: 0 }}>
          {/* Report header */}
          <div className="section-hdr">
            <span className="section-tag">Analysis Result</span>
            <h2>{result.patientName} — {result.reportType} Report</h2>
          </div>

          <div className="card" style={{ marginBottom: 20 }}>
            <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap', alignItems: 'center' }}>
              <div><strong>Patient ID:</strong> {result.patientId}</div>
              <div><strong>Age / Gender:</strong> {result.age} / {result.gender}</div>
              <div><strong>Report:</strong> {result.reportType}</div>
              <div><strong>Date:</strong> {result.reportDate}</div>
              <div><strong>Source:</strong> {result.sourceType}</div>
              <div className={`status-badge ${result.highCount > (result.lowCount + result.normalCount) / 2 ? 'error' : result.lowCount > 0 ? 'warn' : 'ok'}`}>
                {result.highCount} ↑ {result.lowCount} ↓ {result.normalCount} ✓
              </div>
            </div>
            {result.diagnosis && (
              <div style={{ marginTop: 12, padding: '8px 14px', background: '#f0f9ff', borderRadius: 6, fontSize: 14 }}>
                <strong>Diagnosis:</strong> {result.diagnosis}
              </div>
            )}
          </div>

          {/* Parameters table */}
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 14 }}>
              <thead>
                <tr style={{ background: 'var(--bg-secondary)' }}>
                  <th style={thStyle}>Parameter</th>
                  <th style={thStyle}>Value</th>
                  <th style={thStyle}>Unit</th>
                  <th style={thStyle}>Reference Range</th>
                  <th style={thStyle}>Status</th>
                  <th style={thStyle}>Category</th>
                </tr>
              </thead>
              <tbody>
                {result.parameters?.map((p, i) => {
                  const sc = STATUS_COLORS[p.status] || STATUS_COLORS['N/A']
                  return (
                    <tr key={i} style={{ borderBottom: '1px solid var(--border)' }}>
                      <td style={tdStyle}><strong>{p.name}</strong></td>
                      <td style={tdStyle}>{p.value}</td>
                      <td style={tdStyle}>{p.unit}</td>
                      <td style={tdStyle}>{p.referenceRange}</td>
                      <td style={tdStyle}>
                        <span style={{
                          display: 'inline-flex', alignItems: 'center', gap: 6,
                          padding: '3px 12px', borderRadius: 20, fontSize: 13, fontWeight: 600,
                          background: sc.bg, color: sc.text
                        }}>
                          <span style={{ width: 8, height: 8, borderRadius: '50%', background: sc.dot }} />
                          {p.status}
                        </span>
                      </td>
                      <td style={{ ...tdStyle, fontSize: 12, color: 'var(--gray-500)' }}>{p.category}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>

          <div style={{ textAlign: 'center', marginTop: 24, display: 'flex', gap: 12, justifyContent: 'center' }}>
            {result.viewUrl && (
              <a href={result.viewUrl} target="_blank" rel="noreferrer"
                className="btn" style={{ padding: '8px 24px' }}>
                👁️ View PDF
              </a>
            )}
            {result.downloadUrl && (
              <a href={result.downloadUrl} target="_blank" rel="noreferrer"
                className="btn" style={{ padding: '8px 24px', background: '#2563eb', color: '#fff', border: 'none' }}>
                ⬇️ Download PDF
              </a>
            )}
            <button className="btn" style={{ padding: '8px 24px' }}
              onClick={() => { setFile(null); setResult(null); setPatientId(''); setName(''); setAge('') }}>
              🔄 Analyze Another
            </button>
          </div>
        </section>
      )}

      <footer className="footer">
        <div className="footer-bottom">
          <span>&copy; {new Date().getFullYear()} CorpCare. Medical PDF Analyzer</span>
        </div>
      </footer>
    </div>
  )
}

const thStyle = { padding: '10px 14px', textAlign: 'left', fontWeight: 600, fontSize: 13, textTransform: 'uppercase', letterSpacing: '0.05em' }
const tdStyle = { padding: '10px 14px' }
