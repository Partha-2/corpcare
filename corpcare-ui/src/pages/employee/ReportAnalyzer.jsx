import { useState, useRef } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'
import { toast } from '../../components/Toast'

const STATUS_COLORS = {
  NORMAL: { bg: 'rgba(5,150,105,0.08)', badge: '#059669', label: 'Normal' },
  HIGH: { bg: 'rgba(220,38,38,0.08)', badge: '#dc2626', label: 'High' },
  LOW: { bg: 'rgba(37,99,235,0.08)', badge: '#2563eb', label: 'Low' },
  ABNORMAL: { bg: 'rgba(220,38,38,0.08)', badge: '#dc2626', label: 'Abnormal' },
  NOT_FOUND: { bg: 'transparent', badge: '#64748b', label: 'Not Found' }
}

export default function ReportAnalyzer() {
  const [file, setFile] = useState(null)
  const [dragOver, setDragOver] = useState(false)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const inputRef = useRef(null)

  const handleDrop = (e) => {
    e.preventDefault()
    setDragOver(false)
    const f = e.dataTransfer.files[0]
    if (f && f.type === 'application/pdf') {
      if (f.size > 10 * 1024 * 1024) { setError('File exceeds 10MB limit'); return }
      setFile(f); setError(''); setResult(null)
    } else { setError('Please drop a valid PDF file') }
  }

  const handleSelect = (e) => {
    const f = e.target.files[0]
    if (f) {
      if (f.size > 10 * 1024 * 1024) { setError('File exceeds 10MB limit'); return }
      setFile(f); setError(''); setResult(null)
    }
  }

  const handleUpload = async () => {
    if (!file) return
    setLoading(true); setError(''); setResult(null)
    try {
      const form = new FormData()
      form.append('file', file)
      const r = await api.post('/report-analyzer/analyze', form, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      setResult(r.data)
      if (r.data.alerts?.length > 0) {
        toast(`${r.data.alerts.length} parameter(s) out of normal range`, 'error')
      } else {
        toast('Report analyzed successfully', 'success')
      }
    } catch (err) {
      setError(err.response?.data?.error || err.response?.data?.message || 'Analysis failed')
    } finally { setLoading(false) }
  }

  const sc = (status) => STATUS_COLORS[status] || STATUS_COLORS.NOT_FOUND

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <Link to="/employee/dashboard" style={{ fontSize: 13, color: 'var(--primary)', textDecoration: 'none', display: 'block', marginBottom: 8 }}>
          ← Back to Dashboard
        </Link>
        <h1>🧪 Report Analyzer</h1>
        <p>Upload medical PDF — auto-detect vendor, extract parameters, flag anomalies</p>
      </div>

      {!result && (
        <div
          className="card"
          onDragOver={e => { e.preventDefault(); setDragOver(true) }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleDrop}
          style={{
            border: `2px dashed ${dragOver ? 'var(--primary)' : 'var(--surface-2)'}`,
            background: dragOver ? 'var(--accent-glow)' : 'var(--surface)',
            textAlign: 'center', padding: 48, cursor: 'pointer',
            transition: 'all 0.3s'
          }}
          onClick={() => inputRef.current?.click()}
        >
          <input ref={inputRef} type="file" accept=".pdf" onChange={handleSelect} style={{ display: 'none' }} />
          <div style={{ fontSize: 56, marginBottom: 12 }}>{file ? '📎' : '📄'}</div>
          <h3 style={{ fontSize: 18, fontWeight: 600, marginBottom: 8 }}>
            {file ? file.name : 'Drag & drop your medical report here'}
          </h3>
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>
            {file ? `${(file.size / 1024).toFixed(1)} KB` : 'or click to browse — PDF only, max 10MB'}
          </p>
          {file && (
            <div style={{ marginTop: 20, display: 'flex', gap: 12, justifyContent: 'center' }}>
              <button className="btn btn-green" onClick={e => { e.stopPropagation(); handleUpload() }} disabled={loading}>
                {loading ? 'Analyzing...' : '🔍 Analyze Report'}
              </button>
              <button className="btn btn-ghost" onClick={e => { e.stopPropagation(); setFile(null) }} disabled={loading}>Clear</button>
            </div>
          )}
          {loading && <div style={{ marginTop: 16 }}><Loading text="" /></div>}
        </div>
      )}

      {error && <div className="alert alert-error" style={{ marginTop: 16 }}>{error}</div>}

      {result && (
        <>
          {/* Classification Card */}
          <div className="card" style={{ marginTop: 16 }}>
            <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
              <div>
                <div style={{ fontSize: 12, color: 'var(--text-3)', marginBottom: 4 }}>🏥 Detected Vendor</div>
                <div style={{ fontSize: 16, fontWeight: 600 }}>{result.vendor || 'Unknown'}</div>
              </div>
              <div>
                <div style={{ fontSize: 12, color: 'var(--text-3)', marginBottom: 4 }}>📊 Confidence</div>
                <span style={{
                  display: 'inline-block', padding: '2px 12px', borderRadius: 12,
                  fontSize: 13, fontWeight: 600,
                  background: result.confidence === 'High' ? 'rgba(5,150,105,0.1)' :
                             result.confidence === 'Medium' ? 'rgba(217,119,6,0.1)' : 'rgba(100,116,139,0.1)',
                  color: result.confidence === 'High' ? '#059669' :
                         result.confidence === 'Medium' ? '#d97706' : '#64748b'
                }}>
                  {result.confidence}
                </span>
              </div>
              <div>
                <div style={{ fontSize: 12, color: 'var(--text-3)', marginBottom: 4 }}>✅ Parameters Parsed</div>
                <div style={{ fontSize: 16, fontWeight: 600 }}>{result.parsedCount}/20</div>
              </div>
            </div>
            {result.patient && (result.patient.name || result.patient.age || result.patient.sex) && (
              <div style={{ marginTop: 12, padding: '10px 14px', background: 'rgba(255,255,255,0.03)', borderRadius: 'var(--radius-sm)', display: 'flex', gap: 20, flexWrap: 'wrap', fontSize: 13 }}>
                {result.patient.name && <span><strong>Name:</strong> {result.patient.name}</span>}
                {result.patient.age && <span><strong>Age:</strong> {result.patient.age}</span>}
                {result.patient.sex && <span><strong>Sex:</strong> {result.patient.sex}</span>}
                {result.patient.date && <span><strong>Date:</strong> {result.patient.date}</span>}
              </div>
            )}
          </div>

          {/* Alerts Banner */}
          {result.alerts?.length > 0 && (
            <div style={{
              marginTop: 16, padding: '14px 18px', borderRadius: 'var(--radius-sm)',
              background: 'rgba(220,38,38,0.12)', borderLeft: '4px solid var(--red)'
            }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--red)', marginBottom: 8 }}>
                ⚠️ {result.alerts.length} parameter(s) out of normal range
              </div>
              {result.alerts.map((a, i) => (
                <div key={i} style={{ fontSize: 13, color: 'var(--red)', lineHeight: 1.6 }}>
                  • {a.message}
                </div>
              ))}
            </div>
          )}

          {/* Results Table */}
          <div className="card" style={{ marginTop: 16 }}>
            <div className="table-wrap">
              <table style={{ fontSize: 13 }}>
                <thead>
                  <tr>
                    <th>Parameter</th>
                    <th>Value</th>
                    <th>Unit</th>
                    <th>Normal Range</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {result.parameters?.map((p, i) => {
                    const colors = sc(p.status)
                    return (
                      <tr key={i} style={{
                        background: p.status === 'NOT_FOUND' ? 'transparent' : colors.bg,
                        fontStyle: p.status === 'NOT_FOUND' ? 'italic' : 'normal',
                        color: p.status === 'NOT_FOUND' ? 'var(--text-3)' : 'inherit'
                      }}>
                        <td style={{ fontWeight: 600 }}>{p.name}</td>
                        <td>{p.status === 'NOT_FOUND' ? 'Not detected' : p.value}</td>
                        <td>{p.unit || '-'}</td>
                        <td>{p.rangeMin != null ? `${p.rangeMin}–${p.rangeMax}` : '-'}</td>
                        <td>
                          <span style={{
                            display: 'inline-block', padding: '2px 10px', borderRadius: 12,
                            fontSize: 11, fontWeight: 600,
                            background: `${colors.badge}18`, color: colors.badge
                          }}>
                            {p.status === 'NOT_FOUND' ? '⚫' : p.status === 'NORMAL' ? '🟢' : p.status === 'HIGH' ? '🔴' : p.status === 'LOW' ? '🔵' : '🔴'} {colors.label}
                          </span>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>

          <div className="card" style={{ marginTop: 16 }}>
            <div style={{ display: 'flex', justifyContent: 'center' }}>
              <button className="btn btn-ghost" onClick={() => { setResult(null); setFile(null) }}>Analyze Another Report</button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
