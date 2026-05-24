export default function Loading({ text = 'Loading...' }) {
  return (
    <div style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      justifyContent: 'center', padding: '60px 20px', color: 'var(--gray-400)'
    }}>
      <div className="spinner" />
      <p style={{ marginTop: 12, fontSize: 14 }}>{text}</p>
    </div>
  )
}
