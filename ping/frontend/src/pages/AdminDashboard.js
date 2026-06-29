import { useState, useMemo, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import './AdminDashboard.css';

const POSTES = [
  { id: 'billetterie', label: 'Billetterie', emoji: '🎫' },
  { id: 'accueil', label: 'Accueil', emoji: '👋' },
  { id: 'buvette', label: 'Buvette', emoji: '☕' },
  { id: 'coin-enfant', label: 'Coin Enfant', emoji: '👶' },
  { id: 'conference', label: 'Conférence', emoji: '🖊' },
];

function AdminDashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('creneaux');
  const [filtrePoste, setFiltrePoste] = useState('tous');
  const [creneaux, setCreneaux] = useState([]);
  const [mesCreneaux, setMesCreneaux] = useState([]);
  const [notifications] = useState(3);

  const token = localStorage.getItem('token');

  const getUserId = () => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub;
    } catch {
      return null;
    }
  };

  const fetchCreneaux = useCallback(async () => {
    try {
      const res = await fetch('/api/creneau/all', {
        headers: { 'Authorization': 'Bearer ' + token },
      });
      if (res.ok) {
        const data = await res.json();
        setCreneaux(data);
      }
    } catch (e) {
      console.error('Erreur chargement creneaux', e);
    }
  }, [token]);

  const fetchMesCreneaux = useCallback(async () => {
    try {
      const res = await fetch('/api/creneau/mes-creneaux', {
        headers: { 'Authorization': 'Bearer ' + token },
      });
      if (res.ok) {
        const data = await res.json();
        setMesCreneaux(data);
      }
    } catch (e) {
      console.error('Erreur chargement mes creneaux', e);
    }
  }, [token]);

  useEffect(() => {
    fetchCreneaux();
    fetchMesCreneaux();
  }, [fetchCreneaux, fetchMesCreneaux]);

  const username = localStorage.getItem('username') || 'admin';

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    navigate('/login');
  };

  const JOURS = ['Jeudi', 'Vendredi', 'Samedi', 'Dimanche'];
  const HORAIRES = ['08:00-12:00', '12:00-16:00', '16:00-20:00'];

  const getMesCreneauxCell = (jour, horaire) => {
    const [debut, fin] = horaire.split('-');
    return mesCreneaux.filter(
      (c) => c.jour === jour && c.heureDebut === debut && c.heureFin === fin
    );
  };

  const handleInscription = async (creneauId) => {
    const creneau = creneaux.find((c) => c.id === creneauId);
    const userId = getUserId();
    const isInscrit = creneau && creneau.isPris && creneau.prisParId === userId;
    const endpoint = isInscrit ? 'desinscription' : 'inscription';

    try {
      const res = await fetch('/api/creneau/' + creneauId + '/' + endpoint, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token,
        },
      });
      if (res.ok) {
        await fetchCreneaux();
        await fetchMesCreneaux();
      }
    } catch (e) {
      console.error('Erreur inscription', e);
    }
  };

  const creneauxFiltres = useMemo(() => {
    if (filtrePoste === 'tous') return creneaux;
    return creneaux.filter((c) => c.poste === filtrePoste);
  }, [filtrePoste, creneaux]);

  const getPoste = (id) => POSTES.find((p) => p.id === id);

  const tabs = [
    { id: 'creneaux', label: "S'inscrire aux créneaux" },
    { id: 'calendrier', label: 'Mon calendrier' },
    { id: 'disponibilites', label: 'Mes disponibilités' },
    { id: 'informations', label: 'Mes informations' },
  ];

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div className="header-left">
          <div className="header-logo">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="white">
              <path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM9 10H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2z" />
            </svg>
          </div>
          <div className="header-info">
            <h1 className="header-title">Espace Bénévole</h1>
            <span className="header-username">{username}</span>
          </div>
        </div>
        <div className="header-right">
          <button className="header-notif-btn">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="#4caf50">
              <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z" />
            </svg>
            Notifications
            {notifications > 0 && <span className="notif-badge">{notifications}</span>}
          </button>
          <button className="header-logout-btn" onClick={handleLogout}>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="#555">
              <path d="M10.09 15.59L11.5 17l5-5-5-5-1.41 1.41L12.67 11H3v2h9.67l-2.58 2.59zM19 3H5c-1.11 0-2 .9-2 2v4h2V5h14v14H5v-4H3v4c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z" />
            </svg>
            Déconnexion
          </button>
        </div>
      </header>

      <div className="dashboard-accent-bar" />

      <main className="dashboard-main">
        <nav className="dashboard-tabs">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              className={`tab-btn ${activeTab === tab.id ? 'tab-active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </nav>

        {activeTab === 'creneaux' && (
          <section className="creneaux-section">
            <h2 className="section-title">S'inscrire aux créneaux</h2>

            <div className="filtre-bar">
              <button
                className={`filtre-btn ${filtrePoste === 'tous' ? 'filtre-active' : ''}`}
                onClick={() => setFiltrePoste('tous')}
              >
                Tous les postes
              </button>
              {POSTES.map((poste) => (
                <button
                  key={poste.id}
                  className={`filtre-btn ${filtrePoste === poste.id ? 'filtre-active' : ''}`}
                  onClick={() => setFiltrePoste(poste.id)}
                >
                  {poste.emoji} {poste.label}
                </button>
              ))}
            </div>

            <div className="creneaux-list">
              {creneauxFiltres.map((creneau) => {
                const poste = getPoste(creneau.poste);
                const userId = getUserId();
                const isInscrit = creneau.isPris && creneau.prisParId === userId;
                const isPrisParAutre = creneau.isPris && creneau.prisParId !== userId;
                return (
                  <div key={creneau.id} className="creneau-card">
                    <div className="creneau-info">
                      <div className="creneau-row">
                        <span className="jour-badge">{creneau.jour}</span>
                        <span className="creneau-horaire">
                          {creneau.heureDebut} - {creneau.heureFin}
                        </span>
                      </div>
                      <span className="poste-badge">
                        {poste ? poste.emoji + ' ' + poste.label : creneau.poste}
                      </span>
                      {isPrisParAutre && (
                        <span className="pris-badge">Pris par {creneau.prisParLogin}</span>
                      )}
                    </div>
                    <button
                      className={`inscrire-btn ${isInscrit ? 'inscrit' : ''}`}
                      onClick={() => handleInscription(creneau.id)}
                      disabled={isPrisParAutre}
                    >
                      {isInscrit ? 'Inscrit' : isPrisParAutre ? 'Indisponible' : "S'inscrire"}
                    </button>
                  </div>
                );
              })}
            </div>
          </section>
        )}

        {activeTab === 'calendrier' && (
          <section className="calendrier-section">
            <h2 className="section-title">Mes créneaux réservés</h2>
            <div className="calendrier-table-wrapper">
              <table className="calendrier-table">
                <thead>
                  <tr>
                    <th>Créneaux</th>
                    {JOURS.map((jour) => (
                      <th key={jour}>{jour}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {HORAIRES.map((horaire) => (
                    <tr key={horaire}>
                      <td className="horaire-cell">{horaire}</td>
                      {JOURS.map((jour) => {
                        const creneauxCell = getMesCreneauxCell(jour, horaire);
                        return (
                          <td key={jour} className={creneauxCell.length > 0 ? 'cell-inscrit' : 'cell-vide'}>
                            {creneauxCell.length > 0
                              ? creneauxCell.map((c) => {
                                  const poste = getPoste(c.poste);
                                  return (
                                    <span key={c.id} className="cell-poste-badge">
                                      {poste ? poste.emoji + ' ' + poste.label : c.poste}
                                    </span>
                                  );
                                })
                              : '–'}
                          </td>
                        );
                      })}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {activeTab === 'disponibilites' && (
          <section className="placeholder-section">
            <h2 className="section-title">Mes disponibilités</h2>
            <p className="placeholder-text">Fonctionnalité à venir.</p>
          </section>
        )}

        {activeTab === 'informations' && (
          <section className="placeholder-section">
            <h2 className="section-title">Mes informations</h2>
            <p className="placeholder-text">Fonctionnalité à venir.</p>
          </section>
        )}
      </main>
    </div>
  );
}

export default AdminDashboard;
