# Ping

Application de gestion pour le Salon Décarbonisation (Eurexpo Lyon), composée d'un backend Quarkus, d'une base PostgreSQL et d'un frontend React.

## Démarrage rapide

```bash
cd ping
docker compose up --build
```

Cette commande construit tout les services 

### Login

Partie Admin
identifiant : admin.admin
mdp : admin 

Partie bénévole
identifiant : benevole.benevole
mdp : benevole

partie manager
identifiant : manager.buvette
mdp : buvette

### Espaces utilisateur

- **`/home`** — Tableau de bord utilisateur (`UserDashboard`) : accessible aux comptes avec le rôle `user`.
- **`/admin`** — Tableau de bord administrateur (`AdminDashboard`) : accessible aux comptes avec le rôle `admin`.
- **`/manager`** — Réutilise le tableau de bord `AdminDashboard`, accessible aux comptes avec le rôle `manager`.

Chaque route protégée vérifie la présence et la validité du token JWT ainsi que le rôle requis ; en cas d'absence de token ou de rôle incorrect, l'utilisateur est redirigé vers `/login`.

### Déconnexion

La déconnexion consiste à supprimer le token du `localStorage`, ce qui invalide l'accès aux routes protégées et renvoie l'utilisateur vers la page de connexion.

L'application sera disponible sur **http://localhost:3000** et proxifiera les appels API vers `http://localhost:8080`.
