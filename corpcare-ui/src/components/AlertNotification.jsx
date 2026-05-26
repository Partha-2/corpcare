import { useEffect, useState } from 'react'

export default function AlertNotification({ notifications }) {
  const [visible, setVisible] = useState(true)

  useEffect(() => {
    setVisible(true)
    if (notifications.length === 0) return
    const timer = setTimeout(() => setVisible(false), 6000)
    return () => clearTimeout(timer)
  }, [notifications])

  if (!visible || notifications.length === 0) return null

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 16 }}>
      {notifications.map((n, i) => {
        const isCritical = n.startsWith('Critical')
        return (
          <div key={i} style={{
            display: 'flex', alignItems: 'flex-start', gap: 10,
            padding: '12px 16px', borderRadius: 'var(--radius-sm)',
            background: isCritical ? 'rgba(220,38,38,0.08)' : 'rgba(217,119,6,0.08)',
            borderLeft: `4px solid ${isCritical ? 'var(--red)' : 'var(--amber)'}`,
            fontSize: 13, lineHeight: 1.5, color: isCritical ? 'var(--red)' : 'var(--amber)'
          }}>
            <span style={{ fontSize: 16, flexShrink: 0 }}>
              {isCritical ? '🚨' : '⚡'}
            </span>
            <div>
              <strong style={{ display: 'block', marginBottom: 2 }}>
                {isCritical ? 'Critical Health Value Detected' : 'Correction Recommended'}
              </strong>
              {n}
            </div>
            <button
              onClick={() => setVisible(false)}
              style={{
                marginLeft: 'auto', background: 'none', border: 'none',
                color: 'inherit', cursor: 'pointer', fontSize: 16, opacity: 0.5,
                flexShrink: 0, padding: 2
              }}
            >✕</button>
          </div>
        )
      })}
    </div>
  )
}
