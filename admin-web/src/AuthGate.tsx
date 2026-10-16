import { FormEvent, useEffect, useState } from 'react'
import { LockKeyhole, GraduationCap, LogOut, ShieldCheck } from 'lucide-react'
import { supabase, supabaseConfigured } from './lib/supabase'
import type { User } from '@supabase/supabase-js'

type GateState = 'loading' | 'signed-out' | 'admin'

export default function AuthGate({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<GateState>('loading')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [user, setUser] = useState<User | null>(null)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState('')

  async function verifyAdmin(currentUser: User) {
    const { data, error } = await supabase.rpc('is_kalyno_admin')

    if (error || data !== true) {
      await supabase.auth.signOut()
      setUser(null)
      setState('signed-out')
      setMessage(
        'Ce compte ne possède pas les droits administrateur. Vérifie que l’adresse est autorisée et confirmée dans Supabase.',
      )
      return false
    }

    setUser(currentUser)
    setState('admin')
    setMessage('')
    return true
  }

  useEffect(() => {
    let active = true

    async function restoreSession() {
      if (!supabaseConfigured) {
        setMessage('La configuration Supabase est absente. Configure le fichier .env.local.')
        setState('signed-out')
        return
      }

      const { data, error } = await supabase.auth.getSession()

      if (!active) return

      if (error || !data.session?.user) {
        setState('signed-out')
        return
      }

      await verifyAdmin(data.session.user)
    }

    const { data: listener } = supabase.auth.onAuthStateChange((event) => {
      if (event === 'SIGNED_OUT' && active) {
        setUser(null)
        setState('signed-out')
      }
    })

    void restoreSession()

    return () => {
      active = false
      listener.subscription.unsubscribe()
    }
  }, [])

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage('')

    if (!supabaseConfigured) {
      setMessage('Supabase n’est pas configuré. Vérifie le fichier .env.local.')
      return
    }

    setBusy(true)

    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email: email.trim(),
        password,
      })

      if (error || !data.user) {
        setMessage('Connexion impossible. Vérifie ton adresse, ton mot de passe et la confirmation de ton e-mail.')
        return
      }

      await verifyAdmin(data.user)
    } catch {
      setMessage('Impossible de joindre Supabase. Vérifie ta connexion Internet et la configuration.')
    } finally {
      setBusy(false)
    }
  }

  async function handleLogout() {
    setBusy(true)
    const { error } = await supabase.auth.signOut()
    setBusy(false)

    if (error) {
      setMessage('La déconnexion a échoué. Réessaie.')
      return
    }

    setUser(null)
    setState('signed-out')
  }

  if (state === 'loading') {
    return (
      <main style={styles.page}>
        <p>Vérification de la session sécurisée…</p>
      </main>
    )
  }

  if (state === 'admin') {
    return (
      <>
        <div style={styles.sessionBar}>
          <span><ShieldCheck size={16} /> Session administrateur : {user?.email}</span>
          <button style={styles.logout} onClick={handleLogout} disabled={busy}>
            <LogOut size={15} /> Déconnexion
          </button>
        </div>
        {children}
      </>
    )
  }

  return (
    <main style={styles.page}>
      <section style={styles.card}>
        <div style={styles.logo}><GraduationCap size={28} /></div>
        <p style={styles.eyebrow}>ESPACE SÉCURISÉ</p>
        <h1 style={styles.title}>KALYNO</h1>
        <p style={styles.subtitle}>Administration pédagogique</p>

        <form onSubmit={handleLogin} style={styles.form}>
          <label style={styles.label} htmlFor="admin-email">Adresse e-mail</label>
          <input
            id="admin-email"
            type="email"
            autoComplete="username"
            required
            value={email}
            onChange={event => setEmail(event.target.value)}
            placeholder="administrateur@exemple.com"
            style={styles.input}
          />

          <label style={styles.label} htmlFor="admin-password">Mot de passe</label>
          <input
            id="admin-password"
            type="password"
            autoComplete="current-password"
            required
            value={password}
            onChange={event => setPassword(event.target.value)}
            placeholder="Ton mot de passe"
            style={styles.input}
          />

          {message && <p role="alert" style={styles.error}>{message}</p>}

          <button type="submit" disabled={busy} style={styles.submit}>
            <LockKeyhole size={17} />
            {busy ? 'Vérification…' : 'Se connecter'}
          </button>
        </form>

        <p style={styles.footnote}>
          Seuls les comptes confirmés et autorisés dans Supabase peuvent accéder à cet espace.
        </p>
      </section>
    </main>
  )
}

const styles: Record<string, React.CSSProperties> = {
  page: {
    minHeight: '100vh',
    display: 'grid',
    placeItems: 'center',
    padding: 20,
    background: '#f5f7fc',
    color: '#20243a',
    fontFamily: 'Inter, system-ui, sans-serif',
  },
  card: {
    width: '100%',
    maxWidth: 420,
    boxSizing: 'border-box',
    padding: 32,
    background: '#fff',
    border: '1px solid #e6e8f1',
    borderRadius: 18,
    boxShadow: '0 18px 55px #20243a0d',
  },
  logo: {
    display: 'grid',
    placeItems: 'center',
    width: 54,
    height: 54,
    borderRadius: 15,
    color: '#5148c9',
    background: '#f0efff',
  },
  eyebrow: {
    margin: '24px 0 7px',
    color: '#6b63cb',
    fontSize: 10,
    fontWeight: 800,
    letterSpacing: 1.4,
  },
  title: { margin: 0, fontSize: 28, letterSpacing: 1.5 },
  subtitle: { margin: '7px 0 26px', color: '#7e8499', fontSize: 13 },
  form: { display: 'flex', flexDirection: 'column', gap: 10 },
  label: { marginTop: 7, fontSize: 12, fontWeight: 700 },
  input: {
    width: '100%',
    boxSizing: 'border-box',
    padding: '12px 13px',
    border: '1px solid #dfe2ed',
    borderRadius: 9,
    background: '#fff',
    color: '#20243a',
    fontSize: 14,
    outlineColor: '#6b63cb',
  },
  submit: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 9,
    marginTop: 12,
    padding: 13,
    border: 0,
    borderRadius: 9,
    color: '#fff',
    background: '#5148c9',
    fontWeight: 700,
    cursor: 'pointer',
  },
  error: {
    padding: 11,
    border: '1px solid #f0caca',
    borderRadius: 8,
    background: '#fff4f4',
    color: '#a33131',
    fontSize: 12,
    lineHeight: 1.6,
  },
  footnote: {
    margin: '22px 0 0',
    color: '#858ba0',
    fontSize: 11,
    lineHeight: 1.7,
  },
  sessionBar: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 10,
    padding: '10px 18px',
    background: '#eeedff',
    color: '#403a9b',
    fontSize: 12,
  },
  logout: {
    display: 'flex',
    alignItems: 'center',
    gap: 7,
    padding: '7px 10px',
    border: '1px solid #d7d4fa',
    borderRadius: 7,
    background: '#fff',
    color: '#403a9b',
    cursor: 'pointer',
  },
}
