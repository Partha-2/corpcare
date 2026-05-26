const VENDOR_STYLES = {
  SHIVANI_TEMPLATE: { label: 'Shivani Diagnostic Centre', color: '#7c3aed', bg: 'rgba(124,58,237,0.1)' },
  STARLAB_TEMPLATE: { label: 'Star Lab', color: '#0891b2', bg: 'rgba(8,145,178,0.1)' },
  GENERIC_TEMPLATE: { label: 'Generic Medical Report', color: '#64748b', bg: 'rgba(100,116,139,0.1)' }
}

export default function VendorBadge({ vendorFormat }) {
  const style = VENDOR_STYLES[vendorFormat] || VENDOR_STYLES.GENERIC_TEMPLATE
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      padding: '5px 14px', borderRadius: 20,
      fontSize: 12, fontWeight: 600, letterSpacing: '0.3px',
      color: style.color, background: style.bg,
      border: `1px solid ${style.color}22`
    }}>
      <span style={{
        width: 6, height: 6, borderRadius: '50%',
        background: style.color, display: 'inline-block'
      }} />
      {style.label}
    </span>
  )
}
