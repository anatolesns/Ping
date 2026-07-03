# Ping

Application de gestion pour le Salon Décarbonisation (Eurexpo Lyon), composée d'un backend Quarkus, d'une base PostgreSQL et d'un frontend React.

## Démarrage rapide

Prérequis : [Docker](https://www.docker.com/) et Docker Compose.

```bash
cd ping
docker compose up --build
```

Cette commande construit et tout les services 

### Authentification


### Login

Partie Admin
identifiant : admin.admin
mdp : admin 

Partie bénévole
identifiant : benevole.benevole
mdp : benevole

- La page d'accueil est **`/login`**. L'utilisateur s'y connecte avec un identifiant et un mot de passe.
- Selon le(s) rôle(s) présents dans le token, l'utilisateur est redirigé automatiquement :
  - `admin` → `/admin`
  - `manager` → `/manager`
  - autre (`user`) → `/home`

### Espaces utilisateur

- **`/home`** — Tableau de bord utilisateur (`UserDashboard`) : accessible aux comptes avec le rôle `user`.
- **`/admin`** — Tableau de bord administrateur (`AdminDashboard`) : accessible aux comptes avec le rôle `admin`.
- **`/manager`** — Réutilise le tableau de bord `AdminDashboard`, accessible aux comptes avec le rôle `manager`.

Chaque route protégée vérifie la présence et la validité du token JWT ainsi que le rôle requis ; en cas d'absence de token ou de rôle incorrect, l'utilisateur est redirigé vers `/login`.

### Déconnexion

La déconnexion consiste à supprimer le token du `localStorage`, ce qui invalide l'accès aux routes protégées et renvoie l'utilisateur vers la page de connexion.

L'application sera disponible sur **http://localhost:3000** et proxifiera les appels API vers `http://localhost:8080` (backend à lancer séparément, par exemple via `docker compose up db app`).
