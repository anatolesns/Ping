import { useState, useMemo, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import './AdminDashboard.css';

const POSTES = [
  { id: 'billetterie', label: 'Billetterie' },
  { id: 'accueil', label: 'Accueil' },
  { id: 'buvette', label: 'Buvette' },
  { id: 'coin-enfant', label: 'Coin Enfant' },
  { id: 'conference', label: 'Conférence' },
];

const JOURS = ['Jeudi', 'Vendredi', 'Samedi', 'Dimanche'];
const HORAIRES = ['08:00-12:00', '12:00-16:00', '16:00-20:00'];

const TABS = [
  { id: 'planning-poste', label: 'Planning par Poste', colorClass: 'mgr-tab-poste' },
  { id: 'planning-benevole', label: 'Planning par Bénévole', colorClass: 'mgr-tab-benevole' },
  { id: 'gestion-benevoles', label: 'Gestion des Bénévoles', colorClass: 'mgr-tab-gestion' },
];

function AdminDashboard() {
  const navigate = useNavigate();

  const token = localStorage.getItem('token');
  const username = localStorage.getItem('username') || 'manager';

  const managerPoste = (() => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const groups = payload.groups || [];
      const g = groups.find((gr) => gr.startsWith('manager_'));
      return g ? g.replace('manager_', '') : null;
    } catch {
      return null;
    }
  })();

  const managerPosteLabel = managerPoste
    ? (POSTES.find((p) => p.id === managerPoste)?.label || managerPoste)
    : null;

  const [activeTab, setActiveTab] = useState('planning-poste');
  const [filtrePoste, setFiltrePoste] = useState(managerPoste || 'tous');
  const [creneaux, setCreneaux] = useState([]);
  const [benevoles, setBenevoles] = useState([]);
  const [openInfoId, setOpenInfoId] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const [assignTarget, setAssignTarget] = useState(null);
  const [assigning, setAssigning] = useState(false);

  const [gbEditingId, setGbEditingId] = useState(null);
  const [gbNomComplet, setGbNomComplet] = useState('');
  const [gbAge, setGbAge] = useState('');
  const [gbTelephone, setGbTelephone] = useState('');
  const [gbEmail, setGbEmail] = useState('');
  const [gbDisponibilites, setGbDisponibilites] = useState([]);
  const [gbDispoJour, setGbDispoJour] = useState('');
  const [gbDispoCreneau, setGbDispoCreneau] = useState('');
  const [gbSaving, setGbSaving] = useState(false);
  const [gbError, setGbError] = useState('');

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

  const fetchBenevoles = useCallback(async () => {
    try {
      const res = await fetch('/api/benevole/all', {
        headers: { 'Authorization': 'Bearer ' + token },
      });
      if (res.ok) {
        const data = await res.json();
        setBenevoles(data);
      }
    } catch (e) {
      console.error('Erreur chargement benevoles', e);
    }
  }, [token]);

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

  useEffect(() => {
    fetchCreneaux();
    fetchBenevoles();
    fetchNotifications();
  }, [fetchCreneaux, fetchBenevoles, fetchNotifications]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    navigate('/login');
  };

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

  const handleRemove = async (creneauId) => {
    try {
      const res = await fetch('/api/creneau/' + creneauId + '/desinscription', {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + token },
      });
      if (res.ok) {
        await fetchCreneaux();
      }
    } catch (e) {
      console.error('Erreur retrait benevole', e);
    }
  };

  const openAssignModal = (jour, horaire, poste) => {
    setAssignTarget({ jour, horaire, poste });
  };

  const closeAssignModal = () => {
    if (assigning) return;
    setAssignTarget(null);
  };

  const getAvailableBenevoles = (jour, horaire) => {
    const [debut, fin] = horaire.split('-');
    const dispoKey = jour + ' ' + horaire;
    const occupes = new Set(
      creneaux
        .filter((c) => c.isPris && c.prisParId && c.jour === jour && c.heureDebut === debut && c.heureFin === fin)
        .map((c) => c.prisParId)
    );
    return benevoles.filter(
      (b) => (b.disponibilites || []).includes(dispoKey) && !occupes.has(b.id)
    );
  };

  const handleAssign = async (benevoleId) => {
    if (!assignTarget) return;
    setAssigning(true);
    try {
      const { jour, horaire, poste } = assignTarget;
      const [heureDebut, heureFin] = horaire.split('-');
      const existants = getCreneauxCell(jour, horaire, poste);
      let creneauId = existants.length > 0 ? existants[0].id : null;

      if (!creneauId) {
        const createRes = await fetch('/api/creneau', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token,
          },
          body: JSON.stringify({ jour, heureDebut, heureFin, poste }),
        });
        if (!createRes.ok) throw new Error('Erreur lors de la création du créneau');
        const created = await createRes.json();
        creneauId = created.id;
      }

      const assignRes = await fetch('/api/creneau/' + creneauId + '/assign/' + benevoleId, {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + token },
      });
      if (!assignRes.ok) throw new Error('Erreur lors de l\'assignation du bénévole');

      await fetchCreneaux();
      setAssignTarget(null);
    } catch (e) {
      console.error('Erreur assignation benevole', e);
    }
    setAssigning(false);
  };

  const slugifyLogin = (nomComplet) => {
    const clean = nomComplet
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .toLowerCase()
      .replace(/[^a-z0-9\s]/g, '')
      .trim()
      .split(/\s+/);
    if (clean.length >= 2) {
      return clean[0] + '.' + clean[clean.length - 1];
    }
    return (clean[0] || 'benevole') + '.' + Math.floor(Math.random() * 1000);
  };

  const generatePassword = () => Math.random().toString(36).slice(-10);

  const resetGbForm = () => {
    setGbEditingId(null);
    setGbNomComplet('');
    setGbAge('');
    setGbTelephone('');
    setGbEmail('');
    setGbDisponibilites([]);
    setGbDispoJour('');
    setGbDispoCreneau('');
    setGbError('');
  };

  const startEditBenevole = (b) => {
    setGbEditingId(b.id);
    setGbNomComplet(b.nomComplet || '');
    setGbAge(b.age != null ? String(b.age) : '');
    setGbTelephone(b.telephone || '');
    setGbEmail(b.email || '');
    setGbDisponibilites(b.disponibilites || []);
    setGbDispoJour('');
    setGbDispoCreneau('');
    setGbError('');
  };

  const handleAddGbDispo = () => {
    if (!gbDispoJour || !gbDispoCreneau) return;
    const entry = gbDispoJour + ' ' + gbDispoCreneau;
    if (gbDisponibilites.includes(entry)) return;
    setGbDisponibilites([...gbDisponibilites, entry]);
    setGbDispoJour('');
    setGbDispoCreneau('');
  };

  const handleRemoveGbDispo = (index) => {
    setGbDisponibilites(gbDisponibilites.filter((_, i) => i !== index));
  };

  const handleSubmitGb = async () => {
    if (!gbNomComplet.trim() || !gbAge || !gbTelephone.trim()) {
      setGbError('Nom complet, âge et téléphone sont obligatoires.');
      return;
    }
    setGbSaving(true);
    setGbError('');
    try {
      const profileBody = {
        nomComplet: gbNomComplet.trim(),
        age: parseInt(gbAge, 10),
        telephone: gbTelephone.trim(),
        email: gbEmail.trim() || null,
        disponibilites: gbDisponibilites,
      };

      if (gbEditingId) {
        const res = await fetch('/api/benevole/' + gbEditingId, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token,
          },
          body: JSON.stringify(profileBody),
        });
        if (!res.ok) throw new Error('Erreur lors de la mise à jour du bénévole');
      } else {
        let login = slugifyLogin(gbNomComplet);
        const password = generatePassword();
        let createRes = await fetch('/api/user', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token,
          },
          body: JSON.stringify({ login, password, isAdmin: false }),
        });
        if (createRes.status === 409) {
          login = login + Math.floor(Math.random() * 1000);
          createRes = await fetch('/api/user', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': 'Bearer ' + token,
            },
            body: JSON.stringify({ login, password, isAdmin: false }),
          });
        }
        if (!createRes.ok) throw new Error('Erreur lors de la création du compte');
        const userData = await createRes.json();

        const profileRes = await fetch('/api/benevole/' + userData.id, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + token,
          },
          body: JSON.stringify(profileBody),
        });
        if (!profileRes.ok) throw new Error('Erreur lors de la création du profil bénévole');

        console.log('Bénévole créé — login : ' + login + ' / mot de passe : ' + password);
      }

      resetGbForm();
      await fetchBenevoles();
    } catch (e) {
      setGbError(e.message || 'Erreur lors de l\'enregistrement');
    }
    setGbSaving(false);
  };

  const handleDeleteBenevole = async (b) => {
    if (!window.confirm('Supprimer ' + b.nomComplet + ' ?')) return;
    try {
      const benevoleCreneaux = creneaux.filter((c) => c.isPris && c.prisParId === b.id);
      for (const c of benevoleCreneaux) {
        await fetch('/api/creneau/' + c.id + '/desinscription', {
          method: 'PUT',
          headers: { 'Authorization': 'Bearer ' + token },
        });
      }
      await fetch('/api/benevole/' + b.id, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token },
      });
      await fetch('/api/user/' + b.id, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token },
      });
      if (gbEditingId === b.id) resetGbForm();
      await fetchBenevoles();
      await fetchCreneaux();
    } catch (e) {
      console.error('Erreur suppression benevole', e);
    }
  };

  const postesAffiches = useMemo(() => {
    if (filtrePoste === 'tous') return POSTES;
    return POSTES.filter((p) => p.id === filtrePoste);
  }, [filtrePoste]);

  const getCreneauxCell = (jour, horaire, posteId) => {
    const [debut, fin] = horaire.split('-');
    return creneaux.filter(
      (c) => c.jour === jour && c.heureDebut === debut && c.heureFin === fin && c.poste === posteId
    );
  };

  const getPosteLabel = (id) => {
    const poste = POSTES.find((p) => p.id === id);
    return poste ? poste.label : id;
  };

  const benevolesParId = useMemo(() => {
    const map = {};
    benevoles.forEach((b) => { map[b.id] = b; });
    return map;
  }, [benevoles]);

  const planningParBenevole = useMemo(() => {
    const groupes = {};
    creneaux
      .filter((c) => c.isPris && c.prisParId && (!managerPoste || c.poste === managerPoste))
      .forEach((c) => {
        if (!groupes[c.prisParId]) {
          groupes[c.prisParId] = {
            id: c.prisParId,
            login: c.prisParLogin,
            creneaux: [],
          };
        }
        groupes[c.prisParId].creneaux.push(c);
      });

    return Object.values(groupes)
      .map((g) => {
        const heures = g.creneaux.reduce((total, c) => {
          const [hd, md] = c.heureDebut.split(':').map(Number);
          const [hf, mf] = c.heureFin.split(':').map(Number);
          return total + (hf - hd) + (mf - md) / 60;
        }, 0);
        const profil = benevolesParId[g.id];
        return {
          ...g,
          nom: (profil && profil.nomComplet) || g.login,
          heures,
          profil,
        };
      })
      .sort((a, b) => a.nom.localeCompare(b.nom));
  }, [creneaux, benevolesParId]);

  return (
    <div className="mgr-page">
      <header className="mgr-header">
        <div className="mgr-header-left">
          <div className="mgr-logo">
            <svg viewBox="0 0 24 24" width="26" height="26" fill="white">
              <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z" />
            </svg>
          </div>
          <div className="mgr-header-info">
            <h1 className="mgr-title">{managerPosteLabel ? `Manager ${managerPosteLabel}` : 'Espace Manager'}</h1>
            <span className="mgr-username">{username}</span>
          </div>
        </div>
        <div className="mgr-header-right">
          <button className="mgr-notif-btn" onClick={handleOpenNotifications}>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="#4caf50">
              <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z" />
            </svg>
            Notifications
            {unreadCount > 0 && <span className="mgr-notif-badge">{unreadCount}</span>}
          </button>
          <button className="mgr-logout-btn" onClick={handleLogout}>
            <svg viewBox="0 0 24 24" width="18" height="18" fill="#555">
              <path d="M10.09 15.59L11.5 17l5-5-5-5-1.41 1.41L12.67 11H3v2h9.67l-2.58 2.59zM19 3H5c-1.11 0-2 .9-2 2v4h2V5h14v14H5v-4H3v4c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z" />
            </svg>
            Déconnexion
          </button>
        </div>
      </header>

      <div className="mgr-content-wrapper">
        <div className="mgr-card">
          <nav className="mgr-tabs">
            {TABS.filter((tab) => !managerPoste || tab.id !== 'gestion-benevoles').map((tab) => (
              <button
                key={tab.id}
                className={`mgr-tab-btn ${tab.colorClass} ${activeTab === tab.id ? 'mgr-tab-active' : ''}`}
                onClick={() => setActiveTab(tab.id)}
              >
                {tab.id === 'planning-poste' && (
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                    <path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z" />
                  </svg>
                )}
                {tab.id === 'planning-benevole' && (
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                    <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                  </svg>
                )}
                {tab.id === 'gestion-benevoles' && (
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                  </svg>
                )}
                {tab.label}
              </button>
            ))}
          </nav>

          <div className="mgr-card-divider" />

          <div className="mgr-card-body">
            {activeTab === 'planning-poste' && (
              <section>
                <h2 className="mgr-section-title">Calendrier de la Semaine</h2>

                {!managerPoste && (
                  <div className="mgr-filtre-bar">
                    <button
                      className={`mgr-filtre-btn ${filtrePoste === 'tous' ? 'mgr-filtre-active' : ''}`}
                      onClick={() => setFiltrePoste('tous')}
                    >
                      Tous les postes
                    </button>
                    {POSTES.map((poste) => (
                      <button
                        key={poste.id}
                        className={`mgr-filtre-btn ${filtrePoste === poste.id ? 'mgr-filtre-active' : ''}`}
                        onClick={() => setFiltrePoste(poste.id)}
                      >
                        {poste.label}
                      </button>
                    ))}
                  </div>
                )}

                <div className="mgr-table-wrapper">
                  <table className="mgr-table">
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
                          <td className="mgr-horaire-cell">{horaire}</td>
                          {JOURS.map((jour) => (
                            <td key={jour} className="mgr-day-cell">
                              {postesAffiches.map((poste) => {
                                const cellCreneaux = getCreneauxCell(jour, horaire, poste.id);
                                const inscrits = cellCreneaux.filter((c) => c.isPris);
                                return (
                                  <div
                                    key={poste.id}
                                    className={`mgr-poste-card ${inscrits.length > 0 ? 'mgr-poste-filled' : ''}`}
                                    onClick={() => openAssignModal(jour, horaire, poste.id)}
                                    role="button"
                                    tabIndex={0}
                                  >
                                    <div className="mgr-poste-header">
                                      <span className="mgr-poste-badge">{poste.label}</span>
                                      <span className="mgr-poste-count">{inscrits.length}</span>
                                    </div>
                                    {inscrits.length === 0 ? (
                                      <span className="mgr-vide-text">Vide</span>
                                    ) : (
                                      inscrits.map((c) => (
                                        <div key={c.id} className="mgr-benevole-row">
                                          <span className="mgr-benevole-name">
                                            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                                              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                                            </svg>
                                            {c.prisParLogin}
                                          </span>
                                          <button
                                            className="mgr-remove-btn"
                                            onClick={(e) => { e.stopPropagation(); handleRemove(c.id); }}
                                            title="Retirer ce bénévole"
                                          >
                                            ✕
                                          </button>
                                        </div>
                                      ))
                                    )}
                                  </div>
                                );
                              })}
                            </td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            )}

            {activeTab === 'planning-benevole' && (
              <section>
                <h2 className="mgr-section-title">Liste des Bénévoles</h2>

                {planningParBenevole.length === 0 && (
                  <p className="mgr-placeholder-text">Aucun bénévole inscrit à un créneau.</p>
                )}

                <div className="mgr-benevole-list">
                  {planningParBenevole.map((b) => (
                    <div key={b.id} className="mgr-benevole-card">
                      <div className="mgr-benevole-card-header">
                        <div>
                          <h3 className="mgr-benevole-card-name">{b.nom}</h3>
                          <span className="mgr-benevole-card-subtitle">
                            {b.heures} heure{b.heures > 1 ? 's' : ''} • {b.creneaux.length} créneau{b.creneaux.length > 1 ? 'x' : ''} assigné{b.creneaux.length > 1 ? 's' : ''}
                          </span>
                        </div>
                        <button
                          className="mgr-info-btn"
                          onClick={() => setOpenInfoId(openInfoId === b.id ? null : b.id)}
                        >
                          Information
                        </button>
                      </div>

                      {openInfoId === b.id && (
                        <div className="mgr-info-panel">
                          {b.profil ? (
                            <>
                              {b.profil.age && <p>Âge : {b.profil.age}</p>}
                              {b.profil.telephone && <p>Téléphone : {b.profil.telephone}</p>}
                              {b.profil.email && <p>Email : {b.profil.email}</p>}
                            </>
                          ) : (
                            <p>Aucune information complémentaire renseignée.</p>
                          )}
                        </div>
                      )}

                      <p className="mgr-creneaux-label">Créneaux assignés :</p>
                      <div className="mgr-creneaux-assign-list">
                        {b.creneaux.map((c) => (
                          <div key={c.id} className="mgr-creneau-assign-row">
                            <span className="mgr-creneau-jour-pill">{c.jour}</span>
                            <span className="mgr-creneau-assign-horaire">{c.heureDebut}-{c.heureFin}</span>
                            <span className="mgr-creneau-assign-poste">{getPosteLabel(c.poste)}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            )}

            {activeTab === 'gestion-benevoles' && !managerPoste && (
              <section>
                <div className="mgr-gb-form-card">
                  <h2 className="mgr-section-title">
                    {gbEditingId ? 'Modifier le bénévole' : 'Ajouter un nouveau bénévole'}
                  </h2>

                  {gbError && <p className="mgr-gb-error">{gbError}</p>}

                  <div className="mgr-gb-field">
                    <label className="mgr-gb-label">Nom complet *</label>
                    <input
                      type="text"
                      className="mgr-gb-input"
                      placeholder="Nom et prénom"
                      value={gbNomComplet}
                      onChange={(e) => setGbNomComplet(e.target.value)}
                    />
                  </div>

                  <div className="mgr-gb-row">
                    <div className="mgr-gb-field">
                      <label className="mgr-gb-label">Âge *</label>
                      <input
                        type="number"
                        className="mgr-gb-input"
                        placeholder="Âge"
                        value={gbAge}
                        onChange={(e) => setGbAge(e.target.value)}
                      />
                    </div>
                    <div className="mgr-gb-field">
                      <label className="mgr-gb-label">Téléphone *</label>
                      <input
                        type="text"
                        className="mgr-gb-input"
                        placeholder="06 12 34 56 78"
                        value={gbTelephone}
                        onChange={(e) => setGbTelephone(e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="mgr-gb-field">
                    <label className="mgr-gb-label">Adresse mail</label>
                    <input
                      type="email"
                      className="mgr-gb-input"
                      placeholder="benevole@exemple.com"
                      value={gbEmail}
                      onChange={(e) => setGbEmail(e.target.value)}
                    />
                  </div>

                  <div className="mgr-gb-field">
                    <label className="mgr-gb-label">Disponibilités</label>
                    <div className="mgr-gb-dispo-row">
                      <select
                        className="mgr-gb-select"
                        value={gbDispoJour}
                        onChange={(e) => setGbDispoJour(e.target.value)}
                      >
                        <option value="">Sélectionner un jour</option>
                        {JOURS.map((j) => (
                          <option key={j} value={j}>{j}</option>
                        ))}
                      </select>
                      <select
                        className="mgr-gb-select"
                        value={gbDispoCreneau}
                        onChange={(e) => setGbDispoCreneau(e.target.value)}
                      >
                        <option value="">Sélectionner un créneau</option>
                        {HORAIRES.map((h) => (
                          <option key={h} value={h}>{h}</option>
                        ))}
                      </select>
                      <button
                        className="mgr-gb-add-btn"
                        disabled={!gbDispoJour || !gbDispoCreneau}
                        onClick={handleAddGbDispo}
                      >
                        Ajouter
                      </button>
                    </div>

                    {gbDisponibilites.length === 0 ? (
                      <p className="mgr-gb-dispo-empty">Aucune disponibilité ajoutée</p>
                    ) : (
                      <div className="mgr-gb-dispo-list">
                        {gbDisponibilites.map((entry, i) => (
                          <span key={i} className="mgr-gb-dispo-chip">
                            {entry}
                            <button onClick={() => handleRemoveGbDispo(i)}>✕</button>
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  <button className="mgr-gb-submit-btn" onClick={handleSubmitGb} disabled={gbSaving}>
                    {gbSaving ? 'Enregistrement...' : gbEditingId ? 'Mettre à jour le bénévole' : 'Enregistrer le bénévole'}
                  </button>

                  {gbEditingId && (
                    <button className="mgr-gb-cancel-btn" onClick={resetGbForm}>
                      Annuler la modification
                    </button>
                  )}
                </div>

                <h2 className="mgr-section-title mgr-gb-list-title">Liste des bénévoles</h2>

                {benevoles.length === 0 && (
                  <p className="mgr-placeholder-text">Aucun bénévole enregistré.</p>
                )}

                <div className="mgr-benevole-list">
                  {benevoles.map((b) => {
                    const nbCreneaux = creneaux.filter((c) => c.isPris && c.prisParId === b.id).length;
                    return (
                      <div key={b.id} className="mgr-gb-card">
                        <div className="mgr-gb-card-main">
                          <h3 className="mgr-benevole-card-name">{b.nomComplet}</h3>
                          <span className="mgr-benevole-card-subtitle">
                            {b.age} ans • {b.telephone}
                          </span>
                          {b.email && (
                            <span className="mgr-benevole-card-subtitle">{b.email}</span>
                          )}
                          <span className="mgr-benevole-card-subtitle">
                            {(b.disponibilites || []).length} disponibilités • {nbCreneaux} créneaux assignés
                          </span>
                        </div>
                        <div className="mgr-gb-card-actions">
                          <button className="mgr-gb-edit-btn" onClick={() => startEditBenevole(b)}>
                            Modifier
                          </button>
                          <button className="mgr-gb-delete-btn" onClick={() => handleDeleteBenevole(b)}>
                            Supprimer
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </section>
            )}
          </div>
        </div>
      </div>

      {showNotifications && (
        <div className="mgr-notif-overlay" onClick={handleCloseNotifications}>
          <div className="mgr-notif-modal" onClick={(e) => e.stopPropagation()}>
            <h2 className="mgr-notif-modal-title">Notifications</h2>
            <div className="mgr-notif-list">
              {notifications.length === 0 && (
                <p className="mgr-notif-empty">Aucune notification.</p>
              )}
              {notifications.map((notif) => (
                <div key={notif.id} className="mgr-notif-item">
                  <p className="mgr-notif-item-title">{notif.title}</p>
                  <p className="mgr-notif-item-message">{notif.message}</p>
                  <p className="mgr-notif-item-time">{formatRelativeTime(notif.createdAt)}</p>
                </div>
              ))}
            </div>
            <button className="mgr-notif-close-btn" onClick={handleCloseNotifications}>Fermer</button>
          </div>
        </div>
      )}

      {assignTarget && (
        <div className="mgr-modal-overlay" onClick={closeAssignModal}>
          <div className="mgr-modal" onClick={(e) => e.stopPropagation()}>
            <h2 className="mgr-modal-title">Assigner un bénévole</h2>

            <div className="mgr-modal-creneau-info">
              <span className="mgr-creneau-jour-pill">{assignTarget.jour}</span>
              <span className="mgr-modal-horaire">{assignTarget.horaire}</span>
              <p className="mgr-modal-poste">{getPosteLabel(assignTarget.poste)}</p>
            </div>

            {(() => {
              const dispo = getAvailableBenevoles(assignTarget.jour, assignTarget.horaire);
              if (dispo.length === 0) {
                return <p className="mgr-modal-empty">Aucun bénévole disponible pour ce créneau</p>;
              }
              return (
                <div className="mgr-modal-benevole-list">
                  {dispo.map((b) => (
                    <div key={b.id} className="mgr-modal-benevole-row">
                      <span className="mgr-modal-benevole-name">
                        <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                          <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                        </svg>
                        {b.nomComplet}
                      </span>
                      <button
                        className="mgr-modal-assign-btn"
                        disabled={assigning}
                        onClick={() => handleAssign(b.id)}
                      >
                        Assigner
                      </button>
                    </div>
                  ))}
                </div>
              );
            })()}

            <button className="mgr-modal-cancel-btn" onClick={closeAssignModal} disabled={assigning}>
              Annuler
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;
