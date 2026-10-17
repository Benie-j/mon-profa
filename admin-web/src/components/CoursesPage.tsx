import { useCallback, useEffect, useState, type FormEvent } from 'react'
import {
  BookOpen, ChevronDown, ChevronRight, FileText, LoaderCircle,
  Plus, RefreshCw, Save, X,
} from 'lucide-react'
import { supabase } from '../lib/supabase'

type Subject = {
  id: string
  name: string
  school_class: string
  series: string | null
}

type Chapter = {
  id: string
  subject_id: string
  title: string
  description: string
  sort_order: number
  status: 'draft' | 'published'
}

type Lesson = {
  id: string
  chapter_id: string
  title: string
  introduction: string
  content: string
  summary: string
  sort_order: number
  status: 'draft' | 'published'
}

type Editor =
  | { kind: 'chapter'; id?: string }
  | { kind: 'lesson'; chapterId: string; id?: string }
  | null

const emptyChapter = {
  title: '',
  description: '',
  sort_order: 0,
  status: 'draft' as 'draft' | 'published',
}

const emptyLesson = {
  title: '',
  introduction: '',
  content: '',
  summary: '',
  sort_order: 0,
  status: 'draft' as 'draft' | 'published',
}

export default function CoursesPage() {
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [lessons, setLessons] = useState<Lesson[]>([])
  const [subjectId, setSubjectId] = useState('')
  const [expanded, setExpanded] = useState<string[]>([])
  const [editor, setEditor] = useState<Editor>(null)
  const [chapterForm, setChapterForm] = useState(emptyChapter)
  const [lessonForm, setLessonForm] = useState(emptyLesson)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')

    const subjectResult = await supabase
      .from('kalyno_subjects')
      .select('id,name,school_class,series')
      .order('school_class')
      .order('sort_order')
      .order('name')

    if (subjectResult.error) {
      setError(`Chargement des disciplines impossible : ${subjectResult.error.message}`)
      setLoading(false)
      return
    }

    const nextSubjects = (subjectResult.data ?? []) as Subject[]
    setSubjects(nextSubjects)

    const nextSubjectId =
      nextSubjects.some(item => item.id === subjectId)
        ? subjectId
        : nextSubjects[0]?.id ?? ''

    setSubjectId(nextSubjectId)

    if (!nextSubjectId) {
      setChapters([])
      setLessons([])
      setLoading(false)
      return
    }

    const chapterResult = await supabase
      .from('kalyno_chapters')
      .select('id,subject_id,title,description,sort_order,status')
      .eq('subject_id', nextSubjectId)
      .order('sort_order')
      .order('title')

    if (chapterResult.error) {
      setError(`Chargement des chapitres impossible : ${chapterResult.error.message}`)
      setLoading(false)
      return
    }

    const nextChapters = (chapterResult.data ?? []) as Chapter[]
    setChapters(nextChapters)

    if (nextChapters.length === 0) {
      setLessons([])
      setLoading(false)
      return
    }

    const lessonResult = await supabase
      .from('kalyno_lessons')
      .select('id,chapter_id,title,introduction,content,summary,sort_order,status')
      .in('chapter_id', nextChapters.map(item => item.id))
      .order('sort_order')
      .order('title')

    if (lessonResult.error) {
      setError(`Chargement des leçons impossible : ${lessonResult.error.message}`)
      setLoading(false)
      return
    }

    setLessons((lessonResult.data ?? []) as Lesson[])
    setLoading(false)
  }, [subjectId])

  useEffect(() => {
    void loadData()
  }, [loadData])

  function chooseSubject(id: string) {
    setSubjectId(id)
    setEditor(null)
    setNotice('')
  }

  function openNewChapter() {
    setChapterForm({ ...emptyChapter, sort_order: chapters.length })
    setEditor({ kind: 'chapter' })
    setError('')
    setNotice('')
  }

  function openEditChapter(chapter: Chapter) {
    setChapterForm({
      title: chapter.title,
      description: chapter.description ?? '',
      sort_order: chapter.sort_order,
      status: chapter.status,
    })
    setEditor({ kind: 'chapter', id: chapter.id })
    setError('')
    setNotice('')
  }

  function openNewLesson(chapterId: string) {
    const count = lessons.filter(item => item.chapter_id === chapterId).length
    setLessonForm({ ...emptyLesson, sort_order: count })
    setEditor({ kind: 'lesson', chapterId })
    setError('')
    setNotice('')
  }

  function openEditLesson(lesson: Lesson) {
    setLessonForm({
      title: lesson.title,
      introduction: lesson.introduction ?? '',
      content: lesson.content ?? '',
      summary: lesson.summary ?? '',
      sort_order: lesson.sort_order,
      status: lesson.status,
    })
    setEditor({ kind: 'lesson', chapterId: lesson.chapter_id, id: lesson.id })
    setError('')
    setNotice('')
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!editor) return

    setSaving(true)
    setError('')
    setNotice('')

    let message = ''

    if (editor.kind === 'chapter') {
      const title = chapterForm.title.trim()
      if (!title) {
        setError('Le titre du chapitre est obligatoire.')
        setSaving(false)
        return
      }

      const payload = {
        subject_id: subjectId,
        title,
        description: chapterForm.description.trim(),
        sort_order: chapterForm.sort_order,
        status: chapterForm.status,
        updated_at: new Date().toISOString(),
      }

      const result = editor.id
        ? await supabase.from('kalyno_chapters').update(payload).eq('id', editor.id)
        : await supabase.from('kalyno_chapters').insert(payload)

      if (result.error) {
        setError(`Enregistrement du chapitre impossible : ${result.error.message}`)
        setSaving(false)
        return
      }

      message = editor.id ? 'Chapitre modifié.' : 'Chapitre créé en base de données.'
    } else {
      const title = lessonForm.title.trim()
      if (!title) {
        setError('Le titre de la leçon est obligatoire.')
        setSaving(false)
        return
      }

      const payload = {
        chapter_id: editor.chapterId,
        title,
        introduction: lessonForm.introduction.trim(),
        content: lessonForm.content.trim(),
        summary: lessonForm.summary.trim(),
        sort_order: lessonForm.sort_order,
        status: lessonForm.status,
        updated_at: new Date().toISOString(),
      }

      const result = editor.id
        ? await supabase.from('kalyno_lessons').update(payload).eq('id', editor.id)
        : await supabase.from('kalyno_lessons').insert(payload)

      if (result.error) {
        setError(`Enregistrement de la leçon impossible : ${result.error.message}`)
        setSaving(false)
        return
      }

      message = editor.id ? 'Leçon modifiée.' : 'Leçon créée en base de données.'
    }

    setEditor(null)
    setSaving(false)
    setNotice(message)
    await loadData()
  }

  async function changeStatus(
    kind: 'chapter' | 'lesson',
    id: string,
    status: 'draft' | 'published',
  ) {
    setError('')
    setNotice('')

    const table = kind === 'chapter' ? 'kalyno_chapters' : 'kalyno_lessons'
    const result = await supabase
      .from(table)
      .update({ status, updated_at: new Date().toISOString() })
      .eq('id', id)

    if (result.error) {
      setError(`Changement de statut impossible : ${result.error.message}`)
      return
    }

    setNotice(status === 'published' ? 'Contenu publié.' : 'Contenu remis en brouillon.')
    await loadData()
  }

  const currentSubject = subjects.find(item => item.id === subjectId)

  return (
    <section className="panel subjects-panel">
      <div className="subjects-toolbar">
        <label className="subject-field" style={{ flex: 1, minWidth: 200 }}>
          <span>Discipline</span>
          <select
            value={subjectId}
            onChange={event => chooseSubject(event.target.value)}
            disabled={loading || subjects.length === 0}
          >
            {subjects.length === 0 && <option value="">Aucune discipline disponible</option>}
            {subjects.map(subject => (
              <option key={subject.id} value={subject.id}>
                {subject.name} — {subject.school_class}
                {subject.series ? ` ${subject.series}` : ''}
              </option>
            ))}
          </select>
        </label>
        <button className="button button-secondary" onClick={() => void loadData()} disabled={loading}>
          <RefreshCw size={16} /> Actualiser
        </button>
        <button className="button button-primary" onClick={openNewChapter} disabled={!subjectId || loading}>
          <Plus size={17} /> Nouveau chapitre
        </button>
      </div>

      {notice && <div className="subjects-success" role="status">{notice}</div>}
      {error && !editor && (
        <div className="subjects-error" role="alert">
          <span>{error}</span>
          <button className="button button-secondary" onClick={() => void loadData()}>Réessayer</button>
        </div>
      )}

      <div className="subjects-summary">
        <div>
          <span className="subjects-summary-label">CONTENU DE LA DISCIPLINE</span>
          <strong>{chapters.length} chapitre(s)</strong>
          <small>{lessons.length} leçon(s) enregistrée(s) dans Supabase</small>
        </div>
        <div className="subjects-summary-icon"><BookOpen size={23} /></div>
      </div>

      {loading ? (
        <div className="subjects-state">
          <LoaderCircle className="subjects-spinner" size={24} />
          <span>Chargement des contenus réels…</span>
        </div>
      ) : subjects.length === 0 ? (
        <div className="subjects-state">
          <BookOpen size={30} />
          <h2>Aucune discipline disponible</h2>
          <p>Ajoute d’abord une discipline dans le module Disciplines.</p>
        </div>
      ) : chapters.length === 0 ? (
        <div className="subjects-state">
          <div className="empty-illustration"><BookOpen size={30} /></div>
          <h2>Aucun chapitre pour cette discipline</h2>
          <p>
            {currentSubject?.name} ne contient encore aucun chapitre.
            Tu peux créer le premier chapitre sans publier de contenu aux élèves.
          </p>
          <button className="button button-primary" onClick={openNewChapter}>
            <Plus size={17} /> Créer un chapitre
          </button>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: 12, padding: '8px 0' }}>
          {chapters.map((chapter, index) => {
            const chapterLessons = lessons.filter(item => item.chapter_id === chapter.id)
            const isExpanded = expanded.includes(chapter.id)

            return (
              <article key={chapter.id} style={{
                border: '1px solid #e9ebf2',
                borderRadius: 12,
                background: '#fff',
                overflow: 'hidden',
              }}>
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 12,
                  padding: 15,
                  flexWrap: 'wrap',
                }}>
                  <button
                    className="icon-button"
                    aria-label={isExpanded ? 'Réduire le chapitre' : 'Développer le chapitre'}
                    onClick={() => setExpanded(old =>
                      isExpanded ? old.filter(id => id !== chapter.id) : [...old, chapter.id]
                    )}
                  >
                    {isExpanded ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
                  </button>
                  <div className="subject-name-icon"><BookOpen size={18} /></div>
                  <div style={{ flex: 1, minWidth: 150 }}>
                    <strong>{index + 1}. {chapter.title}</strong>
                    <small style={{ display: 'block', color: '#858ba0', marginTop: 5 }}>
                      {chapter.description || 'Aucune description'} · {chapterLessons.length} leçon(s)
                    </small>
                  </div>
                  <span className="subject-pill">
                    {chapter.status === 'published' ? 'Publié' : 'Brouillon'}
                  </span>
                  <button className="subject-edit" onClick={() => openEditChapter(chapter)}>Modifier</button>
                  <button
                    className="button button-secondary"
                    onClick={() => void changeStatus(
                      'chapter', chapter.id,
                      chapter.status === 'published' ? 'draft' : 'published',
                    )}
                  >
                    {chapter.status === 'published' ? 'Dépublier' : 'Publier'}
                  </button>
                </div>

                {isExpanded && (
                  <div style={{ borderTop: '1px solid #e9ebf2', padding: 16, background: '#fafbff' }}>
                    {chapterLessons.length === 0 ? (
                      <p style={{ color: '#858ba0', fontSize: 12 }}>Aucune leçon dans ce chapitre.</p>
                    ) : (
                      <div style={{ display: 'grid', gap: 8, marginBottom: 14 }}>
                        {chapterLessons.map((lesson, lessonIndex) => (
                          <div key={lesson.id} style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 10,
                            padding: 12,
                            border: '1px solid #e9ebf2',
                            borderRadius: 9,
                            background: '#fff',
                            flexWrap: 'wrap',
                          }}>
                            <FileText size={17} />
                            <div style={{ flex: 1, minWidth: 140 }}>
                              <strong style={{ fontSize: 12 }}>
                                {lessonIndex + 1}. {lesson.title}
                              </strong>
                              <small style={{ display: 'block', color: '#858ba0', marginTop: 4 }}>
                                {lesson.status === 'published' ? 'Publié' : 'Brouillon'}
                              </small>
                            </div>
                            <button className="subject-edit" onClick={() => openEditLesson(lesson)}>Modifier</button>
                            <button
                              className="button button-secondary"
                              onClick={() => void changeStatus(
                                'lesson', lesson.id,
                                lesson.status === 'published' ? 'draft' : 'published',
                              )}
                            >
                              {lesson.status === 'published' ? 'Dépublier' : 'Publier'}
                            </button>
                          </div>
                        ))}
                      </div>
                    )}
                    <button className="button button-primary" onClick={() => openNewLesson(chapter.id)}>
                      <Plus size={16} /> Ajouter une leçon
                    </button>
                  </div>
                )}
              </article>
            )
          })}
        </div>
      )}

      {editor && (
        <div className="subject-dialog-backdrop" role="presentation">
          <section
            className="subject-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="course-editor-title"
          >
            <header className="subject-dialog-header">
              <div>
                <span className="subjects-summary-label">CONTENU PÉDAGOGIQUE</span>
                <h2 id="course-editor-title">
                  {editor.kind === 'chapter'
                    ? editor.id ? 'Modifier le chapitre' : 'Nouveau chapitre'
                    : editor.id ? 'Modifier la leçon' : 'Nouvelle leçon'}
                </h2>
              </div>
              <button className="icon-button" aria-label="Fermer" onClick={() => !saving && setEditor(null)}>
                <X size={19} />
              </button>
            </header>

            <form onSubmit={event => void handleSubmit(event)}>
              {editor.kind === 'chapter' ? (
                <>
                  <label className="subject-field">
                    <span>Titre du chapitre *</span>
                    <input required maxLength={180} value={chapterForm.title}
                      onChange={event => setChapterForm(old => ({ ...old, title: event.target.value }))}
                      placeholder="Ex. Calcul littéral" />
                  </label>
                  <label className="subject-field">
                    <span>Description</span>
                    <textarea rows={3} maxLength={2000} value={chapterForm.description}
                      onChange={event => setChapterForm(old => ({ ...old, description: event.target.value }))}
                      placeholder="Objectifs et notions abordées…" />
                  </label>
                  <label className="subject-field">
                    <span>Ordre d’affichage</span>
                    <input type="number" min={0} value={chapterForm.sort_order}
                      onChange={event => setChapterForm(old => ({ ...old, sort_order: Number(event.target.value) }))} />
                  </label>
                  <label className="subject-field">
                    <span>Statut</span>
                    <select value={chapterForm.status}
                      onChange={event => setChapterForm(old => ({ ...old, status: event.target.value as 'draft' | 'published' }))}>
                      <option value="draft">Brouillon — non publié</option>
                      <option value="published">Publié</option>
                    </select>
                  </label>
                </>
              ) : (
                <>
                  <label className="subject-field">
                    <span>Titre de la leçon *</span>
                    <input required maxLength={180} value={lessonForm.title}
                      onChange={event => setLessonForm(old => ({ ...old, title: event.target.value }))}
                      placeholder="Titre de la leçon" />
                  </label>
                  <label className="subject-field">
                    <span>Introduction</span>
                    <textarea rows={3} maxLength={5000} value={lessonForm.introduction}
                      onChange={event => setLessonForm(old => ({ ...old, introduction: event.target.value }))}
                      placeholder="Présente brièvement la notion…" />
                  </label>
                  <label className="subject-field">
                    <span>Contenu du cours</span>
                    <textarea rows={8} maxLength={50000} value={lessonForm.content}
                      onChange={event => setLessonForm(old => ({ ...old, content: event.target.value }))}
                      placeholder="Rédige l’explication, les définitions et les exemples corrigés…" />
                  </label>
                  <label className="subject-field">
                    <span>Résumé à retenir</span>
                    <textarea rows={4} maxLength={10000} value={lessonForm.summary}
                      onChange={event => setLessonForm(old => ({ ...old, summary: event.target.value }))}
                      placeholder="Les points essentiels de la leçon…" />
                  </label>
                  <label className="subject-field">
                    <span>Ordre d’affichage</span>
                    <input type="number" min={0} value={lessonForm.sort_order}
                      onChange={event => setLessonForm(old => ({ ...old, sort_order: Number(event.target.value) }))} />
                  </label>
                  <label className="subject-field">
                    <span>Statut</span>
                    <select value={lessonForm.status}
                      onChange={event => setLessonForm(old => ({ ...old, status: event.target.value as 'draft' | 'published' }))}>
                      <option value="draft">Brouillon — non publié</option>
                      <option value="published">Publié</option>
                    </select>
                  </label>
                </>
              )}

              {error && <div className="subjects-error" role="alert">{error}</div>}

              <div className="subject-dialog-actions">
                <button type="button" className="button button-secondary"
                  onClick={() => setEditor(null)} disabled={saving}>Annuler</button>
                <button type="submit" className="button button-primary" disabled={saving}>
                  {saving
                    ? <><LoaderCircle className="subjects-spinner" size={16} /> Enregistrement…</>
                    : <><Save size={16} /> Enregistrer</>}
                </button>
              </div>
              <p className="subject-form-note">
                Les données sont enregistrées dans Supabase. Le statut brouillon est sélectionné par défaut.
              </p>
            </form>
          </section>
        </div>
      )}
    </section>
  )
}
