import { useState, useEffect } from 'react'
import api from '../api/axios'

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']

function formatDate(dateStr) {
  const d = new Date(dateStr)
  return `${d.getDate()} ${MONTHS[d.getMonth()]} ${d.getFullYear()}`
}

export default function MedicalReportList({ employeeId, refreshKey }) {
  const [reports, setReports] = useState([])
  const [loading, setLoading] = useState(true)

  const fetchReports = () => {
    setLoading(true)
    api.get(`/medical-reports/employee/${employeeId}`)
      .then(r => setReports(r.data.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchReports() }, [employeeId, refreshKey])

  const handleView = (report) => {
    api.get(`/medical-reports/view/${report.id}/employee/${employeeId}`, {
      responseType: 'blob'
    }).then(r => {
      const url = URL.createObjectURL(r.data)
      window.open(url, '_blank')
    }).catch(() => {})
  }

  const handleDownload = (report) => {
    api.get(`/medical-reports/download/${report.id}/employee/${employeeId}`, {
      responseType: 'blob'
    }).then(r => {
      const url = URL.createObjectURL(r.data)
      const a = document.createElement('a')
      a.href = url
      a.download = `${report.reportType}.pdf`
      a.click()
      URL.revokeObjectURL(url)
    }).catch(() => {})
  }

  const handleDelete = (reportId) => {
    if (!window.confirm('Are you sure you want to delete this report?')) return
    api.delete(`/medical-reports/${reportId}`)
      .then(() => setReports(prev => prev.filter(r => r.id !== reportId)))
      .catch(() => {})
  }

  if (loading) return <div style={{ textAlign: 'center', padding: 24, color: 'var(--gray-500)' }}>Loading reports...</div>

  if (reports.length === 0) {
    return (
      <div className="card" style={{ marginTop: 16 }}>
        <div className="card-header"><h2>Medical Reports</h2></div>
        <p style={{ color: 'var(--gray-500)', padding: 16 }}>No reports uploaded yet.</p>
      </div>
    )
  }

  return (
    <div className="card" style={{ marginTop: 16 }}>
      <div className="card-header">
        <h2>Medical Reports ({reports.length})</h2>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        {reports.map(report => (
          <div key={report.id} className="card" style={{ margin: 0, padding: 16 }}>
            <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 4 }}>{report.reportType}</h3>
            <small style={{ color: 'var(--gray-500)' }}>{report.originalFileName}</small>
            <div style={{ fontSize: 13, color: 'var(--gray-500)', marginTop: 4 }}>
              Uploaded {formatDate(report.uploadedAt)} by {report.uploadedBy}
            </div>
            <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
              <button className="btn" style={{ fontSize: 13, padding: '4px 12px' }} onClick={() => handleView(report)}>
                View PDF
              </button>
              <button className="btn" style={{ fontSize: 13, padding: '4px 12px' }} onClick={() => handleDownload(report)}>
                Download
              </button>
              <button className="btn" style={{ fontSize: 13, padding: '4px 12px', background: '#ef4444', color: '#fff', border: 'none' }} onClick={() => handleDelete(report.id)}>
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
