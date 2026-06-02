import { useState } from 'react'
import api from '../api/axios'

export default function MedicalReportUpload({ employeeId, onUploadSuccess }) {
  const [file, setFile] = useState(null)
  const [uploadedBy, setUploadedBy] = useState('')
  const [uploading, setUploading] = useState(false)
  const [success, setSuccess] = useState(null)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!file || !uploadedBy) return
    setUploading(true)
    setSuccess(null)
    setError('')
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('uploadedBy', uploadedBy)
      const r = await api.post(`/medical-reports/upload/${employeeId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      const count = r.data.data.length
      setSuccess(`Report split into ${count} PDFs successfully`)
      setFile(null)
      setUploadedBy('')
      if (onUploadSuccess) onUploadSuccess()
    } catch (err) {
      setError(err.response?.data?.message || 'Upload failed')
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="card" style={{ marginTop: 16 }}>
      <div className="card-header">
        <h2>Upload Medical Report</h2>
      </div>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>PDF File</label>
          <input
            type="file"
            accept=".pdf"
            onChange={e => setFile(e.target.files[0])}
            required
          />
          {file && <small style={{ color: 'var(--gray-500)' }}>{file.name}</small>}
        </div>
        <div className="form-group">
          <label>Uploaded By</label>
          <input
            type="text"
            placeholder="Doctor or admin name"
            value={uploadedBy}
            onChange={e => setUploadedBy(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="btn btn-primary" disabled={uploading}>
          {uploading ? 'Uploading...' : 'Upload & Split'}
        </button>
      </form>
      {success && <div className="alert" style={{ background: '#d4edda', color: '#155724', marginTop: 12 }}>{success}</div>}
      {error && <div className="alert alert-error" style={{ marginTop: 12 }}>{error}</div>}
    </div>
  )
}
