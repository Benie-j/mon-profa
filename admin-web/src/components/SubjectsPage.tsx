import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { BookOpen, Edit3, LoaderCircle, Plus, RefreshCw, Search, X } from 'lucide-react'
import { supabase } from '../lib/supabase'

type Subject = {
  id: string
  name: string
  school_class: string
  series: string | null
  description: string
  sort_order: number
  created_at: string
  updated_at: string
}

type SubjectForm = {
  name: string
  school_class: string
  series: string
  description: string
  sort_order: number
}

const emptyForm: SubjectForm = {
  name: '',
  school_class: '3e',
  series: '',
  description: '',
  sort_order: 0,
}

export default function SubjectsPage() {
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [search, setSearch] = useState('')
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<Subject | null>(null)
  const [form, setForm] = useState<SubjectForm>(emptyForm)

  const loadSubjects = useCallback(async () => {
    setLoading(true)
    setError('')

    const { data, error: requestError } = await supabase
      .from('kalyno_subjects')
      .select('id,name,school_class,series,description,sort_order,created_at,updated_at')
      .order('school_class', { ascending: true })
      .order('sort_order', { ascending: true })
      .order('name', { ascending: true })

    if (requestError) {
      setError(`Chargement impossible : ${requestError.message}`)
      setSubjects([])
    } else {
      setSubjects((data ?? []) as Subject[])
    }

    setLoading(false)
  }, [])

  useEffect(() => {
    void loadSubjects()
  }, [loadSubjects])

  const filteredSubjects = subjects.filter(subject => {
    const text = `${subject.name} ${subject.school_class} ${subject.series ?? ''} ${subject.description}`
    return text.toLocaleLowerCase('fr').includes(search.trim().toLocaleLowerCase('fr'))
  })

  function openCreate() {
    setEditing(null)
    setForm({ ...emptyForm, sort_order: subjects.length })
    setError('')
    setNotice('')
    setDialogOpen(true)
  }

  function openEdit(subject: Subject) {
    setEditing(subject)
    setForm({
      name: subject.name,
      school_class: subject.school_class,
      series: subject.series ?? '',
      description: subject.description ?? '',
      sort_order: subject.sort_order,
    })
    setError('')
    setNotice('')
    setDialogOpen(true)
  }

  function updateField<K extends keyof SubjectForm>(key: K, value: SubjectForm[K]) {
    setForm(current => ({
      ...current,
      [key]: value,
      ...(key === 'school_class' && value !== 'Terminale' ? { series: '' } : {}),
    }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setNotice('')

    const name = form.name.trim()
    if (!name) {
      setError('Le nom de la discipline est obligatoire.')
      return
    }

    if (form.school_class === 'Terminale' && !form.series) {
      setError('Choisis la série de Terminale.')
      return
    }

    setSaving(true)

    const payload = {
      name,
      school_class: form.school_class,
      series: form.school_class === 'Terminale' ? form.series : null,
      description: form.description.trim(),
      sort_order: Number.isFinite(form.sort_order) ? form.sort_order : 0,
      updated_at: new Date().toISOString(),
    }

    const result = editing
      ? await supabase.from('kalyno_subjects').update(payload).eq('id', editing.id)
      : await supabase.from('kalyno_subjects').insert(payload)

    if (result.error) {
      setError(`Enregistrement impossible : ${result.error.message}`)
      setSaving(false)
      return
    }

    setDialogOpen(false)
    setEditing(null)
    setNotice(editing ? 'Discipline modifiée.' : 'Discipline ajoutée.')
    setSaving(false)
    await loadSubjects()
  }

  return (
    <section className="panel subjects-panel">
      <div className="subjects-toolbar">
        <label className="search-field subjects-search">
          <Search size={17} />
          <input
            aria-label="Rechercher une discipline"
            placeholder="Rechercher par nom, classe ou série…"
            value={search}
            onChange={event => setSearch(event.target.value)}
          />
        </label>
        <button className="button button-secondary" onClick={() => void loadSubjects()} disabled={loading}>
          <RefreshCw size={16} /> Actualiser
        </button>
        <button className="button button-primary" onClick={openCreate}>
          <Plus size={17} /> Ajouter
        </button>
      </div>

      {notice && <div className="subjects-success" role="status">{notice}</div>}
      {error && !dialogOpen && (
        <div className="subjects-error" role="alert">
          <span>{error}</span>
          <button className="button button-secondary" onClick={() => void loadSubjects()}>Réessayer</button>
        </div>
      )}

      <div className="subjects-summary">
        <div>
          <span className="subjects-summary-label">DISCIPLINES ENREGISTRÉES</span>
          <strong>{subjects.length}</strong>
          <small>Nombre réel dans Supabase</small>
        </div>
        <div className="subjects-summary-icon"><BookOpen size={23} /></div>
      </div>

      {loading ? (
        <div className="subjects-state"><LoaderCircle className="subjects-spinner" size={24} /><span>Chargement des disciplines…</span></div>
      ) : error ? (
        <div className="subjects-state">
          <BookOpen size={28} />
          <h2>Les données n’ont pas pu être chargées</h2>
          <p>Vérifie la connexion, les droits de l’administrateur et les règles d’accès Supabase.</p>
        </div>
      ) : filteredSubjects.length === 0 ? (
        <div className="subjects-state">
          <div className="empty-illustration"><BookOpen size={30} /></div>
          <h2>{subjects.length === 0 ? 'Aucune discipline enregistrée' : 'Aucun résultat'}</h2>
          <p>
            {subjects.length === 0
              ? 'La base est vide. Ajoute ta première discipline pour commencer à organiser les contenus pédagogiques.'
              : 'Aucune discipline ne correspond à cette recherche.'}
          </p>
          {subjects.length === 0 && (
            <button className="button button-primary" onClick={openCreate}><Plus size={17} /> Ajouter une discipline</button>
          )}
        </div>
      ) : (
        <div className="subjects-table-wrap">
          <table className="subjects-table">
            <thead>
              <tr>
                <th>Discipline</th>
                <th>Classe</th>
                <th>Série</th>
                <th>Ordre</th>
                <th><span className="sr-only">Actions</span></th>
              </tr>
            </thead>
            <tbody>
              {filteredSubjects.map(subject => (
                <tr key={subject.id}>
                  <td>
                    <div className="subject-name">
                      <span className="subject-name-icon"><BookOpen size={17} /></span>
                      <span><strong>{subject.name}</strong><small>{subject.description || 'Aucune description'}</small></span>
                    </div>
                  </td>
                  <td><span className="subject-pill">{subject.school_class}</span></td>
                  <td>{subject.series || '—'}</td>
                  <td>{subject.sort_order}</td>
                  <td>
                    <button className="subject-edit" onClick={() => openEdit(subject)} aria-label={`Modifier ${subject.name}`}>
                      <Edit3 size={16} /> Modifier
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {dialogOpen && (
        <div className="subject-dialog-backdrop" role="presentation" onMouseDown={event => {
          if (event.target === event.currentTarget && !saving) setDialogOpen(false)
        }}>
          <section className="subject-dialog" role="dialog" aria-modal="true" aria-labelledby="subject-dialog-title">
            <header className="subject-dialog-header">
              <div>
                <span className="subjects-summary-label">CATALOGUE PÉDAGOGIQUE</span>
                <h2 id="subject-dialog-title">{editing ? 'Modifier la discipline' : 'Nouvelle discipline'}</h2>
              </div>
              <button className="icon-button" aria-label="Fermer" onClick={() => setDialogOpen(false)} disabled={saving}><X size={19} /></button>
            </header>

            <form onSubmit={event => void handleSubmit(event)}>
              <label className="subject-field">
                <span>Nom de la discipline *</span>
                <input required maxLength={120} autoFocus value={form.name} onChange={event => updateField('name', event.target.value)} placeholder="Ex. Mathématiques" />
              </label>

              <div className="subject-form-grid">
                <label className="subject-field">
                  <span>Classe *</span>
                  <select value={form.school_class} onChange={event => updateField('school_class', event.target.value)}>
                    <option value="3e">3e</option>
                    <option value="Terminale">Terminale</option>
                  </select>
                </label>

                <label className="subject-field">
                  <span>Série {form.school_class === 'Terminale' ? '*' : '(facultatif)'}</span>
                  <select
                    value={form.series}
                    disabled={form.school_class !== 'Terminale'}
                    required={form.school_class === 'Terminale'}
                    onChange={event => updateField('series', event.target.value)}
                  >
                    <option value="">Sans série</option>
                    <option value="A">A</option>
                    <option value="C">C</option>
                    <option value="D">D</option>
                  </select>
                </label>
              </div>

              <label className="subject-field">
                <span>Description</span>
                <textarea rows={3} maxLength={1000} value={form.description} onChange={event => updateField('description', event.target.value)} placeholder="Objectif ou précisions sur cette discipline…" />
              </label>

              <label className="subject-field">
                <span>Ordre d’affichage</span>
                <input type="number" min={0} max={9999} value={form.sort_order} onChange={event => updateField('sort_order', Number(event.target.value))} />
              </label>

              {error && <div className="subjects-error" role="alert">{error}</div>}

              <div className="subject-dialog-actions">
                <button type="button" className="button button-secondary" onClick={() => setDialogOpen(false)} disabled={saving}>Annuler</button>
                <button type="submit" className="button button-primary" disabled={saving}>
                  {saving ? <><LoaderCircle className="subjects-spinner" size={16} /> Enregistrement…</> : editing ? 'Enregistrer les modifications' : 'Enregistrer la discipline'}
                </button>
              </div>
              <p className="subject-form-note">Les modifications sont enregistrées directement dans la base de données.</p>
            </form>
          </section>
        </div>
      )}
    </section>
  )
}
