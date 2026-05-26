import { useState, useRef } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/axios'
import Loading from '../../components/Loading'

export default function HealthReport() {
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
      setFile(f)
      setError('')
      setResult(null)
    } else {
      setError('Please drop a valid PDF file')
    }
  }

  const handleSelect = (e) => {
    const f = e.target.files[0]
    if (f) {
      setFile(f)
      setError('')
      setResult(null)
    }
  }

  const handleUpload = async () => {
    if (!file) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const form = new FormData()
      form.append('file', file)
      const r = await api.post('/health-report/analyze', form, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
      setResult(r.data.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Analysis failed')
    } finally {
      setLoading(false)
    }
  }

  const statusClass = (val) => {
    if (val === 'Normal' || val === 'Healthy') return 'status-available'
    if (val === 'High' || val === 'Low' || val === 'Above Required Range' || val === 'Below Required Range' || val === 'Above Healthy Range' || val === 'Below Healthy Range') return 'status-booked'
    return ''
  }

  const fields = result ? [
    { label: 'Name', value: result.name },
    { label: 'Age', value: result.age + ' yrs' },
    { label: 'Sex', value: result.sex },
    { label: 'Blood Group', value: result.bloodGroup },
    { label: 'Height', value: result.height + ' cm', status: result.heightStatus },
    { label: 'Weight', value: result.weight + ' kg', status: result.weightStatus },
    { label: 'BMI', value: result.bmi, status: result.bmi && result.bmi !== 'N/A' ? (parseFloat(result.bmi) < 18.5 || parseFloat(result.bmi) > 24.9 ? 'High' : 'Normal') : '' },
    { label: 'BP Systolic', value: result.bloodPressureSystolic + ' mmHg', status: result.bpStatus },
    { label: 'BP Diastolic', value: result.bloodPressureDiastolic + ' mmHg' },
    { label: 'Blood Sugar (Fasting)', value: result.bloodSugarFasting + ' mg/dL', status: result.sugarStatus },
    { label: 'Blood Sugar (PP)', value: result.bloodSugarPostPrandial + ' mg/dL' },
    { label: 'Blood Sugar (Random)', value: result.bloodSugarRandom + ' mg/dL' },
    { label: 'Hemoglobin', value: result.hemoglobin + ' g/dL', status: result.hemoglobinStatus },
    { label: 'RBC Count', value: result.rbcCount + ' M/μL' },
    { label: 'WBC Count', value: result.wbcCount + ' /μL' },
    { label: 'Platelet Count', value: result.plateletCount + ' /μL' },
    { label: 'Total Cholesterol', value: result.totalCholesterol + ' mg/dL', status: result.cholesterolStatus },
    { label: 'HDL Cholesterol', value: result.hdlCholesterol + ' mg/dL' },
    { label: 'LDL Cholesterol', value: result.ldlCholesterol + ' mg/dL' },
    { label: 'Triglycerides', value: result.triglycerides + ' mg/dL' },
    { label: 'Serum Creatinine', value: result.serumCreatinine + ' mg/dL', status: result.creatinineStatus },
    { label: 'Urea', value: result.urea + ' mg/dL' },
    { label: 'Uric Acid', value: result.uricAcid + ' mg/dL' },
    { label: 'Pulse Rate', value: result.pulseRate + ' bpm' },
    { label: 'Oxygen Saturation', value: result.oxygenSaturation + ' %' },
    { label: 'Temperature', value: result.temperature + ' °F' },
    { label: 'Vitamin D', value: result.vitaminD + ' ng/mL' },
    { label: 'Vitamin B12', value: result.vitaminB12 + ' pg/mL' },
    { label: 'TSH', value: result.tsh + ' μIU/mL' },
    { label: 'ESR', value: result.esr + ' mm/hr' },
    { label: 'Total Bilirubin', value: result.totalBilirubin + ' mg/dL' },
    { label: 'Total Protein', value: result.totalProtein + ' g/dL' }
  ].filter(f => f.value && f.value !== 'N/A' && f.value !== 'undefined N/A') : []

  return (
    <div className="fade-in">
      <div className="page-hdr">
        <Link to="/employee/dashboard" style={{ fontSize: 13, color: 'var(--primary)', textDecoration: 'none', display: 'block', marginBottom: 8 }}>← Back to Dashboard</Link>
        <h1>📄 Health Report Analysis</h1>
        <p>Upload your medical PDF report to extract and analyze health parameters</p>
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
          <div style={{ fontSize: 48, marginBottom: 12 }}>{file ? '📎' : '📄'}</div>
          <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 8 }}>
            {file ? file.name : 'Drag & drop your PDF report here'}
          </h3>
          <p style={{ fontSize: 13, color: 'var(--text-3)' }}>
            {file ? `${(file.size / 1024).toFixed(1)} KB` : 'or click to browse — PDF only'}
          </p>
          {file && (
            <div style={{ marginTop: 16, display: 'flex', gap: 12, justifyContent: 'center' }}>
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
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <div>
                <h2 style={{ fontSize: 18, fontWeight: 700 }}>🧬 Health Report Summary</h2>
                <p style={{ fontSize: 13, color: 'var(--text-3)', marginTop: 4 }}>AI-extracted from your uploaded PDF</p>
              </div>
              <button className="btn btn-ghost" onClick={() => { setResult(null); setFile(null) }}>Upload New</button>
            </div>

            <div className="stats-grid" style={{ marginBottom: 20 }}>
              {result.recommendedWeightMin !== 'N/A' && (
                <div className="stat-card" style={{ gridColumn: 'span 3' }}>
                  <div style={{ fontSize: 13, color: 'var(--text-3)', marginBottom: 4 }}>Recommended Weight Range</div>
                  <div style={{ fontSize: 20, fontWeight: 700 }}>{result.recommendedWeightMin} — {result.recommendedWeightMax} kg</div>
                </div>
              )}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 8 }}>
              {fields.map((f, i) => (
                <div key={i} style={{
                  background: 'rgba(255,255,255,0.03)',
                  border: '1px solid var(--surface-2)',
                  borderRadius: 'var(--radius-sm)',
                  padding: '10px 14px',
                  display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                  gap: 8
                }}>
                  <div>
                    <div style={{ fontSize: 11, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '0.3px', fontWeight: 600 }}>{f.label}</div>
                    <div style={{ fontSize: 14, fontWeight: 600, marginTop: 2 }}>{f.value}</div>
                  </div>
                  {f.status && (
                    <span className={`status-badge ${statusClass(f.status)}`} style={{ fontSize: 11, whiteSpace: 'nowrap' }}>● {f.status}</span>
                  )}
                </div>
              ))}
            </div>
          </div>

          <div className="card" style={{ marginTop: 16 }}>
            <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>⚠️ Disclaimer</h2>
            <p style={{ fontSize: 13, color: 'var(--text-3)', lineHeight: 1.8 }}>
              This is an AI-assisted analysis for informational purposes only. Values are extracted automatically from your PDF report and may contain errors. Always consult a qualified healthcare professional for medical advice, diagnosis, or treatment.
            </p>
          </div>
        </>
      )}
    </div>
  )
}
