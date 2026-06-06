import { useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'

const CATEGORY_LABELS = {
  lab: { icon: '🧪', title: 'Lab Report', color: '#2563eb' },
  eye: { icon: '👁️', title: 'Eye Report', color: '#059669' },
  chest: { icon: '🫁', title: 'Chest Report', color: '#6d28d9' },
}

export default function SmartSplit() {
  const [file, setFile] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')

  const baseURL = import.meta.env.VITE_API_URL || '/api'

  const handleUpload = async (e) => {
    e.preventDefault()
    if (!file) return
    setUploading(true)
    setError('')
    setResult(null)

    try {
      const formData = new FormData()
      formData.append('file', file)
      const r = await axios.post(`${baseURL}/pdf/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      setResult(r.data.data)
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Upload failed')
    } finally {
      setUploading(false)
    }
  }

  const getFileUrl = (url) => {
    if (!url) return '#'
    return url.startsWith('http') ? url : `${baseURL}/pdf/${url.split('/').pop()}`
  }

  return (
    <div className="landing">
      <nav className="landing-nav">
        <Link to="/" className="brand">Corp<span>Care</span></Link>
        <div className="nav-links">
          <Link to="/">Home</Link>
        </div>
      </nav>

      <section className="hero" style={{ paddingBottom: 40 }}>
        <div className="hero-content" style={{ maxWidth: 700, margin: '0 auto', textAlign: 'center' }}>
          <div className="badge">
            <span className="pulse-dot" /> Smart PDF Splitter
          </div>
          <h1>Upload a Medical PDF.<br/><span>Get Smartly Split Reports.</span></h1>
          <p>
            Upload a multi-page medical PDF. Our AI-powered system automatically detects
            and separates lab reports, eye exams, and chest X-ray pages into individual PDFs.
          </p>

          <form onSubmit={handleUpload} style={{ marginTop: 32 }}>
            <div style={{
              display: 'flex', gap: 12, alignItems: 'center',
              justifyContent: 'center', flexWrap: 'wrap'
            }}>
              <input
                type="file"
                accept=".pdf"
                onChange={e => setFile(e.target.files[0])}
                style={{
                  padding: '10px 16px', border: '2px dashed var(--border)',
                  borderRadius: 8, background: 'var(--bg-secondary)', flex: 1,
                  minWidth: 250, fontSize: 14
                }}
                required
              />
              <button type="submit" className="btn-primary" style={{ padding: '10px 28px' }}
                disabled={uploading}>
                {uploading ? '🔄 Splitting...' : '🚀 Upload & Split'}
              </button>
            </div>
            {file && <p style={{ fontSize: 13, color: 'var(--gray-500)', marginTop: 8 }}>{file.name}</p>}
          </form>

          {error && (
            <div className="alert alert-error" style={{ marginTop: 16 }}>{error}</div>
          )}
        </div>
      </section>

      {result && (
        <section className="section" style={{ paddingTop: 0 }}>
          <div className="section-hdr">
            <span className="section-tag">Split Results</span>
            <h2>Your PDF has been split into categories</h2>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 20 }}>
            {result.splitResults?.map((sr, i) => {
              const meta = CATEGORY_LABELS[sr.category] || { icon: '📄', title: sr.category, color: '#6b7280' }
              return (
                <div key={i} className="card" style={{ borderTop: `4px solid ${meta.color}` }}>
                  <div style={{ fontSize: 32, marginBottom: 8 }}>{meta.icon}</div>
                  <h3 style={{ fontSize: 18, fontWeight: 700 }}>{meta.title}</h3>
                  <div style={{ fontSize: 14, color: 'var(--gray-500)', marginBottom: 12 }}>
                    {sr.found ? `${sr.pageCount} page(s)` : 'No pages detected'}
                  </div>
                  {sr.found && (
                    <div style={{ display: 'flex', gap: 8 }}>
                      <a href={getFileUrl(sr.viewUrl)} target="_blank" rel="noreferrer"
                        className="btn" style={{ fontSize: 13, padding: '6px 16px' }}>
                        👁️ View
                      </a>
                      <a href={getFileUrl(sr.downloadUrl)} target="_blank" rel="noreferrer"
                        className="btn" style={{ fontSize: 13, padding: '6px 16px', background: meta.color, color: '#fff', border: 'none' }}>
                        ⬇️ Download
                      </a>
                    </div>
                  )}
                </div>
              )
            })}
          </div>

          {result.imageResults?.length > 0 && (
            <>
              <div className="section-hdr" style={{ marginTop: 32 }}>
                <span className="section-tag">Image Analysis</span>
                <h2>OCR-processed image pages</h2>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 16 }}>
                {result.imageResults.map((ir, i) => (
                  <div key={i} className="card">
                    <div style={{ fontSize: 24, marginBottom: 4 }}>🖼️</div>
                    <h4 style={{ fontSize: 15, fontWeight: 700 }}>{ir.type} — Page {ir.pageNumber}</h4>
                    <div style={{ fontSize: 13, color: 'var(--gray-500)', marginBottom: 8 }}>
                      Confidence: {ir.confidence} · OCR: {ir.ocrEngine} · {ir.valuesExtracted} value(s)
                    </div>
                    {ir.values && Object.keys(ir.values).length > 0 && (
                      <div style={{ fontSize: 13 }}>
                        {Object.entries(ir.values).slice(0, 5).map(([k, v]) => (
                          <div key={k} style={{ marginBottom: 2 }}><strong>{k}:</strong> {v}</div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </>
          )}

          <div style={{ textAlign: 'center', marginTop: 32 }}>
            <button className="btn" onClick={() => { setFile(null); setResult(null) }}>
              🔄 Split Another PDF
            </button>
          </div>
        </section>
      )}

      <footer className="footer">
        <div className="footer-bottom">
          <span>© {new Date().getFullYear()} CorpCare. Smart PDF Splitter</span>
        </div>
      </footer>
    </div>
  )
}
