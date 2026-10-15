import { useState } from 'react'
import {
  Activity, Archive, ArrowDownRight, ArrowUpRight, BookOpen,
  CheckCircle2, ChevronDown, ChevronRight, CircleHelp, Clock3,
  Cloud, Command, FileCheck2, FileText, FolderOpen, GraduationCap,
  LayoutDashboard, LockKeyhole, Menu, Plus, Search,
  Settings2, ShieldCheck, SlidersHorizontal, Users, X, Zap,
} from 'lucide-react'
import './styles.css'

type Section =
  | 'overview' | 'courses' | 'subjects' | 'classes'
  | 'files' | 'publishing' | 'team' | 'settings'

const navigation = [
  {
    title: 'ESPACE PÉDAGOGIQUE',
    items: [
      { id: 'overview' as Section, label: 'Vue d’ensemble', icon: LayoutDashboard },
      { id: 'courses' as Section, label: 'Cours et leçons', icon: BookOpen },
      { id: 'subjects' as Section, label: 'Disciplines', icon: GraduationCap },
      { id: 'classes' as Section, label: 'Classes et niveaux', icon: SlidersHorizontal },
    ],
  },
  {
    title: 'RESSOURCES',
    items: [
      { id: 'files' as Section, label: 'Bibliothèque', icon: FolderOpen },
      { id: 'publishing' as Section, label: 'Publications', icon: FileCheck2 },
    ],
  },
  {
    title: 'ADMINISTRATION',
    items: [
      { id: 'team' as Section, label: 'Équipe et accès', icon: Users },
      { id: 'settings' as Section, label: 'Paramètres', icon: Settings2 },
    ],
  },
]

const sectionInfo: Record<Section, { title: string; subtitle: string }> = {
  overview: {
    title: 'Vue d’ensemble',
    subtitle: 'Le centre de pilotage de votre espace pédagogique.',
  },
  courses: {
    title: 'Cours et leçons',
    subtitle: 'Structurez les programmes et préparez les ressources de vos élèves.',
  },
  subjects: {
    title: 'Disciplines',
    subtitle: 'Organisez les matières enseignées dans KALYNO.',
  },
  classes: {
    title: 'Classes et niveaux',
    subtitle: 'Structurez les niveaux scolaires et leurs programmes.',
  },
  files: {
    title: 'Bibliothèque documentaire',
    subtitle: 'Centralisez les documents qui alimentent vos contenus pédagogiques.',
  },
  publishing: {
    title: 'Publications',
    subtitle: 'Contrôlez ce qui est prêt, en attente ou publié pour les élèves.',
  },
  team: {
    title: 'Équipe et accès',
    subtitle: 'Préparez une collaboration sécurisée avec vos futurs contributeurs.',
  },
  settings: {
    title: 'Paramètres',
    subtitle: 'Configurez les services et les règles de votre espace KALYNO.',
  },
}

function App() {
  const [section, setSection] = useState<Section>('overview')
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [notice, setNotice] = useState('')
  const info = sectionInfo[section]

  function unavailable(feature: string) {
    setNotice(`${feature} : cette fonction nécessite encore la connexion au backend.`)
  }

  function navigate(id: Section) {
    setSection(id)
    setSidebarOpen(false)
    setNotice('')
  }

  return (
    <div className="app-shell">
      {sidebarOpen && (
        <button
          className="mobile-overlay"
          aria-label="Fermer le menu"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <aside className={`sidebar ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="brand">
          <div className="brand-mark"><GraduationCap size={25} /></div>
          <div className="brand-copy">
            <strong>KALYNO</strong>
            <span>ESPACE ADMINISTRATEUR</span>
          </div>
          <button
            className="icon-button sidebar-close"
            aria-label="Fermer le menu"
            onClick={() => setSidebarOpen(false)}
          >
            <X size={19} />
          </button>
        </div>

        <div className="workspace-switch">
          <div className="workspace-icon"><BookOpen size={18} /></div>
          <div className="workspace-copy">
            <strong>Administration</strong>
            <span>Espace principal</span>
          </div>
          <ChevronDown size={16} className="muted-icon" />
        </div>

        <nav className="main-nav" aria-label="Navigation principale">
          {navigation.map(group => (
            <div className="nav-group" key={group.title}>
              <div className="nav-heading">{group.title}</div>
              {group.items.map(item => {
                const Icon = item.icon
                return (
                  <button
                    key={item.id}
                    className={`nav-item ${section === item.id ? 'active' : ''}`}
                    onClick={() => navigate(item.id)}
                  >
                    <Icon size={18} strokeWidth={1.8} />
                    <span>{item.label}</span>
                    {section === item.id && <span className="active-dot" />}
                  </button>
                )
              })}
            </div>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="security-card">
            <div className="security-icon"><ShieldCheck size={18} /></div>
            <strong>Votre espace, vos règles</strong>
            <p>Les accès administrateur et la protection des contenus seront activés avec le backend.</p>
            <button onClick={() => navigate('settings')}>
              Voir la configuration <ChevronRight size={14} />
            </button>
          </div>
          <div className="sidebar-foot">
            <span className="status-dot" />
            <span>Mode de préparation</span>
            <span className="version">v0.2</span>
          </div>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="topbar-left">
            <button
              className="icon-button mobile-menu"
              aria-label="Ouvrir le menu"
              onClick={() => setSidebarOpen(true)}
            >
              <Menu size={21} />
            </button>
            <div className="breadcrumb">
              <span>Administration</span>
              <ChevronRight size={14} />
              <strong>{info.title}</strong>
            </div>
          </div>
          <div className="topbar-actions">
            <div className="environment-label">
              <span className="status-dot" />
              Backend non connecté
            </div>
            <button
              className="help-button"
              onClick={() => unavailable('Centre d’aide')}
            >
              <CircleHelp size={17} /><span>Aide</span>
            </button>
            <div className="avatar-placeholder" title="Compte administrateur à configurer">
              <LockKeyhole size={17} />
            </div>
          </div>
        </header>

        <div className="page-content">
          <div className="page-heading">
            <div>
              <div className="eyebrow"><span /> PILOTAGE PÉDAGOGIQUE</div>
              <h1>{info.title}</h1>
              <p className="page-subtitle">{info.subtitle}</p>
            </div>
            <div className="heading-actions">
              {section === 'overview' ? (
                <button className="button button-primary" onClick={() => navigate('courses')}>
                  <Plus size={17} /> Préparer un cours
                </button>
              ) : (
                <button className="button button-secondary" onClick={() => unavailable('Création')}>
                  <Plus size={17} /> Nouveau
                </button>
              )}
            </div>
          </div>

          {notice && (
            <div className="notice" role="status">
              <Activity size={18} />
              <span>{notice}</span>
              <button className="icon-button" onClick={() => setNotice('')} aria-label="Fermer">
                <X size={17} />
              </button>
            </div>
          )}

          {section === 'overview' && (
            <Overview onNavigate={navigate} />
          )}

          {section === 'courses' && (
            <ResourcePage
              icon={BookOpen}
              title="Votre catalogue de cours"
              description="Chaque cours pourra regrouper un niveau, une discipline, des chapitres, des leçons, des exercices et des corrigés."
              onAction={() => unavailable('Création d’un cours')}
              action="Créer un cours"
            />
          )}

          {section === 'subjects' && (
            <ResourcePage
              icon={GraduationCap}
              title="Aucune discipline chargée"
              description="Les disciplines seront affichées ici lorsqu’elles seront enregistrées dans la source de données réelle."
              onAction={() => unavailable('Création d’une discipline')}
              action="Ajouter une discipline"
            />
          )}

          {section === 'classes' && (
            <ResourcePage
              icon={SlidersHorizontal}
              title="Structure scolaire non configurée"
              description="Les niveaux et classes devront correspondre au programme scolaire retenu pour KALYNO."
              onAction={() => unavailable('Configuration des niveaux')}
              action="Configurer les niveaux"
            />
          )}

          {section === 'files' && <FilesPage onAction={() => unavailable('Connexion Google Drive')} />}

          {section === 'publishing' && (
            <ResourcePage
              icon={FileCheck2}
              title="Aucun contenu à publier"
              description="Les brouillons, validations et publications apparaîtront ici une fois le workflow relié au backend."
              onAction={() => unavailable('Workflow de publication')}
              action="Configurer la publication"
            />
          )}

          {section === 'team' && <TeamPage onAction={() => unavailable('Gestion des collaborateurs')} />}

          {section === 'settings' && <SettingsPage onAction={unavailable} />}

          <footer className="page-footer">
            <div className="footer-brand"><GraduationCap size={17} /> KALYNO</div>
            <span>Le savoir ouvre l’avenir.</span>
            <span className="footer-separator">·</span>
            <span>Administration pédagogique</span>
          </footer>
        </div>
      </main>
    </div>
  )
}

function Overview({ onNavigate }: { onNavigate: (id: Section) => void }) {
  return (
    <>
      <section className="welcome-panel">
        <div className="welcome-content">
          <div className="welcome-tag"><Zap size={14} /> VOTRE ESPACE DE TRAVAIL</div>
          <h2>Construisez un apprentissage<br className="desktop-break" /> de qualité, une ressource à la fois.</h2>
          <p>Préparez vos contenus, organisez vos documents et maîtrisez leur publication depuis un seul espace.</p>
          <button className="button button-light" onClick={() => onNavigate('courses')}>
            Organiser les contenus <ArrowUpRight size={17} />
          </button>
        </div>
        <div className="welcome-art" aria-hidden="true">
          <div className="art-ring ring-one" />
          <div className="art-ring ring-two" />
          <div className="art-book"><BookOpen size={54} strokeWidth={1.25} /></div>
          <div className="art-chip chip-one"><CheckCircle2 size={17} /></div>
          <div className="art-chip chip-two"><FileText size={17} /></div>
          <div className="art-chip chip-three"><GraduationCap size={18} /></div>
        </div>
      </section>

      <div className="section-title-row">
        <div>
          <h2>État de votre espace</h2>
          <p>État de configuration, sans statistiques simulées.</p>
        </div>
        <span className="live-label"><span className="status-dot" /> État actuel</span>
      </div>

      <section className="status-grid">
        <StatusCard
          icon={LockKeyhole}
          label="Authentification"
          value="À configurer"
          detail="Accès administrateur"
          tone="violet"
        />
        <StatusCard
          icon={Cloud}
          label="Google Drive"
          value="Non connecté"
          detail="Documents pédagogiques"
          tone="blue"
        />
        <StatusCard
          icon={Activity}
          label="Backend"
          value="Non connecté"
          detail="Données et sauvegardes"
          tone="amber"
        />
        <StatusCard
          icon={ShieldCheck}
          label="Publication"
          value="Non activée"
          detail="Accès des élèves"
          tone="green"
        />
      </section>

      <div className="content-grid">
        <section className="panel setup-panel">
          <div className="panel-header">
            <div>
              <h2>Préparer l’administration</h2>
              <p>Les étapes nécessaires avant la mise en service.</p>
            </div>
            <div className="panel-icon"><Command size={19} /></div>
          </div>
          <div className="setup-list">
            <SetupItem
              number="01"
              title="Sécuriser l’accès administrateur"
              description="Créer une authentification distincte de celle des élèves."
              onClick={() => onNavigate('settings')}
            />
            <SetupItem
              number="02"
              title="Connecter la bibliothèque documentaire"
              description="Autoriser l’accès aux fichiers Google Drive."
              onClick={() => onNavigate('files')}
            />
            <SetupItem
              number="03"
              title="Définir le programme scolaire"
              description="Préparer disciplines, niveaux et structure des cours."
              onClick={() => onNavigate('subjects')}
            />
            <SetupItem
              number="04"
              title="Activer le workflow de publication"
              description="Contrôler les brouillons et les contenus visibles des élèves."
              onClick={() => onNavigate('publishing')}
            />
          </div>
        </section>

        <section className="panel quick-panel">
          <div className="panel-header">
            <div>
              <h2>Accès rapides</h2>
              <p>Accédez aux principaux espaces.</p>
            </div>
          </div>
          <div className="quick-links">
            <QuickLink icon={BookOpen} title="Cours et leçons" description="Organiser les contenus" onClick={() => onNavigate('courses')} />
            <QuickLink icon={FolderOpen} title="Bibliothèque" description="Documents et ressources" onClick={() => onNavigate('files')} />
            <QuickLink icon={FileCheck2} title="Publications" description="Contrôle de diffusion" onClick={() => onNavigate('publishing')} />
            <QuickLink icon={Users} title="Équipe et accès" description="Préparer les rôles" onClick={() => onNavigate('team')} />
          </div>
          <div className="honesty-note">
            <ShieldCheck size={17} />
            <p><strong>Aucune donnée fictive</strong><br />Les données réelles apparaîtront après connexion des services.</p>
          </div>
        </section>
      </div>
    </>
  )
}

function StatusCard({
  icon: Icon,
  label,
  value,
  detail,
  tone,
}: {
  icon: typeof Cloud
  label: string
  value: string
  detail: string
  tone: string
}) {
  return (
    <article className="status-card">
      <div className={`status-card-icon ${tone}`}><Icon size={19} /></div>
      <div className="status-card-label">{label}</div>
      <div className="status-card-value">{value}</div>
      <div className="status-card-detail">{detail}</div>
    </article>
  )
}

function SetupItem({
  number,
  title,
  description,
  onClick,
}: {
  number: string
  title: string
  description: string
  onClick: () => void
}) {
  return (
    <button className="setup-item" onClick={onClick}>
      <span className="setup-number">{number}</span>
      <span className="setup-copy"><strong>{title}</strong><small>{description}</small></span>
      <ChevronRight size={18} className="setup-arrow" />
    </button>
  )
}

function QuickLink({
  icon: Icon,
  title,
  description,
  onClick,
}: {
  icon: typeof Cloud
  title: string
  description: string
  onClick: () => void
}) {
  return (
    <button className="quick-link" onClick={onClick}>
      <span className="quick-icon"><Icon size={19} /></span>
      <span className="quick-copy"><strong>{title}</strong><small>{description}</small></span>
      <ArrowDownRight size={16} className="quick-arrow" />
    </button>
  )
}

function ResourcePage({
  icon: Icon,
  title,
  description,
  onAction,
  action,
}: {
  icon: typeof Cloud
  title: string
  description: string
  onAction: () => void
  action: string
}) {
  return (
    <section className="panel resource-panel">
      <div className="resource-toolbar">
        <div className="search-field">
          <Search size={17} />
          <input aria-label="Rechercher" placeholder="Rechercher dans les ressources…" disabled />
        </div>
        <button className="button button-secondary" onClick={onAction}>
          <SlidersHorizontal size={16} /> Filtres
        </button>
      </div>
      <div className="empty-state">
        <div className="empty-illustration"><Icon size={31} strokeWidth={1.5} /></div>
        <div className="empty-eyebrow">SOURCE DE DONNÉES NON CONNECTÉE</div>
        <h2>{title}</h2>
        <p>{description}</p>
        <button className="button button-primary" onClick={onAction}>
          <Plus size={17} /> {action}
        </button>
        <span className="empty-footnote"><LockKeyhole size={13} /> Aucune modification n’est enregistrée à ce stade.</span>
      </div>
    </section>
  )
}

function FilesPage({ onAction }: { onAction: () => void }) {
  return (
    <div className="files-layout">
      <section className="panel drive-panel">
        <div className="drive-logo"><Cloud size={29} /></div>
        <div className="empty-eyebrow">ESPACE DOCUMENTAIRE</div>
        <h2>Connectez votre Google Drive</h2>
        <p>Les PDF de cours, exercices, corrigés et autres ressources pourront être organisés sans exposer les identifiants Google dans le navigateur.</p>
        <div className="drive-benefits">
          <span><CheckCircle2 size={16} /> Documents classés par discipline</span>
          <span><CheckCircle2 size={16} /> Liens vers les ressources pédagogiques</span>
          <span><CheckCircle2 size={16} /> Contrôle de l’accès aux fichiers privés</span>
        </div>
        <button className="button button-primary" onClick={onAction}>
          <Cloud size={17} /> Configurer Google Drive
        </button>
        <span className="empty-footnote"><LockKeyhole size={13} /> Aucune connexion Google n’est active.</span>
      </section>
      <section className="panel file-info-panel">
        <h2>Organisation recommandée</h2>
        <p className="muted-text">La structure sera créée après validation de l’intégration.</p>
        {[
          { name: 'Programmes scolaires', desc: 'Référentiels et guides officiels', icon: GraduationCap },
          { name: 'Cours et leçons', desc: 'Documents destinés à l’apprentissage', icon: BookOpen },
          { name: 'Exercices et corrigés', desc: 'Entraînement et solutions', icon: FileText },
          { name: 'Médias pédagogiques', desc: 'Images, audio et autres supports', icon: Archive },
        ].map(item => {
          const Icon = item.icon
          return (
            <div className="folder-row" key={item.name}>
              <div className="folder-icon"><Icon size={18} /></div>
              <div><strong>{item.name}</strong><small>{item.desc}</small></div>
              <LockKeyhole size={14} className="muted-icon" />
            </div>
          )
        })}
        <p className="folder-note">Ces lignes décrivent une organisation proposée, pas des dossiers existants dans votre Drive.</p>
      </section>
    </div>
  )
}

function TeamPage({ onAction }: { onAction: () => void }) {
  return (
    <div className="team-layout">
      <section className="panel team-intro">
        <div className="team-symbol"><Users size={27} /></div>
        <h2>Vous gardez le contrôle de votre équipe.</h2>
        <p>Vous êtes le propriétaire de l’espace. Les collaborateurs auront leur propre compte et des autorisations adaptées à leur rôle.</p>
        <button className="button button-primary" onClick={onAction}><Plus size={17} /> Préparer une invitation</button>
        <span className="empty-footnote">Les invitations ne seront activées qu’après mise en place de l’authentification.</span>
      </section>
      <section className="panel role-panel">
        <h2>Rôles prévus</h2>
        <p className="muted-text">Ces rôles sont des profils proposés, pas des comptes déjà créés.</p>
        {[
          { title: 'Propriétaire', desc: 'Configuration, sécurité, comptes et publication', icon: ShieldCheck },
          { title: 'Gestionnaire pédagogique', desc: 'Organisation et validation des contenus', icon: GraduationCap },
          { title: 'Rédacteur / enseignant', desc: 'Préparation des leçons et exercices', icon: FileText },
          { title: 'Relecteur', desc: 'Consultation et vérification des ressources', icon: CheckCircle2 },
        ].map(role => {
          const Icon = role.icon
          return (
            <div className="role-row" key={role.title}>
              <div className="role-icon"><Icon size={18} /></div>
              <div><strong>{role.title}</strong><small>{role.desc}</small></div>
              <LockKeyhole size={14} className="muted-icon" />
            </div>
          )
        })}
      </section>
    </div>
  )
}

function SettingsPage({ onAction }: { onAction: (feature: string) => void }) {
  const settings = [
    { title: 'Authentification administrateur', desc: 'Connexion sécurisée et contrôle des sessions.', status: 'À configurer', icon: LockKeyhole },
    { title: 'Backend et données', desc: 'Connexion contrôlée à Supabase.', status: 'Non connecté', icon: Activity },
    { title: 'Google Drive', desc: 'Accès autorisé aux ressources documentaires.', status: 'Non connecté', icon: Cloud },
    { title: 'Journal d’activité', desc: 'Traçabilité des actions sensibles.', status: 'À mettre en place', icon: Clock3 },
  ]
  return (
    <section className="panel settings-panel">
      <div className="settings-intro">
        <div className="settings-icon"><Settings2 size={24} /></div>
        <div><h2>Configuration des services</h2><p>Les services doivent être configurés et testés avant toute mise en production.</p></div>
      </div>
      {settings.map(item => {
        const Icon = item.icon
        return (
          <button className="setting-row" key={item.title} onClick={() => onAction(item.title)}>
            <div className="setting-icon"><Icon size={19} /></div>
            <div className="setting-copy"><strong>{item.title}</strong><small>{item.desc}</small></div>
            <span className="setting-status">{item.status}</span>
            <ChevronRight size={17} className="muted-icon" />
          </button>
        )
      })}
      <div className="security-notice"><ShieldCheck size={18} /><p><strong>Important</strong><br />Aucune clé secrète ne doit être intégrée au code React public. Les opérations sensibles devront être protégées côté serveur.</p></div>
    </section>
  )
}

export default App
