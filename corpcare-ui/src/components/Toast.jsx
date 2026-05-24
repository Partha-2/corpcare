import { useState, useEffect, useCallback } from 'react'

let toastId = 0
let addToastGlobal = null

export function toast(msg, type = 'success') {
  if (addToastGlobal) addToastGlobal(msg, type)
}

export default function ToastContainer() {
  const [toasts, setToasts] = useState([])

  const add = useCallback((msg, type) => {
    const id = ++toastId
    setToasts(prev => [...prev, { id, msg, type, exiting: false }])
    setTimeout(() => {
      setToasts(prev => prev.map(t => t.id === id ? { ...t, exiting: true } : t))
      setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 300)
    }, 3000)
  }, [])

  useEffect(() => { addToastGlobal = add; return () => { addToastGlobal = null } }, [add])

  return (
    <div style={{
      position: 'fixed', top: 20, right: 20, zIndex: 9999,
      display: 'flex', flexDirection: 'column', gap: 8, pointerEvents: 'none'
    }}>
      {toasts.map(t => (
        <div key={t.id} style={{
          padding: '12px 20px', borderRadius: 8, fontSize: 14, fontWeight: 500,
          background: t.type === 'success' ? '#065f46' : '#991b1b',
          color: '#fff', boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
          opacity: t.exiting ? 0 : 1, transform: t.exiting ? 'translateX(100%)' : 'translateX(0)',
          transition: 'all 0.3s ease', pointerEvents: 'auto', maxWidth: 380
        }}>
          {t.msg}
        </div>
      ))}
    </div>
  )
}
