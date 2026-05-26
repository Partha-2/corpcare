import { useState, useRef, useCallback } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'
import VendorBadge from '../../components/VendorBadge'
import HealthStatusTable from '../../components/HealthStatusTable'
import AlertNotification from '../../components/AlertNotification'
import { toast } from '../../components/Toast'

export default function HealthReport() {
  const [file, setFile] = useState(null)
  const [dragOver, setDragOver] = useState(false)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const inputRef = useRef(null)

  const handleDrop = useCallback((e) => {
    e.preventDefault()
    setDragOver(false)
    const f = e.dataTransfer.files[0]
    if (f && f.type === 'application/pdf') {
      setFile(f)
      setError('')
      setResult(null)
    } else {
      setError('Please drop a valid PDF file')
    }
  }, [])

  const handleSelect = useCallback((e) => {
    const f = e.target.files[0]
    if (f) {
      setFile(f)
      setError('')
      setResult(null)
    }
  }, [])

  const handleUpload = useCallback(async () => {
    if (!file) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const form = new FormData()
      form.append('file', file)
      const r = await api.post('/health/analyze', form, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      const data = r.data.data
      setResult(data)
      for (const n of (data.notifications || [])) {
        toast(n, n.startsWith('Critical') ? 'error' : 'warning')
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Analysis failed')
    } finally {
      setLoading(false)
    }
  }, [file])

  const downloadReport = useCallback(() => {
    if (!result) return
    const params = result.parameters || []
    const parts = [
      '---------------------------------------',
      'EMPLOYEE HEALTH ANALYSIS REPORT',
      '---------------------------------------',
      '',
      'Vendor Format Detected: ' + (result.vendorFormat || 'N/A'),
      '',
      'Employee Details:',
      'Name: ' + (result.employeeName || 'N/A'),
      'Age: ' + (result.age || 'N/A'),
      'Sex: ' + (result.sex || 'N/A'),
      'Blood Group: ' + (result.bloodGroup || 'N/A'),
      '',
      'Health Parameters:',
      '--------------------------------------------------------------------',
      padStr('Parameter', 25) + padStr('Current Value', 18) + padStr('Range', 18) + 'Status',
      '--------------------------------------------------------------------'
    ]
    for (const p of params) {
      if (p.status === 'NOT_AVAILABLE') continue
      const val = p.value + (p.unit ? ' ' + p.unit : '')
      const range = p.referenceRange || '-'
      parts.push(padStr(p.name, 25) + padStr(val, 18) + padStr(range, 18) + (p.status || ''))
    }
    parts.push('--------------------------------------------------------------------')
    parts.push('')
    parts.push('Recommendations:')
    for (const p of params) {
      if (p.status !== 'NORMAL' && p.status !== 'NOT_AVAILABLE' && p.recommendation) {
        parts.push('- ' + p.name + ': ' + p.recommendation)
      }
    }
    parts.push('')
    parts.push('--- End of Report ---')
    const blob = new Blob([parts.join('\n')], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `Health_Report_${result.employeeName || 'Employee'}.txt`
    a.click()
    URL.revokeObjectURL(url)
  }, [result])

  function padStr(s, len) {
    s = String(s || '')
    return s.length < len ? s + ' '.repeat(len - s.length) : s.substring(0, len)
  }

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <Link to="/employee/dashboard" style={{ fontSize: 13, color: 'var(--primary)', textDecoration: 'none', display: 'block', marginBottom: 8 }}>← Back to Dashboard</Link>
        <h1>🧬 Advanced Health Report Analyzer</h1>
        <p>AI-Based Multi Vendor PDF Analysis — Drag and drop your medical report</p>
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
            {file ? file.name : 'Drag & drop your PDF report here'}
          </h3>
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>
            {file ? `${(file.size / 1024).toFixed(1)} KB` : 'or click to browse — PDF only'}
          </p>
          {file && (
            <div style={{ marginTop: 20, display: 'flex', gap: 12, justifyContent: 'center' }}>
              <button className="btn btn-green" onClick={e => { e.stopPropagation(); handleUpload() }} disabled={loading}>
                {loading ? 'Analyzing...' : '🔍 Analyze Report'}
              </button>
              <button className="btn btn-ghost" onClick={e => { e.stopPropagation(); setFile(null) }} disabled={loading}>
                Clear
              </button>
            </div>
          )}
          {loading && <div style={{ marginTop: 16 }}><Loading text="" /></div>}
        </div>
      )}

      {error && <div className="alert alert-error" style={{ marginTop: 16 }}>{error}</div>}

      {result && (
        <>
          <div className="card" style={{ background: 'linear-gradient(135deg, var(--surface), #1a2a4a)', border: '1px solid var(--surface-2)', marginTop: 16 }}>
            <AlertNotification notifications={result.notifications || []} />

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 8 }}>
              <div>
                <h2 style={{ fontSize: 18, fontWeight: 700 }}>🧬 Health Report Summary</h2>
                <p style={{ fontSize: 13, color: 'var(--text-3)', marginTop: 4 }}>AI-extracted & vendor-matched from your uploaded PDF</p>
              </div>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <VendorBadge vendorFormat={result.vendorFormat} />
                <button className="btn btn-ghost" onClick={downloadReport} style={{ fontSize: 12 }}>⬇ Download Report</button>
                <button className="btn btn-ghost" onClick={() => { setResult(null); setFile(null) }} style={{ fontSize: 12 }}>Upload New</button>
              </div>
            </div>

            <div style={{
              display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 16,
              padding: 12, background: 'rgba(255,255,255,0.03)', borderRadius: 'var(--radius-sm)'
            }}>
              {result.employeeName !== 'Not Available' && (
                <span style={{ fontSize: 13, color: 'var(--text-2)' }}><strong>Name:</strong> {result.employeeName}</span>
              )}
              {result.age !== 'Not Available' && (
                <span style={{ fontSize: 13, color: 'var(--text-2)' }}><strong>Age:</strong> {result.age}</span>
              )}
              {result.sex !== 'Not Available' && (
                <span style={{ fontSize: 13, color: 'var(--text-2)' }}><strong>Sex:</strong> {result.sex}</span>
              )}
              {result.bloodGroup !== 'Not Available' && (
                <span style={{ fontSize: 13, color: 'var(--text-2)' }}><strong>Blood Group:</strong> {result.bloodGroup}</span>
              )}
            </div>

            <HealthStatusTable parameters={result.parameters || []} />
          </div>

          <div className="card" style={{ marginTop: 16 }}>
            <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>⚠️ Disclaimer</h2>
            <p style={{ fontSize: 13, color: 'var(--text-3)', lineHeight: 1.8 }}>
              This is an AI-assisted analysis for informational purposes only. Values are automatically extracted from your
              uploaded PDF based on vendor template matching. Range classifications and recommendations are based on standard
              medical reference ranges. Always consult a qualified healthcare professional for medical advice, diagnosis, or treatment.
            </p>
          </div>
        </>
      )}
    </div>
  )
}
