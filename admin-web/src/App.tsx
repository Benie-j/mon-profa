import { useMemo, useRef, useState } from "react";
import {
  Activity, ArrowDownRight, ArrowUpRight, BookOpen, Check, ChevronDown,
  CircleHelp, Cloud, FileText, FolderOpen, GraduationCap, LayoutDashboard,
  Library, Menu, MoreHorizontal, Plus, Search, Settings, ShieldCheck,
  UploadCloud, Users, X, FileUp, Eye, Pencil, Send, Trash2
} from "lucide-react";

type Course = {
  id: number;
  title: string;
  subject: string;
  level: string;
  type: string;
  updated: string;
  status: "Publié" | "Brouillon";
  color: string;
  file?: string;
};

const initialCourses: Course[] = [
  { id: 1, title: "Les fonctions linéaires", subject: "Mathématiques", level: "3e", type: "Leçon PDF", updated: "Aujourd’hui", status: "Publié", color: "violet" },
  { id: 2, title: "La Seconde Guerre mondiale", subject: "Histoire-Géographie", level: "3e", type: "Cours", updated: "Hier", status: "Publié", color: "orange" },
  { id: 3, title: "Les figures de style", subject: "Français", level: "2nde", type: "Leçon PDF", updated: "12 oct. 2026", status: "Brouillon", color: "blue" },
  { id: 4, title: "Électricité : loi d’Ohm", subject: "Physique-Chimie", level: "3e", type: "Cours + exercices", updated: "10 oct. 2026", status: "Publié", color: "green" },
  { id: 5, title: "Équations du premier degré", subject: "Mathématiques", level: "4e", type: "Exercices corrigés", updated: "8 oct. 2026", status: "Brouillon", color: "violet" }
];

const navItems = [
  { label: "Vue d’ensemble", icon: LayoutDashboard },
  { label: "Cours & leçons", icon: BookOpen },
  { label: "Disciplines", icon: Library },
  { label: "Classes", icon: GraduationCap },
  { label: "Fichiers", icon: FolderOpen }
];

function App() {
  const [courses, setCourses] = useState(initialCourses);
  const [page, setPage] = useState("Vue d’ensemble");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("Tous");
  const [showModal, setShowModal] = useState(false);
  const [mobileMenu, setMobileMenu] = useState(false);
  const [notice, setNotice] = useState("");
  const [title, setTitle] = useState("");
  const [subject, setSubject] = useState("Mathématiques");
  const [level, setLevel] = useState("3e");
  const [file, setFile] = useState<File | null>(null);
  const [publishNow, setPublishNow] = useState(false);
  const fileRef = useRef<HTMLInputElement>(null);

  const visibleCourses = useMemo(() => courses.filter((course) => {
    const term = search.trim().toLowerCase();
    const matchesTerm = !term || [course.title, course.subject, course.level].some(v => v.toLowerCase().includes(term));
    const matchesStatus = statusFilter === "Tous" || course.status === statusFilter;
    const matchesPage = page !== "Disciplines" || courses.some(c => c.subject === course.subject);
    return matchesTerm && matchesStatus && matchesPage;
  }), [courses, search, statusFilter, page]);

  const published = courses.filter(c => c.status === "Publié").length;
  const drafts = courses.filter(c => c.status === "Brouillon").length;

  function notify(message: string) {
    setNotice(message);
    window.setTimeout(() => setNotice(""), 3500);
  }

  function createCourse(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!title.trim()) return;
    const newCourse: Course = {
      id: Date.now(),
      title: title.trim(),
      subject,
      level,
      type: file ? file.name : "Contenu à compléter",
      updated: "À l’instant",
      status: publishNow ? "Publié" : "Brouillon",
      color: subject === "Français" ? "blue" : subject === "Physique-Chimie" ? "green" : subject === "Histoire-Géographie" ? "orange" : "violet",
      file: file?.name
    };
    setCourses(prev => [newCourse, ...prev]);
    setTitle("");
    setFile(null);
    setPublishNow(false);
    setShowModal(false);
    setPage("Cours & leçons");
    notify("Cours ajouté à la liste locale. Aucun envoi vers Drive n’a été effectué.");
  }

  function togglePublish(id: number) {
    setCourses(prev => prev.map(c => c.id === id ? { ...c, status: c.status === "Publié" ? "Brouillon" : "Publié" } : c));
    notify("Statut modifié localement. La publication réelle sera activée après intégration du stockage.");
  }

  function deleteCourse(id: number) {
    setCourses(prev => prev.filter(c => c.id !== id));
    notify("Élément supprimé de la liste locale.");
  }

  const isCoursesPage = page === "Cours & leçons" || page === "Disciplines" || page === "Fichiers";

  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileMenu ? "sidebar-open" : ""}`}>
        <div className="brand">
          <div className="brand-mark"><GraduationCap size={23} /></div>
          <div><div className="brand-name">KALYNO<span>.</span></div><div className="brand-sub">ESPACE ADMIN</div></div>
          <button className="icon-btn mobile-close" onClick={() => setMobileMenu(false)} aria-label="Fermer le menu"><X size={19}/></button>
        </div>
        <div className="workspace-label">ESPACE DE TRAVAIL</div>
        <nav className="nav-list">
          {navItems.map(item => {
            const Icon = item.icon;
            return <button key={item.label} className={`nav-item ${page === item.label ? "active" : ""}`} onClick={() => { setPage(item.label); setMobileMenu(false); }}>
              <Icon size={18} strokeWidth={1.9}/><span>{item.label}</span>
              {item.label === "Cours & leçons" && <span className="nav-count">{courses.length}</span>}
            </button>;
          })}
        </nav>
        <div className="sidebar-separator" />
        <div className="workspace-label">CONFIGURATION</div>
        <button className={`nav-item ${page === "Paramètres" ? "active" : ""}`} onClick={() => setPage("Paramètres")}><Settings size={18}/><span>Paramètres</span></button>
        <div className="sidebar-spacer" />
        <div className="storage-card">
          <div className="storage-icon"><Cloud size={18}/></div>
          <div className="storage-title">Bibliothèque distante</div>
          <p>Connecte Google Drive pour gérer les documents de tes élèves.</p>
          <button className="storage-button" onClick={() => notify("La connexion Google Drive sera configurée à l’étape OAuth.")}>Configurer Drive <ArrowUpRight size={14}/></button>
          <div className="storage-status"><span/> Drive non connecté</div>
        </div>
        <div className="sidebar-footer"><div className="avatar">K</div><div className="footer-user"><strong>Administrateur</strong><span>Configuration locale</span></div><MoreHorizontal size={19} className="muted"/></div>
      </aside>

      {mobileMenu && <button className="scrim" aria-label="Fermer le menu" onClick={() => setMobileMenu(false)} />}

      <main className="main-area">
        <header className="topbar">
          <button className="icon-btn menu-toggle" onClick={() => setMobileMenu(true)} aria-label="Ouvrir le menu"><Menu size={21}/></button>
          <div className="breadcrumb"><span>Administration</span><span className="crumb-slash">/</span><strong>{page}</strong></div>
          <div className="topbar-right">
            <div className="secure-label"><ShieldCheck size={15}/> Espace sécurisé à configurer</div>
            <div className="top-avatar">K</div>
          </div>
        </header>

        <div className="page-content">
          <div className="welcome-row">
            <div>
              <div className="eyebrow"><span className="eyebrow-dot"/> LE SAVOIR OUVRE L’AVENIR</div>
              <h1>{page === "Vue d’ensemble" ? "Bonjour, bienvenue 👋" : page}</h1>
              <p className="page-subtitle">{page === "Vue d’ensemble" ? "Prépare et organise les ressources pédagogiques de KALYNO." : page === "Cours & leçons" ? "Crée, organise et prépare les contenus pour les élèves." : page === "Disciplines" ? "Retrouve les disciplines présentes dans ta bibliothèque." : page === "Fichiers" ? "Consulte les documents associés à tes cours." : page === "Classes" ? "Organise les ressources par niveau scolaire." : "Configure les éléments de ton espace d’administration."}</p>
            </div>
            <button className="primary-btn" onClick={() => setShowModal(true)}><Plus size={18}/> Nouveau cours</button>
          </div>

          {page === "Vue d’ensemble" && <>
            <div className="stats-grid">
              <StatCard icon={<BookOpen size={19}/>} label="Cours enregistrés" value={String(courses.length).padStart(2,"0")} note="Dans cette session" tone="purple" trend="Bibliothèque locale"/>
              <StatCard icon={<Send size={19}/>} label="Cours publiés" value={String(published).padStart(2,"0")} note="Prêts pour les élèves" tone="green" trend="Statut simulé"/>
              <StatCard icon={<FileText size={19}/>} label="Brouillons" value={String(drafts).padStart(2,"0")} note="À terminer ou réviser" tone="orange" trend="À finaliser"/>
              <StatCard icon={<Users size={19}/>} label="Disciplines" value={String(new Set(courses.map(c=>c.subject)).size).padStart(2,"0")} note="Avec du contenu" tone="blue" trend="Toutes classes"/>
            </div>

            <div className="section-grid">
              <section className="panel recent-panel">
                <div className="panel-heading"><div><h2>Contenus récents</h2><p>Les derniers éléments de ta bibliothèque</p></div><button className="text-button" onClick={() => setPage("Cours & leçons")}>Tout voir <ArrowUpRight size={15}/></button></div>
                <CourseTable courses={courses.slice(0,4)} onToggle={togglePublish} onDelete={deleteCourse} compact/>
              </section>
              <section className="panel quick-panel">
                <div className="panel-heading"><div><h2>Actions rapides</h2><p>Gagne du temps au quotidien</p></div><div className="heading-icon"><Activity size={17}/></div></div>
                <button className="quick-action" onClick={() => setShowModal(true)}><div className="quick-icon purple"><Plus size={18}/></div><div><strong>Créer un cours</strong><span>Ajouter une nouvelle ressource</span></div><ArrowUpRight size={16} className="quick-arrow"/></button>
                <button className="quick-action" onClick={() => {setPage("Cours & leçons"); setStatusFilter("Brouillon");}}><div className="quick-icon amber"><Pencil size={18}/></div><div><strong>Reprendre les brouillons</strong><span>{drafts} contenu(s) à finaliser</span></div><ArrowUpRight size={16} className="quick-arrow"/></button>
                <button className="quick-action" onClick={() => notify("Google Drive sera disponible après configuration OAuth.")}><div className="quick-icon blue"><UploadCloud size={18}/></div><div><strong>Importer des documents</strong><span>Préparer PDF et supports</span></div><ArrowUpRight size={16} className="quick-arrow"/></button>
                <div className="tip-card"><CircleHelp size={17}/><div><strong>Bon à savoir</strong><p>Vérifie les exercices et corrigés avant de publier un cours destiné aux élèves.</p></div></div>
              </section>
            </div>

            <section className="drive-banner">
              <div className="drive-banner-icon"><Cloud size={25}/></div>
              <div className="drive-banner-copy"><strong>Tes cours, au même endroit.</strong><p>La connexion Google Drive permettra de conserver tes PDF, leçons et corrigés sans alourdir l’application Android.</p></div>
              <button className="secondary-btn" onClick={() => notify("Étape suivante : configuration OAuth Google Drive.")}>Préparer la connexion <ArrowUpRight size={15}/></button>
            </section>
          </>}

          {isCoursesPage && <section className="panel library-panel">
            <div className="panel-heading library-heading"><div><h2>{page === "Disciplines" ? "Bibliothèque par discipline" : page === "Fichiers" ? "Documents des cours" : "Tous les cours"}</h2><p>{visibleCourses.length} élément(s) affiché(s) · données de démonstration locales</p></div><button className="outline-btn" onClick={() => setShowModal(true)}><Plus size={16}/> Ajouter</button></div>
            <div className="filter-row">
              <div className="search-box"><Search size={17}/><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Rechercher un cours, une discipline..." /></div>
              <div className="filter-select"><select value={statusFilter} onChange={e => setStatusFilter(e.target.value)} aria-label="Filtrer par statut"><option>Tous</option><option>Publié</option><option>Brouillon</option></select><ChevronDown size={15}/></div>
            </div>
            <CourseTable courses={visibleCourses} onToggle={togglePublish} onDelete={deleteCourse}/>
            {visibleCourses.length === 0 && <div className="empty-state"><FolderOpen size={27}/><strong>Aucun cours trouvé</strong><span>Essaie un autre mot-clé ou change le filtre.</span></div>}
          </section>}

          {page === "Classes" && <section className="class-grid">
            {["6e","5e","4e","3e","2nde","1re","Terminale"].map((level, i) => <button className="class-card" key={level} onClick={() => {setSearch(level);setPage("Cours & leçons");}}><div className={`class-symbol tone-${i%4}`}><GraduationCap size={21}/></div><strong>{level}</strong><span>{courses.filter(c=>c.level===level).length} cours dans la bibliothèque</span><ArrowUpRight size={16}/></button>)}
          </section>}

          {page === "Paramètres" && <section className="panel settings-panel"><div className="panel-heading"><div><h2>Configuration du site</h2><p>Les connexions externes ne sont pas encore activées.</p></div></div><div className="setting-row"><div className="setting-symbol"><Cloud size={19}/></div><div className="setting-copy"><strong>Google Drive</strong><span>Connexion OAuth et choix du dossier des cours</span></div><span className="pill pill-draft">Non connecté</span><button className="outline-btn" onClick={() => notify("La configuration OAuth sera ajoutée après la validation de l’interface.")}>Configurer</button></div><div className="setting-row"><div className="setting-symbol"><ShieldCheck size={19}/></div><div className="setting-copy"><strong>Accès administrateur</strong><span>Une authentification réelle est nécessaire avant le déploiement public.</span></div><span className="pill pill-draft">À configurer</span></div><div className="setting-row"><div className="setting-symbol"><Activity size={19}/></div><div className="setting-copy"><strong>Supabase élèves</strong><span>Le backend actuel n’a pas été modifié.</span></div><span className="pill pill-published">Préservé</span></div></section>}

          <footer className="page-footer"><span>© 2026 KALYNO · Le savoir ouvre l’avenir</span><span><span className="footer-dot"/> Prototype d’interface · Connexions à configurer</span></footer>
        </div>
      </main>

      {showModal && <div className="modal-backdrop" onMouseDown={e => {if (e.target === e.currentTarget) setShowModal(false);}}>
        <form className="course-modal" onSubmit={createCourse}>
          <div className="modal-header"><div><div className="modal-kicker">BIBLIOTHÈQUE PÉDAGOGIQUE</div><h2>Ajouter un cours</h2><p>Prépare une ressource pour les élèves de KALYNO.</p></div><button type="button" className="icon-btn" onClick={() => setShowModal(false)} aria-label="Fermer"><X size={19}/></button></div>
          <label className="field-label">Titre du cours <span>*</span><input autoFocus required value={title} onChange={e=>setTitle(e.target.value)} placeholder="Ex. : Les nombres rationnels"/></label>
          <div className="form-two-col"><label className="field-label">Discipline<select value={subject} onChange={e=>setSubject(e.target.value)}><option>Mathématiques</option><option>Français</option><option>Histoire-Géographie</option><option>Physique-Chimie</option><option>SVT</option><option>Anglais</option></select></label><label className="field-label">Classe<select value={level} onChange={e=>setLevel(e.target.value)}>{["6e","5e","4e","3e","2nde","1re","Terminale"].map(v=><option key={v}>{v}</option>)}</select></label></div>
          <div className="field-label">Document associé <span className="optional">(facultatif)</span></div>
          <button type="button" className="upload-zone" onClick={()=>fileRef.current?.click()}><div className="upload-symbol"><FileUp size={22}/></div><strong>{file ? file.name : "Choisir un fichier"}</strong><span>{file ? `${(file.size/1024/1024).toFixed(2)} Mo · sélection locale` : "PDF, DOCX, PPTX ou audio · fichier non envoyé"}</span><input ref={fileRef} type="file" hidden accept=".pdf,.doc,.docx,.ppt,.pptx,.mp3,.m4a,.wav,.jpg,.jpeg,.png" onChange={e=>setFile(e.target.files?.[0] ?? null)}/></button>
          <label className="publish-option"><input type="checkbox" checked={publishNow} onChange={e=>setPublishNow(e.target.checked)}/><span><strong>Marquer comme publié</strong><small>Ce choix est seulement local dans cette version de démonstration.</small></span></label>
          <div className="modal-note"><ShieldCheck size={16}/><span>Le document ne sera pas transféré vers Google Drive à cette étape.</span></div>
          <div className="modal-actions"><button type="button" className="cancel-btn" onClick={()=>setShowModal(false)}>Annuler</button><button type="submit" className="primary-btn"><Check size={17}/> Enregistrer le cours</button></div>
        </form>
      </div>}

      {notice && <div className="toast"><Check size={17}/><span>{notice}</span><button onClick={()=>setNotice("")} aria-label="Fermer"><X size={15}/></button></div>}
    </div>
  );
}

function StatCard({icon,label,value,note,tone,trend}:{icon:React.ReactNode;label:string;value:string;note:string;tone:string;trend:string}) {
  return <div className="stat-card"><div className="stat-top"><div className={`stat-icon ${tone}`}>{icon}</div><span className="stat-trend">{tone==="green"?<ArrowUpRight size={14}/>:tone==="orange"?<ArrowDownRight size={14}/>:<Activity size={14}/>} {trend}</span></div><div className="stat-label">{label}</div><div className="stat-bottom"><strong>{value}</strong><span>{note}</span></div></div>;
}

function CourseTable({courses,onToggle,onDelete,compact=false}:{courses:Course[];onToggle:(id:number)=>void;onDelete:(id:number)=>void;compact?:boolean}) {
  return <div className="table-scroll"><table className="course-table"><thead><tr><th>CONTENU</th><th>CLASSE</th>{!compact&&<th>MODIFIÉ</th>}<th>STATUT</th><th></th></tr></thead><tbody>{courses.map(course=><tr key={course.id}><td><div className="course-cell"><div className={`course-file ${course.color}`}><FileText size={18}/></div><div className="course-info"><strong>{course.title}</strong><span>{course.subject} · {course.type}</span></div></div></td><td><span className="level-pill">{course.level}</span></td>{!compact&&<td className="date-cell">{course.updated}</td>}<td><button className={`pill ${course.status==="Publié"?"pill-published":"pill-draft"}`} onClick={()=>onToggle(course.id)} title="Changer le statut"><span/>{course.status}</button></td><td><div className="row-actions"><button title="Détails" onClick={()=>window.alert(`${course.title}\n${course.subject} · ${course.level}\n${course.status}\n\nCette fiche est actuellement locale.`)}><Eye size={15}/></button><button title="Supprimer de la liste locale" onClick={()=>onDelete(course.id)}><Trash2 size={15}/></button></div></td></tr>)}</tbody></table></div>;
}

export default App;
