import { useState, useRef, useEffect } from 'react'
import api from '../api/axios'

export default function ChatBot() {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState([
    { role: 'bot', text: '👋 Hi! I\'m the CorpCare Assistant. Ask me anything about the platform!' }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const endRef = useRef(null)

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = async () => {
    const text = input.trim()
    if (!text || loading) return
    setInput('')
    setMessages(prev => [...prev, { role: 'user', text }])
    setLoading(true)

    try {
      const r = await api.post('/chat', { message: text })
      const reply = r.data?.data || 'Sorry, I didn\'t understand that.'
      setMessages(prev => [...prev, { role: 'bot', text: reply }])
    } catch {
      setMessages(prev => [...prev, { role: 'bot', text: '⚠️ Sorry, I\'m having trouble right now. Please try again.' }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <button
        onClick={() => setOpen(!open)}
        className="chat-fab"
      >
        {open ? '✕' : '💬'}
      </button>

      {open && (
        <div className="chat-panel">
          <div className="chat-hdr">
            <div className="chat-brand">
              <span style={{ fontSize: 20 }}>💬</span>
              <div>
                <strong>CorpCare Assistant</strong>
                <span className="chat-status">Online</span>
              </div>
            </div>
            <button onClick={() => setOpen(false)} className="chat-close">✕</button>
          </div>
          <div className="chat-body">
            {messages.map((m, i) => (
              <div key={i} className={`chat-msg ${m.role}`}>
                {m.text.split('\n').map((line, j) => (
                  <p key={j}>{line}</p>
                ))}
              </div>
            ))}
            {loading && (
              <div className="chat-msg bot">
                <p style={{ display: 'flex', gap: 4 }}>
                  <span className="dot-pulse" />
                </p>
              </div>
            )}
            <div ref={endRef} />
          </div>
          <form className="chat-input" onSubmit={e => { e.preventDefault(); handleSend() }}>
            <input
              value={input}
              onChange={e => setInput(e.target.value)}
              placeholder="Ask anything about CorpCare..."
              disabled={loading}
            />
            <button type="submit" disabled={loading || !input.trim()}>Send</button>
          </form>
        </div>
      )}
    </>
  )
}
