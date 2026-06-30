import { useState, useMemo, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import './UserDashboard.css';

const POSTES = [
  { id: 'billetterie', label: 'Billetterie', emoji: '🎫' },
  { id: 'accueil', label: 'Accueil', emoji: '👋' },
  { id: 'buvette', label: 'Buvette', emoji: '☕' },
  { id: 'coin-enfant', label: 'Coin Enfant', emoji: '👶' },
  { id: 'conference', label: 'Conférence', emoji: '🖊' },
];

function UserDashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('creneaux');
  const [filtrePoste, setFiltrePoste] = useState('tous');
  const [creneaux, setCreneaux] = useState([]);
  const [mesCreneaux, setMesCreneaux] = useState([]);
  const [benevole, setBenevole] = useState({
    nomComplet: '',
    age: '',
    telephone: '',
    email: '',
  });
  const [resume, setResume] = useState({
    creneauxReserves: 0,
    heuresInscrites: 0,
    disponibilites: 0,
  });
  const [disponibilites, setDisponibilites] = useState([]);
  const [selectedJour, setSelectedJour] = useState('');
  const [selectedCreneau, setSelectedCreneau] = useState('');
  const [saving, setSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState('');
  const [notifications, setNotifications] = useState([]);
  const [showNotifications, setShowNotifications] = useState(false);

  const token = localStorage.getItem('token');

  const getUserId = () => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub;
    } catch {
      return null;
    }
  };

  const fetchNotifications = useCallback(async () => {
    try {
      const res = await fetch('/api/notification/me', {
        headers: { 'Authorization': 'Bearer ' + token },
      });
      if (res.ok) {
        const data = await res.json();
        setNotifications(data);
      }
    } catch (e) {
      console.error('Erreur chargement notifications', e);
    }
  }, [token]);

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

  const fetchBenevole = useCallback(async () => {
    try {
      const res = await fetch('/api/benevole/me', {
        headers: { 'Authorization': 'Bearer ' + token },
      });
      if (res.ok) {
        const data = await res.json();
        setBenevole({
          nomComplet: data.nomComplet || '',
          age: data.age || '',
          telephone: data.telephone || '',
          email: data.email || '',
        });
        setDisponibilites(data.disponibilites || []);
      }
    } catch (e) {
      console.error('Erreur chargement profil benevole', e);
    }
  }, [token]);

  useEffect(() => {
    fetchCreneaux();
    fetchMesCreneaux();
    fetchBenevole();
    fetchNotifications();
  }, [fetchCreneaux, fetchMesCreneaux, fetchBenevole, fetchNotifications]);

  useEffect(() => {
    let heures = 0;
    mesCreneaux.forEach((c) => {
      const [hd, md] = c.heureDebut.split(':').map(Number);
      const [hf, mf] = c.heureFin.split(':').map(Number);
      heures += hf - hd + (mf - md) / 60;
    });
    setResume((prev) => ({
      ...prev,
      creneauxReserves: mesCreneaux.length,
      heuresInscrites: heures,
    }));
  }, [mesCreneaux]);

  useEffect(() => {
    setResume((prev) => ({
      ...prev,
      disponibilites: disponibilites.length,
    }));
  }, [disponibilites]);

  const handleSaveBenevole = async () => {
    setSaving(true);
    setSaveMessage('');
    try {
      const res = await fetch('/api/benevole/me', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token,
        },
        body: JSON.stringify({
          nomComplet: benevole.nomComplet,
          age: benevole.age ? parseInt(benevole.age) : null,
          telephone: benevole.telephone,
          email: benevole.email,
          disponibilites: disponibilites,
        }),
      });
      if (res.ok) {
        setSaveMessage('Modifications enregistrées');
      } else {
        setSaveMessage('Erreur lors de la sauvegarde');
      }
    } catch (e) {
      setSaveMessage('Erreur lors de la sauvegarde');
    }
    setSaving(false);
  };

  const handleAddDispo = async () => {
    if (!selectedJour || !selectedCreneau) return;
    const entry = selectedJour + ' ' + selectedCreneau;
    if (disponibilites.includes(entry)) return;
    const updated = [...disponibilites, entry];
    try {
      const res = await fetch('/api/benevole/me', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token,
        },
        body: JSON.stringify({ disponibilites: updated }),
      });
      if (res.ok) {
        setDisponibilites(updated);
        setSelectedJour('');
        setSelectedCreneau('');
      }
    } catch (e) {
      console.error('Erreur ajout disponibilite', e);
    }
  };

  const handleRemoveDispo = async (index) => {
    const updated = disponibilites.filter((_, i) => i !== index);
    try {
      const res = await fetch('/api/benevole/me', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token,
        },
        body: JSON.stringify({ disponibilites: updated }),
      });
      if (res.ok) {
        setDisponibilites(updated);
      }
    } catch (e) {
      console.error('Erreur retrait disponibilite', e);
    }
  };

  const username = localStorage.getItem('username') || 'bénévole';

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
    const userId = getUserId();
    const filtered = filtrePoste === 'tous' ? creneaux : creneaux.filter((c) => c.poste === filtrePoste);
    return [...filtered].sort((a, b) => {
      const priority = (c) => {
        if (c.isPris && c.prisParId === userId) return 0;
        if (!c.isPris) return 1;
        return 2;
      };
      return priority(a) - priority(b);
    });
  }, [filtrePoste, creneaux]);

  const getPoste = (id) => POSTES.find((p) => p.id === id);

  const formatRelativeTime = (createdAt) => {
    const diff = Math.floor((Date.now() - new Date(createdAt).getTime()) / 1000);
    if (diff < 60) return "À l'instant";
    if (diff < 3600) {
      const m = Math.floor(diff / 60);
      return `Il y a ${m} minute${m > 1 ? 's' : ''}`;
    }
    if (diff < 86400) {
      const h = Math.floor(diff / 3600);
      return `Il y a ${h} heure${h > 1 ? 's' : ''}`;
    }
    const d = Math.floor(diff / 86400);
    return `Il y a ${d} jour${d > 1 ? 's' : ''}`;
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const handleOpenNotifications = () => {
    setShowNotifications(true);
  };

  const handleCloseNotifications = async () => {
    setShowNotifications(false);
    try {
      await fetch('/api/notification/me/read-all', {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + token },
      });
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (e) {
      console.error('Erreur marquage notifications lues', e);
    }
  };

  const tabs = [
    { id: 'creneaux', label: "S'inscrire aux créneaux" },
    { id: 'calendrier', label: 'Mon calendrier' },
    { id: 'disponibilites', label: 'Mes disponibilités' },
    { id: 'informations', label: 'Mes informations' },
  ];

  return (
    <div className="user-page">
      <header className="user-header">
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
          <button className="header-notif-btn" onClick={handleOpenNotifications}>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="#4caf50">
              <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z" />
            </svg>
            Notifications
            {unreadCount > 0 && <span className="notif-badge">{unreadCount}</span>}
          </button>
          <button className="header-logout-btn" onClick={handleLogout}>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="#555">
              <path d="M10.09 15.59L11.5 17l5-5-5-5-1.41 1.41L12.67 11H3v2h9.67l-2.58 2.59zM19 3H5c-1.11 0-2 .9-2 2v4h2V5h14v14H5v-4H3v4c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z" />
            </svg>
            Déconnexion
          </button>
        </div>
      </header>

      <div className="user-accent-bar" />

      {showNotifications && (
        <div className="notif-overlay" onClick={handleCloseNotifications}>
          <div className="notif-modal" onClick={(e) => e.stopPropagation()}>
            <h2 className="notif-modal-title">Notifications</h2>
            <div className="notif-list">
              {notifications.length === 0 && (
                <p className="notif-empty">Aucune notification.</p>
              )}
              {notifications.map((notif) => (
                <div key={notif.id} className="notif-item">
                  <p className="notif-item-title">{notif.title}</p>
                  <p className="notif-item-message">{notif.message}</p>
                  <p className="notif-item-time">{formatRelativeTime(notif.createdAt)}</p>
                </div>
              ))}
            </div>
            <button className="notif-close-btn" onClick={handleCloseNotifications}>Fermer</button>
          </div>
        </div>
      )}

      <main className="user-main">
        <nav className="user-tabs">
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
                      className={`inscrire-btn ${isInscrit ? 'inscrit' : isPrisParAutre ? 'indisponible' : ''}`}
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
          <section className="dispo-section">
            <h2 className="section-title">Gérer mes disponibilités</h2>

            <p className="dispo-subtitle">Ajouter une disponibilité</p>
            <div className="dispo-add-row">
              <select
                className="dispo-select"
                value={selectedJour}
                onChange={(e) => setSelectedJour(e.target.value)}
              >
                <option value="">Sélectionner un jour</option>
                {JOURS.map((j) => (
                  <option key={j} value={j}>{j}</option>
                ))}
              </select>
              <select
                className="dispo-select"
                value={selectedCreneau}
                onChange={(e) => setSelectedCreneau(e.target.value)}
              >
                <option value="">Sélectionner un créneau</option>
                {HORAIRES.map((h) => (
                  <option key={h} value={h}>{h}</option>
                ))}
              </select>
              <button
                className="dispo-add-btn"
                disabled={!selectedJour || !selectedCreneau}
                onClick={handleAddDispo}
              >
                Ajouter
              </button>
            </div>

            <p className="dispo-subtitle">Mes disponibilités déclarées</p>
            <div className="dispo-list">
              {disponibilites.length === 0 && (
                <p className="dispo-empty">Aucune disponibilité déclarée.</p>
              )}
              {disponibilites.map((entry, i) => {
                const parts = entry.split(' ');
                const jour = parts[0];
                const horaire = parts[1] || '';
                return (
                  <div key={i} className="dispo-card">
                    <div className="dispo-card-info">
                      <span className="dispo-jour-badge">{jour}</span>
                      <span className="dispo-horaire">{horaire}</span>
                    </div>
                    <button className="dispo-retirer-btn" onClick={() => handleRemoveDispo(i)}>
                      Retirer
                    </button>
                  </div>
                );
              })}
            </div>
          </section>
        )}

        {activeTab === 'informations' && (
          <section className="info-section">
            <h2 className="section-title">Mes informations personnelles</h2>

            <div className="form-group">
              <label className="form-label">Nom complet</label>
              <input
                type="text"
                className="form-input"
                value={benevole.nomComplet}
                onChange={(e) => setBenevole({ ...benevole, nomComplet: e.target.value })}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Âge</label>
                <input
                  type="number"
                  className="form-input"
                  value={benevole.age}
                  onChange={(e) => setBenevole({ ...benevole, age: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Téléphone</label>
                <input
                  type="text"
                  className="form-input"
                  value={benevole.telephone}
                  onChange={(e) => setBenevole({ ...benevole, telephone: e.target.value })}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Email</label>
              <input
                type="email"
                className="form-input"
                value={benevole.email}
                onChange={(e) => setBenevole({ ...benevole, email: e.target.value })}
              />
            </div>

            <div className="resume-box">
              <h3 className="resume-title">Résumé</h3>
              <p className="resume-line"><strong>Créneaux réservés :</strong> {resume.creneauxReserves}</p>
              <p className="resume-line"><strong>Heures inscrites :</strong> {resume.heuresInscrites}h</p>
              <p className="resume-line"><strong>Disponibilités déclarées :</strong> {resume.disponibilites}</p>
            </div>

            {saveMessage && <p className="save-message">{saveMessage}</p>}

            <button className="save-button" onClick={handleSaveBenevole} disabled={saving}>
              {saving ? 'Enregistrement...' : 'Enregistrer les modifications'}
            </button>
          </section>
        )}
      </main>
    </div>
  );
}

export default UserDashboard;
