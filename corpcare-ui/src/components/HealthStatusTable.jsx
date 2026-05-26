const COLOR_MAP = {
  GREEN: { bg: 'rgba(5,150,105,0.1)', text: '#059669', border: 'rgba(5,150,105,0.2)' },
  YELLOW: { bg: 'rgba(217,119,6,0.1)', text: '#d97706', border: 'rgba(217,119,6,0.2)' },
  RED: { bg: 'rgba(220,38,38,0.1)', text: '#dc2626', border: 'rgba(220,38,38,0.2)' },
  GRAY: { bg: 'rgba(100,116,139,0.08)', text: '#64748b', border: 'rgba(100,116,139,0.15)' }
}

const STATUS_LABEL = {
  NORMAL: 'Normal',
  BELOW_RANGE: 'Below Range',
  ABOVE_RANGE: 'Above Range',
  NOT_AVAILABLE: 'N/A'
}

export default function HealthStatusTable({ parameters }) {
  return (
    <div className="table-wrap" style={{ marginTop: 8 }}>
      <table style={{ fontSize: 13 }}>
        <thead>
          <tr>
            <th style={{ width: '28%' }}>Parameter</th>
            <th style={{ width: '18%' }}>Current Value</th>
            <th style={{ width: '18%' }}>Range</th>
            <th style={{ width: '16%' }}>Status</th>
            <th>Recommendation</th>
          </tr>
        </thead>
        <tbody>
          {parameters.filter(p => p.status !== 'NOT_AVAILABLE').map((p, i) => {
            const colors = COLOR_MAP[p.color] || COLOR_MAP.GRAY
            return (
              <tr key={i}>
                <td><strong>{p.name}</strong></td>
                <td>{p.value} {p.unit && <span style={{ fontSize: 11, color: 'var(--text-3)' }}>{p.unit}</span>}</td>
                <td style={{ fontSize: 12, color: 'var(--text-3)' }}>{p.referenceRange || '-'}</td>
                <td>
                  <span style={{
                    display: 'inline-block', padding: '3px 10px', borderRadius: 12,
                    fontSize: 11, fontWeight: 600,
                    background: colors.bg, color: colors.text, border: `1px solid ${colors.border}`
                  }}>
                    ● {STATUS_LABEL[p.status] || p.status}
                  </span>
                </td>
                <td style={{ fontSize: 12, color: p.color === 'RED' ? 'var(--red)' : p.color === 'YELLOW' ? 'var(--amber)' : 'var(--text-3)' }}>
                  {p.recommendation}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
