import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'
import axios from 'axios'
import Loading from '../../components/Loading'

const $ = (v) => v ?? '-'

export default function AdminAnalyzer() {
  const [patients, setPatients] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [selectedPatient, setSelectedPatient] = useState(null)
  const [reports, setReports] = useState([])
  const [reportsLoading, setReportsLoading] = useState(false)
  const [selectedReport, setSelectedReport] = useState(null)
  const [reportLoading, setReportLoading] = useState(false)

  // analyze form
  const [file, setFile] = useState(null)
  const [patientId, setPatientId] = useState('')
  const [name, setName] = useState('')
  const [gender, setGender] = useState('MALE')
  const [age, setAge] = useState('')
  const [analyzing, setAnalyzing] = useState(false)
  const [error, setError] = useState('')

  const baseURL = import.meta.env.VITE_API_URL || '/api'

  useEffect(() => { loadPatients() }, [])

  const loadPatients = async () => {
    try {
      const r = await api.get('/patient/list')
      setPatients(r.data.data || [])
    } catch { /* ignore */ }
    finally { setLoading(false) }
  }

  const loadReports = async (pid) => {
    setSelectedPatient(pid)
    setSelectedReport(null)
    setReportsLoading(true)
    try {
      const r = await api.get(`/patient/${pid}/reports`)
      setReports(r.data.data || [])
    } catch { setReports([]) }
    finally { setReportsLoading(false) }
  }

  const loadReport = async (pid, rid) => {
    setReportLoading(true)
    try {
      const r = await api.get(`/patient/${pid}/reports/${rid}`)
      setSelectedReport(r.data.data)
    } catch { setSelectedReport(null) }
    finally { setReportLoading(false) }
  }

  const deleteReport = async (pid, rid) => {
    if (!confirm('Delete this report?')) return
    try {
      await api.delete(`/patient/${pid}/reports/${rid}`)
      loadReports(pid)
      setSelectedReport(null)
    } catch (e) {
      alert('Delete failed: ' + (e.response?.data?.message || e.message))
    }
  }

  const deletePatient = async (pid) => {
    if (!confirm(`Delete patient ${pid} and ALL their reports?`)) return
    try {
      await api.delete(`/patient/${pid}`)
      setSelectedPatient(null)
      setSelectedReport(null)
      loadPatients()
    } catch (e) {
      alert('Delete failed: ' + (e.response?.data?.message || e.message))
    }
  }

  const handleAnalyze = async (e) => {
    e.preventDefault()
    if (!file) return setError('Select a PDF file')
    const err = validate()
    if (err) return setError(err)
    setAnalyzing(true)
    setError('')
    try {
      const fd = new FormData()
      fd.append('file', file)
      fd.append('patientId', patientId.trim())
      fd.append('name', name.trim())
      fd.append('gender', gender)
      fd.append('age', age)
      const r = await axios.post(`${baseURL}/patient/analyze`, fd, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      loadPatients()
      loadReports(patientId.trim())
      setFile(null)
      setPatientId('')
      setName('')
      setAge('')
      alert('Report analyzed successfully')
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Analysis failed')
    } finally { setAnalyzing(false) }
  }

  const validate = () => {
    if (!patientId.trim() || !/^[0-9]{3,10}$/.test(patientId.trim())) return 'Patient ID: 3-10 digits'
    if (!name.trim() || name.trim().length < 3 || name.trim().length > 60 || !/^[a-zA-Z\s.'-]+$/.test(name.trim())) return 'Name: 3-60 chars, letters/spaces only'
    const a = parseInt(age, 10)
    if (!age || isNaN(a) || a < 1 || a > 120) return 'Age: 1-120'
    return null
  }

  const filtered = patients.filter(p =>
    (p.patientId || '').includes(search) || (p.patientName || '').toLowerCase().includes(search.toLowerCase())
  )

  const exportUrl = (url) => url && !url.startsWith('http') ? `${baseURL}${url.replace('/api', '')}` : url

  if (loading) return <Loading text="Loading patients..." />

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <h1>📋 Medical Report Analyzer</h1>
        <p>Browse patients, view analyzed reports, and run new analysis</p>
      </div>

      {/* Analyze New Report */}
      <div className="card" style={{ marginBottom: 20 }}>
        <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 12 }}>🔬 Analyze New Report</h3>
        <form onSubmit={handleAnalyze}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <input type="file" accept=".pdf" onChange={e => setFile(e.target.files[0])}
              style={{ padding: '8px 12px', border: '2px dashed var(--border)', borderRadius: 6, fontSize: 13 }} required />
            <div style={{ display: 'flex', gap: 10 }}>
              <input type="text" placeholder="Patient ID (digits only)" value={patientId}
                onChange={e => setPatientId(e.target.value.replace(/[^0-9]/g, '').slice(0, 10))}
                style={{ flex: 1, padding: '8px 12px', border: '1px solid var(--border)', borderRadius: 6, fontSize: 13 }} required />
              <input type="text" placeholder="Full Name" value={name}
                onChange={e => setName(e.target.value.replace(/[^a-zA-Z\s.'-]/g, '').slice(0, 60))}
                style={{ flex: 2, padding: '8px 12px', border: '1px solid var(--border)', borderRadius: 6, fontSize: 13 }} required />
            </div>
            <div style={{ display: 'flex', gap: 10 }}>
              <select value={gender} onChange={e => setGender(e.target.value)}
                style={{ flex: 1, padding: '8px 12px', border: '1px solid var(--border)', borderRadius: 6, fontSize: 13 }}>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
              </select>
              <input type="number" placeholder="Age" value={age} onChange={e => setAge(e.target.value)}
                min={1} max={120}
                style={{ flex: 1, padding: '8px 12px', border: '1px solid var(--border)', borderRadius: 6, fontSize: 13 }} required />
            </div>
            <button type="submit" className="btn btn-green" disabled={analyzing} style={{ alignSelf: 'flex-start' }}>
              {analyzing ? 'Analyzing...' : '🔍 Analyze'}
            </button>
          </div>
        </form>
        {error && <div className="alert alert-error" style={{ marginTop: 8, fontSize: 13 }}>{error}</div>}
      </div>

      <div style={{ display: 'flex', gap: 20, alignItems: 'flex-start', flexWrap: 'wrap' }}>
        {/* Patient List */}
        <div className="card" style={{ flex: '1 1 320px', minWidth: 280 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <h3 style={{ fontSize: 15, fontWeight: 700 }}>👥 Patients ({patients.length})</h3>
            <input type="text" placeholder="Search..." value={search}
              onChange={e => setSearch(e.target.value)}
              style={{ padding: '6px 10px', border: '1px solid var(--border)', borderRadius: 6, fontSize: 12, width: 140 }} />
          </div>
          {filtered.length === 0 ? (
            <p style={{ fontSize: 13, color: 'var(--text-3)' }}>No patients found. Analyze a report to create one.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              {filtered.map(p => (
                <div key={p.patientId}
                  onClick={() => loadReports(p.patientId)}
                  style={{
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                    padding: '8px 12px', borderRadius: 6, cursor: 'pointer', fontSize: 13,
                    background: selectedPatient === p.patientId ? 'var(--surface-3)' : 'var(--surface-2)',
                    border: selectedPatient === p.patientId ? '1px solid var(--primary)' : '1px solid transparent'
                  }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>{p.patientName || 'Unknown'}</div>
                    <div style={{ fontSize: 11, color: 'var(--text-3)' }}>ID: {p.patientId} · {p.gender} · {p.age}y</div>
                  </div>
                  <button className="btn btn-sm btn-ghost" onClick={e => { e.stopPropagation(); deletePatient(p.patientId) }}
                    style={{ fontSize: 11, padding: '2px 8px', color: 'var(--red)' }}>Delete</button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Reports */}
        {selectedPatient && (
          <div className="card" style={{ flex: '2 1 400px', minWidth: 300 }}>
            <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 12 }}>
              📄 Reports — {selectedPatient}
            </h3>
            {reportsLoading ? <Loading text="" /> : reports.length === 0 ? (
              <p style={{ fontSize: 13, color: 'var(--text-3)' }}>No reports for this patient.</p>
            ) : (
              <div className="table-wrap">
                <table style={{ fontSize: 12 }}>
                  <thead>
                    <tr>
                      <th>Type</th>
                      <th>Date</th>
                      <th>Source</th>
                      <th>Params</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reports.map(r => (
                      <tr key={r.reportId}
                        onClick={() => loadReport(selectedPatient, r.reportId)}
                        style={{ cursor: 'pointer', background: selectedReport?.reportId === r.reportId ? 'var(--surface-3)' : '' }}>
                        <td style={{ fontWeight: 600 }}>{r.reportType}</td>
                        <td>{r.reportDate}</td>
                        <td>{r.sourceType}</td>
                        <td>{r.totalParameters ?? '-'}</td>
                        <td>
                          <span style={{ fontSize: 11, display: 'flex', gap: 4 }}>
                            {r.highCount > 0 && <span style={{ color: '#dc2626' }}>{r.highCount}↑</span>}
                            {r.lowCount > 0 && <span style={{ color: '#2563eb' }}>{r.lowCount}↓</span>}
                            {r.normalCount > 0 && <span style={{ color: '#16a34a' }}>{r.normalCount}✓</span>}
                          </span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: 4 }} onClick={e => e.stopPropagation()}>
                            {r.viewUrl && <a href={exportUrl(r.viewUrl)} target="_blank" rel="noreferrer" className="btn btn-sm">👁️</a>}
                            {r.downloadUrl && <a href={exportUrl(r.downloadUrl)} target="_blank" rel="noreferrer" className="btn btn-sm">⬇️</a>}
                            <button className="btn btn-sm" style={{ color: 'var(--red)' }}
                              onClick={() => deleteReport(selectedPatient, r.reportId)}>🗑️</button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* Report Detail */}
            {reportLoading && <Loading text="" />}
            {selectedReport && !reportLoading && (
              <div style={{ marginTop: 16, borderTop: '1px solid var(--border)', paddingTop: 12 }}>
                <h4 style={{ fontSize: 14, fontWeight: 700, marginBottom: 8 }}>
                  📊 {selectedReport.reportType} — {selectedReport.patientName}
                </h4>
                {selectedReport.diagnosis && (
                  <div style={{ padding: '6px 10px', background: '#f0f9ff', borderRadius: 4, fontSize: 12, marginBottom: 8 }}>
                    <strong>Diagnosis:</strong> {selectedReport.diagnosis}
                  </div>
                )}
                <div className="table-wrap">
                  <table style={{ fontSize: 12 }}>
                    <thead>
                      <tr>
                        <th>Parameter</th>
                        <th>Value</th>
                        <th>Unit</th>
                        <th>Range</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedReport.parameters?.map((p, i) => (
                        <tr key={i}>
                          <td style={{ fontWeight: 600 }}>{p.name}</td>
                          <td style={{
                            color: p.status === 'HIGH' ? '#dc2626' : p.status === 'LOW' ? '#2563eb' : 'inherit',
                            fontWeight: p.status === 'HIGH' || p.status === 'LOW' ? 700 : 400
                          }}>{$(p.value)}</td>
                          <td>{p.unit || '-'}</td>
                          <td>{p.referenceRange || '-'}</td>
                          <td>
                            <span style={{
                              display: 'inline-block', padding: '1px 8px', borderRadius: 10, fontSize: 11, fontWeight: 600,
                              background: p.status === 'HIGH' ? '#fef2f2' : p.status === 'LOW' ? '#eff6ff' : p.status === 'NORMAL' ? '#f0fdf4' : '#f9fafb',
                              color: p.status === 'HIGH' ? '#dc2626' : p.status === 'LOW' ? '#2563eb' : p.status === 'NORMAL' ? '#16a34a' : '#6b7280'
                            }}>{p.status || '-'}</span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
