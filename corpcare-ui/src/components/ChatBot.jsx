import { useState, useRef, useEffect } from 'react'

const faqs = [
  {
    q: 'What is CorpCare?',
    a: 'CorpCare is a B2B platform for corporate employee health management. Companies onboard employees, hospitals create slots, and employees self-book appointments with WhatsApp + voice call confirmations.'
  },
  {
    q: 'How do I book an appointment?',
    a: 'Go to Employee Login → enter your email & employee code → select a hospital → pick an available slot → confirm. You\'ll get a WhatsApp message and a voice call confirmation.'
  },
  {
    q: 'What is my employee code?',
    a: 'Your employee code is provided by your company\'s HR. It\'s a unique identifier like "VK001" or "EMP001". Contact your HR if you don\'t have it.'
  },
  {
    q: 'Which portals are available?',
    a: 'There are 4 portals — Admin (platform owner), Client (corporate HR), Hospital (partner hospitals), and Employee (individual users). Each has a specific login.'
  },
  {
    q: 'How do I cancel an appointment?',
    a: 'Log into your Employee portal → go to "My Appointments" → click "Cancel" next to the booking. Confirmed slots are freed immediately.'
  },
  {
    q: 'Is WhatsApp confirmation automatic?',
    a: 'Yes! Every booking automatically sends a WhatsApp message via Twilio with slot details, hospital location, and timing.'
  },
  {
    q: 'Do I get a voice call?',
    a: 'Yes — Bolna.ai calls you automatically after booking to confirm the appointment details in your preferred language.'
  },
  {
    q: 'How do I add my health vitals?',
    a: 'Log in as an employee → go to "My Vitals" → fill in height, weight, blood pressure, blood sugar & blood group. Save to update your health profile.'
  },
  {
    q: 'Who can access the Admin portal?',
    a: 'Only the platform owner with the admin password. Contact your system administrator for access.'
  },
  {
    q: 'Is my data secure?',
    a: 'Yes. All data is encrypted in transit and at rest. We follow industry-standard security practices for health information management.'
  }
]

function getAnswer(input) {
  const lower = input.toLowerCase()
  const words = lower.split(/\s+/)

  let best = null
  let bestScore = 0

  for (const faq of faqs) {
    const qLower = faq.q.toLowerCase()
    let score = 0
    for (const word of words) {
      if (word.length < 3) continue
      if (qLower.includes(word)) score++
    }
    if (score > bestScore) {
      bestScore = score
      best = faq
    }
  }

  if (best && bestScore > 0) return best.a
  return null
}

function getAllQuestions() {
  return faqs.map(f => f.q)
}

export default function ChatBot() {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState([
    { role: 'bot', text: '👋 Hi! Ask me anything about CorpCare. For example:\n• How to book an appointment?\n• What portals are available?\n• How to cancel?' }
  ])
  const [input, setInput] = useState('')
  const endRef = useRef(null)

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = () => {
    const text = input.trim()
    if (!text) return
    setInput('')
    setMessages(prev => [...prev, { role: 'user', text }])

    setTimeout(() => {
      const answer = getAnswer(text)
      if (answer) {
        setMessages(prev => [...prev, { role: 'bot', text: answer }])
      } else {
        setMessages(prev => [...prev, {
          role: 'bot',
          text: `I'm not sure about that. Try asking about:\n${getAllQuestions().map(q => `• ${q}`).join('\n')}`
        }])
      }
    }, 400)
  }

  return (
    <>
      <button
        onClick={() => setOpen(!open)}
        style={{
          position: 'fixed', bottom: 24, right: 24, zIndex: 9998,
          width: 56, height: 56, borderRadius: '50%', border: 'none',
          background: 'linear-gradient(135deg, #2563eb, #6366f1)',
          color: 'white', fontSize: 24, cursor: 'pointer',
          boxShadow: '0 4px 20px rgba(37,99,235,0.4)',
          transition: 'all 0.2s', display: 'flex', alignItems: 'center', justifyContent: 'center'
        }}
        onMouseEnter={e => e.currentTarget.style.transform = 'scale(1.1)'}
        onMouseLeave={e => e.currentTarget.style.transform = 'scale(1)'}
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
            <div ref={endRef} />
          </div>
          <form className="chat-input" onSubmit={e => { e.preventDefault(); handleSend() }}>
            <input
              value={input}
              onChange={e => setInput(e.target.value)}
              placeholder="Type a question..."
              autoFocus
            />
            <button type="submit">Send</button>
          </form>
        </div>
      )}
    </>
  )
}
