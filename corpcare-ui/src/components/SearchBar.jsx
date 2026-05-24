export default function SearchBar({ value, onChange, placeholder = 'Search...' }) {
  return (
    <div style={{ position: 'relative' }}>
      <span style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', fontSize: 14, color: 'var(--gray-400)', pointerEvents: 'none' }}>🔍</span>
      <input
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder={placeholder}
        style={{
          width: '100%', padding: '10px 14px 10px 38px', fontSize: 14,
          border: '1.5px solid var(--gray-200)', borderRadius: 'var(--radius-xs)',
          background: 'var(--input-bg)', fontFamily: 'inherit', transition: 'border-color 0.15s',
          outline: 'none'
        }}
        onFocus={e => e.target.style.borderColor = 'var(--primary)'}
        onBlur={e => e.target.style.borderColor = 'var(--gray-200)'}
      />
    </div>
  )
}
