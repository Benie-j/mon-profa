# KALYNO — Administration web

Interface React + TypeScript + Vite pour préparer la gestion des contenus pédagogiques.

## Démarrer dans Termux

```bash
npm install
npm run dev
```

Vite affiche l'adresse locale. Sur le téléphone, ouvre `http://127.0.0.1:5173`.

## Vérifier la compilation

```bash
npm run build
```

## État actuel

- Interface responsive avec tableau de bord.
- Liste de cours d'exemple, recherche et filtre de statut.
- Formulaire de création et sélection locale d'un fichier.
- Changement de statut et suppression uniquement dans l'état React.
- Aucune connexion Google Drive ou Supabase.
- Aucune authentification administrateur réelle : ne pas déployer publiquement en l'état.
- Les fichiers sélectionnés ne sont pas téléversés et les changements ne sont pas persistés.

Étapes suivantes : configurer OAuth Google Drive, définir le dossier Drive des ressources, protéger l'accès administrateur et relier le catalogue publié à l'application Android sans modifier le système d'authentification des élèves.
