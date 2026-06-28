import React, { useState, useEffect, useMemo } from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';

const TABS = [
  { id: 'poste', label: "Planning par Poste", icon: "calendar" },
  { id: 'benevole', label: "Planning par Bénévole", icon: "user-plus" },
  { id: 'gestion', label: "Gestion des Bénévoles", icon: "user-check" }
];

const ROLES = [
  "Billetterie",
  "Accueil",
  "Buvette",
  "Coin Enfant",
  "Conférence"
];

const DAYS = ["Jeudi", "Vendredi", "Samedi", "Dimanche"];
const TIME_SLOTS = ["08:00 - 12:00", "12:00 - 16:00", "16:00 - 20:00"];

const ROLE_EMOJIS = {
  "Billetterie": "🎫 Billetterie",
  "Accueil": "👋 Accueil",
  "Buvette": "☕ Buvette",
  "Coin Enfant": "🧸 Coin Enfant",
  "Conférence": "🎤 Conférence"
};

function AdminHome() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('poste');
  const [activeFilter, setActiveFilter] = useState('Tous les postes');

  const [shiftsData, setShiftsData] = useState([]);
  const [volunteersData, setVolunteersData] = useState([]);
  const [selectedShiftForAssign, setSelectedShiftForAssign] = useState(null);

  const [newVolName, setNewVolName] = useState('');
  const [newVolAge, setNewVolAge] = useState('');
  const [newVolPhone, setNewVolPhone] = useState('');
  const [newVolAvails, setNewVolAvails] = useState([]);
  const [newAvailDay, setNewAvailDay] = useState('');
  const [newAvailTime, setNewAvailTime] = useState('');

  const [editVol, setEditVol] = useState(null);
  const [editAvailDay, setEditAvailDay] = useState('');
  const [editAvailTime, setEditAvailTime] = useState('');

  const [infoVolName, setInfoVolName] = useState(null);

  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState([]);

  // Charger les créneaux
  useEffect(() => {
    const fetchShifts = async () => {
      try {
        // TODO: Récupérer les créneaux depuis la nouvelle API (ex: GET /api/shifts)
        /*
        const data = await getShiftsFromAPI();
        if (data) {
          setShiftsData(data);
        }
        */
        setShiftsData([]);

        // TODO: Récupérer les bénévoles depuis la nouvelle API (ex: GET /api/volunteers)
        /*
        const vData = await getVolunteersFromAPI();
        if (vData) {
          setVolunteersData(vData);
        }
        */

        // TODO: Récupérer les notifications depuis la nouvelle API (ex: GET /api/notifications)
        /*
        const nData = await getNotificationsFromAPI();
        if (nData) {
          setNotifications(nData);
        }
        */
        setNotifications([]);
      } catch (err) {
        console.error("Erreur de chargement", err);
      }
    };
    fetchShifts();
  }, []);

  const volunteersWithShifts = useMemo(() => {
    const volMap = {};

    volunteersData.forEach(v => {
      volMap[v.name] = [];
    });

    shiftsData.forEach(shift => {
      shift.volunteers?.forEach(vName => {
        if (!volMap[vName]) volMap[vName] = [];
        volMap[vName].push(shift);
      });
    });

    const sortedNames = Object.keys(volMap).sort();
    return sortedNames.map(name => ({
      name,
      shifts: volMap[name],
      hours: volMap[name].length * 4
    }));
  }, [shiftsData, volunteersData]);

  // Supprimer un bénévole d'un créneau
  const handleRemoveVolunteer = async (shiftId, volunteerName) => {
    if (!window.confirm(`Êtes-vous sûr de vouloir retirer ${volunteerName} de ce créneau ?`)) return;

    try {
      const updatedShifts = shiftsData.map(s => {
        if (s.id === shiftId) {
          return { ...s, volunteers: s.volunteers.filter(v => v !== volunteerName) };
        }
        return s;
      });
      setShiftsData(updatedShifts);

      // TODO: Sauvegarder la suppression du bénévole pour ce créneau via l'API (ex: DELETE /api/shifts/{id}/volunteer)
    } catch (err) {
      alert("Erreur lors de la mise à jour");
    }
  };

  const handleAssignVolunteer = async (volunteerName) => {
    if (!selectedShiftForAssign) return;

    try {
      let updatedShifts = [...shiftsData];
      let shiftExists = false;

      updatedShifts = updatedShifts.map(s => {
        if (s.day === selectedShiftForAssign.day && s.time === selectedShiftForAssign.time && s.role === selectedShiftForAssign.role) {
          shiftExists = true;
          const vols = s.volunteers || [];
          if (!vols.includes(volunteerName)) {
            return { ...s, volunteers: [...vols, volunteerName] };
          }
        }
        return s;
      });

      if (!shiftExists) {
        updatedShifts.push({
          id: Date.now(),
          day: selectedShiftForAssign.day,
          time: selectedShiftForAssign.time,
          role: selectedShiftForAssign.role,
          volunteers: [volunteerName]
        });
      }

      setShiftsData(updatedShifts);

      // TODO: Sauvegarder l'assignation du bénévole au créneau via l'API (ex: POST /api/shifts/{id}/volunteer)

      setSelectedShiftForAssign(null);
    } catch (err) {
      alert("Erreur lors de l'assignation.");
    }
  };

  const handleAddAvailability = () => {
    if (newAvailDay && newAvailTime) {
      let daysToAdd = newAvailDay === "Tous les jours" ? DAYS : [newAvailDay];
      let timesToAdd = newAvailTime === "Tous les créneaux" ? TIME_SLOTS : [newAvailTime];

      let newAvails = [...newVolAvails];

      daysToAdd.forEach(d => {
        timesToAdd.forEach(t => {
          if (!newAvails.some(a => a.day === d && a.time === t)) {
            newAvails.push({ day: d, time: t });
          }
        });
      });

      setNewVolAvails(newAvails);
      setNewAvailDay('');
      setNewAvailTime('');
    }
  };

  const handleSaveVolunteer = async () => {
    if (!newVolName || !newVolAge || !newVolPhone) {
      alert("Veuillez remplir les informations obligatoires.");
      return;
    }

    const newVol = {
      id: Date.now(),
      name: newVolName,
      age: newVolAge,
      phone: newVolPhone,
      availabilities: newVolAvails
    };

    const updatedVolunteers = [...volunteersData, newVol];

    setVolunteersData(updatedVolunteers);

    setNewVolName('');
    setNewVolAge('');
    setNewVolPhone('');
    setNewVolAvails([]);

    try {
      // TODO: Sauvegarder le nouveau bénévole via l'API (ex: POST /api/volunteers)

      alert("Bénévole enregistré avec succès !");
    } catch (err) {
      alert("Erreur lors de la sauvegarde.");
    }
  };

  const handleEditAddAvailability = () => {
    if (editAvailDay && editAvailTime) {
      let daysToAdd = editAvailDay === "Tous les jours" ? DAYS : [editAvailDay];
      let timesToAdd = editAvailTime === "Tous les créneaux" ? TIME_SLOTS : [editAvailTime];
      let newAvails = [...(editVol.availabilities || [])];

      daysToAdd.forEach(d => {
        timesToAdd.forEach(t => {
          if (!newAvails.some(a => a.day === d && a.time === t)) {
            newAvails.push({ day: d, time: t });
          }
        });
      });

      setEditVol({ ...editVol, availabilities: newAvails });
      setEditAvailDay('');
      setEditAvailTime('');
    }
  };

  const handleSaveEdit = async () => {
    if (!editVol.name || !editVol.age || !editVol.phone) {
      alert("Veuillez remplir les informations obligatoires.");
      return;
    }

    const oldVol = volunteersData.find(v => v.id === editVol.id);
    const updatedVolunteers = volunteersData.map(v => v.id === editVol.id ? editVol : v);

    if (oldVol) {
      let shiftsModified = false;
      const newShifts = shiftsData.map(s => {
        let vols = s.volunteers ? [...s.volunteers] : [];

        if (vols.includes(oldVol.name)) {
          const isStillAvailable = editVol.availabilities?.some(a => a.day === s.day && a.time === s.time);

          if (!isStillAvailable) {
            vols = vols.filter(n => n !== oldVol.name);
            shiftsModified = true;
          } else if (oldVol.name !== editVol.name) {
            vols = vols.map(n => n === oldVol.name ? editVol.name : n);
            shiftsModified = true;
          }
        }
        return { ...s, volunteers: vols };
      });

      if (shiftsModified) {
        setShiftsData(newShifts);
        // TODO: Mettre à jour les créneaux affectés via l'API si les dispos ont changé
      }
    }

    setVolunteersData(updatedVolunteers);
    setEditVol(null);

    try {
      // TODO: Mettre à jour le profil du bénévole via l'API (ex: PUT /api/volunteers/{id})
      alert("Profil mis à jour avec succès !");
    } catch (err) {
      alert("Erreur lors de la sauvegarde.");
    }
  };

  const handleEditVolunteer = (vol) => {
    setEditVol({ ...vol });
    setEditAvailDay('');
    setEditAvailTime('');
  };

  const handleDeleteVolunteer = async (id) => {
    if (!window.confirm("Voulez-vous vraiment supprimer ce bénévole ?")) return;

    const volToDelete = volunteersData.find(v => v.id === id);
    const updated = volunteersData.filter(v => v.id !== id);
    setVolunteersData(updated);

    if (volToDelete) {
      let shiftsModified = false;
      const newShifts = shiftsData.map(s => {
        if (s.volunteers?.includes(volToDelete.name)) {
          shiftsModified = true;
          return { ...s, volunteers: s.volunteers.filter(n => n !== volToDelete.name) };
        }
        return s;
      });
      if (shiftsModified) {
        setShiftsData(newShifts);
        // TODO: Mettre à jour les créneaux via l'API pour refléter la suppression
      }
    }

    try {
      // TODO: Supprimer le bénévole via l'API (ex: DELETE /api/volunteers/{id})
    } catch (err) {
      alert("Erreur lors de la suppression.");
    }
  };

  return (
    <div className="min-h-screen bg-green-100">
      {showNotifications && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-xl font-bold mb-4">Notifications</h2>
            <div className="space-y-3">
              <p className="text-gray-600">Aucune nouvelle notification.</p>
            </div>
            <button onClick={() => setShowNotifications(false)} className="mt-6 w-full py-2 bg-green-600 text-white rounded-lg">Fermer</button>
          </div>
        </div>
      )}
      <header className="bg-white border-b-2 border-green-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="bg-green-600 p-2 rounded-lg">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-users w-6 h-6 text-white">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
            </div>
            <div>
              <h1 className="text-green-900 font-semibold text-lg">Espace Manager</h1>
              <p className="text-green-600 text-sm">manager</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <button onClick={() => {
              setShowNotifications(true);
              // TODO: Appeler l'API pour marquer les notifications comme lues (ex: PUT /api/notifications/read)
              setNotifications(notifications.map(n => ({ ...n, read: true })));
            }} className="flex items-center gap-2 px-4 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-lg transition-colors relative">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-bell w-5 h-5">
                <path d="M10.268 21a2 2 0 0 0 3.464 0"></path>
                <path d="M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326"></path>
              </svg>
              Notifications
              {notifications.filter(n => !n.read).length > 0 && (
                <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                  {notifications.filter(n => !n.read).length}
                </span>
              )}
            </button>
            <button onClick={() => navigate('/')} className="flex items-center gap-2 px-4 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-lg transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-log-out w-5 h-5">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" x2="9" y1="12" y2="12"></line>
              </svg>
              Déconnexion
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow-lg border-2 border-green-200">
          <div className="border-b-2 border-green-200 p-4">
            <div className="flex gap-4">
              {TABS.map(tab => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`px-6 py-3 rounded-lg transition-colors flex items-center gap-2 ${activeTab === tab.id
                      ? "bg-purple-500 text-white shadow-md"
                      : "bg-white text-gray-700 hover:bg-gray-50 border-2 border-gray-200"
                    }`}
                >
                  {tab.icon === 'calendar' && (
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-5 h-5"><path d="M8 2v4"></path><path d="M16 2v4"></path><rect width="18" height="18" x="3" y="4" rx="2"></rect><path d="M3 10h18"></path></svg>
                  )}
                  {tab.icon === 'user-plus' && (
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-5 h-5"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><line x1="19" x2="19" y1="8" y2="14"></line><line x1="22" x2="16" y1="11" y2="11"></line></svg>
                  )}
                  {tab.icon === 'user-check' && (
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-5 h-5"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><polyline points="16 11 18 13 22 9"></polyline></svg>
                  )}
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

          <div className="p-6">
            {activeTab === 'poste' && (
              <div className="overflow-x-auto">
                <div className="mb-6">
                  <h2 className="mb-4 text-green-900 text-xl font-semibold">Calendrier de la Semaine</h2>
                  <div className="flex flex-wrap gap-2">
                    {["Tous les postes", ...ROLES].map(filter => (
                      <button
                        key={filter}
                        onClick={() => setActiveFilter(filter)}
                        className={`px-4 py-2 rounded-lg transition-colors ${activeFilter === filter
                            ? "bg-gray-700 text-white shadow-md"
                            : "bg-green-100 text-green-700 hover:bg-green-600 hover:text-white border border-gray-300"
                          }`}
                      >
                        {filter}
                      </button>
                    ))}
                  </div>
                </div>

                <table className="w-full border-collapse">
                  <thead>
                    <tr>
                      <th className="border-2 border-green-300 bg-green-600 text-white p-3 sticky left-0 z-10">Créneaux</th>
                      {DAYS.map(day => (
                        <th key={day} className="border-2 border-green-300 bg-green-600 text-white p-3 min-w-[200px]">{day}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {TIME_SLOTS.map(time => (
                      <tr key={time}>
                        <td className="border-2 border-green-300 bg-green-100 p-3 sticky left-0 z-10">
                          <div className="text-green-900 font-medium">{time}</div>
                        </td>
                        {DAYS.map(day => (
                          <td key={day} className="border-2 border-green-300 p-2 align-top bg-white">
                            <div className="space-y-2">
                              {ROLES
                                .filter(r => activeFilter === 'Tous les postes' || activeFilter === r)
                                .map(role => {
                                  const emojiRole = ROLE_EMOJIS[role];
                                  const shift = shiftsData.find(s => s.day === day && s.time === time && s.role === emojiRole);
                                  const volunteers = shift?.volunteers || [];
                                  const count = volunteers.length;

                                  if (count > 0) {
                                    return (
                                      <div key={role} onClick={() => setSelectedShiftForAssign(shift || { day, time, role: emojiRole, volunteers: [] })} className="rounded-lg p-2 border-2 cursor-pointer hover:shadow-md transition-all bg-green-50 border-green-400">
                                        <div className="text-green-900 mb-1 flex items-center justify-between">
                                          <span className="px-2 py-1 bg-green-600 text-white rounded text-xs font-medium shadow-sm">{role}</span>
                                          <span className="text-xs font-medium">{count}</span>
                                        </div>
                                        <div className="text-green-700 space-y-1">
                                          {volunteers.map(v => (
                                            <div key={v} className="flex items-center justify-between gap-1">
                                              <div className="flex items-center gap-1">
                                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="lucide lucide-users w-3 h-3"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M22 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                                                <span className="text-sm">{v}</span>
                                              </div>
                                              <button
                                                onClick={(e) => { e.stopPropagation(); handleRemoveVolunteer(shift.id, v); }}
                                                className="text-red-600 hover:text-red-800 font-bold"
                                              >
                                                ×
                                              </button>
                                            </div>
                                          ))}
                                        </div>
                                      </div>
                                    );
                                  } else {
                                    return (
                                      <div key={role} onClick={() => setSelectedShiftForAssign(shift || { day, time, role: emojiRole, volunteers: [] })} className="rounded-lg p-2 border-2 cursor-pointer hover:shadow-md transition-all bg-gray-50 border-gray-300 hover:border-gray-400">
                                        <div className="text-gray-700 mb-1 flex items-center justify-between">
                                          <span className="px-2 py-1 bg-green-600 text-white rounded text-xs font-medium shadow-sm">{role}</span>
                                          <span className="text-xs font-medium">0</span>
                                        </div>
                                        <div className="text-red-600 text-sm">Vide</div>
                                      </div>
                                    );
                                  }
                                })}
                            </div>
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {activeTab === 'benevole' && (
              <div className="space-y-4">
                <h2 className="mb-4 text-green-900 text-xl font-semibold">Liste des Bénévoles</h2>
                {volunteersWithShifts.length > 0 ? volunteersWithShifts.map(vw => (
                  <div key={vw.name} className="border-2 border-green-200 rounded-lg p-4 hover:border-green-400 transition-colors bg-white">
                    <div className="flex justify-between items-center">
                      <div>
                        <h3 className="text-green-900 text-xl font-bold">{vw.name}</h3>
                        <p className="text-green-600 font-medium">{vw.hours} heures totales ({vw.shifts.length} créneaux)</p>
                      </div>
                      <button
                        onClick={() => setInfoVolName(vw.name)}
                        className="px-4 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-lg transition-colors font-medium"
                      >
                        Informations
                      </button>
                    </div>
                  </div>
                )) : (
                  <div className="text-center py-12 text-gray-500 bg-gray-50 rounded-lg border border-dashed border-gray-300">
                    Aucun bénévole n'est inscrit pour le moment.
                  </div>
                )}
              </div>
            )}

            {activeTab === 'gestion' && (
              <div>
                <div className="mb-8 bg-green-50 border-2 border-green-200 rounded-xl p-6">
                  <h2 className="mb-6 text-green-900 text-xl font-semibold">Ajouter un nouveau bénévole</h2>
                  <div className="space-y-6">
                    <div>
                      <label className="block mb-2 text-green-800 font-medium">Nom complet *</label>
                      <input
                        type="text"
                        value={newVolName}
                        onChange={(e) => setNewVolName(e.target.value)}
                        className="w-full px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        placeholder="Nom et prénom"
                      />
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block mb-2 text-green-800 font-medium">Âge *</label>
                        <input
                          type="number"
                          value={newVolAge}
                          onChange={(e) => setNewVolAge(e.target.value)}
                          className="w-full px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                          placeholder="Âge"
                        />
                      </div>
                      <div>
                        <label className="block mb-2 text-green-800 font-medium">Téléphone *</label>
                        <input
                          type="tel"
                          value={newVolPhone}
                          onChange={(e) => setNewVolPhone(e.target.value)}
                          className="w-full px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                          placeholder="06 12 34 56 78"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block mb-3 text-green-800 font-medium">Disponibilités</label>
                      <div className="flex gap-3 mb-3">
                        <select
                          value={newAvailDay}
                          onChange={(e) => setNewAvailDay(e.target.value)}
                          className="flex-1 px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        >
                          <option value="">Sélectionner un jour</option>
                          <option value="Tous les jours">Tous les jours</option>
                          {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
                        </select>
                        <select
                          value={newAvailTime}
                          onChange={(e) => setNewAvailTime(e.target.value)}
                          className="flex-1 px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        >
                          <option value="">Sélectionner un créneau</option>
                          <option value="Tous les créneaux">Tous les créneaux</option>
                          {TIME_SLOTS.map(t => <option key={t} value={t}>{t}</option>)}
                        </select>
                        <button
                          onClick={handleAddAvailability}
                          className="px-6 py-3 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg transition-colors"
                        >
                          Ajouter
                        </button>
                      </div>
                      <div className="space-y-2">
                        {newVolAvails.length > 0 ? (
                          newVolAvails.map((avail, idx) => (
                            <div key={idx} className="inline-flex items-center gap-2 bg-green-100 text-green-800 px-3 py-1 rounded-full mr-2 mb-2">
                              {avail.day} {avail.time}
                              <button onClick={() => setNewVolAvails(newVolAvails.filter((_, i) => i !== idx))} className="text-red-500 hover:text-red-700 font-bold ml-1">×</button>
                            </div>
                          ))
                        ) : (
                          <p className="text-green-600 text-sm">Aucune disponibilité ajoutée</p>
                        )}
                      </div>
                    </div>
                    <button
                      onClick={handleSaveVolunteer}
                      className="w-full px-6 py-4 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-lg transition-colors shadow-md"
                    >
                      Enregistrer le bénévole
                    </button>
                  </div>
                </div>

                <div>
                  <h2 className="mb-4 text-green-900 text-xl font-semibold">Liste des bénévoles</h2>
                  <div className="space-y-3">
                    {volunteersData.length > 0 ? volunteersData.map(vol => {
                      let assignedCount = 0;
                      shiftsData.forEach(s => {
                        if (s.volunteers?.includes(vol.name)) assignedCount++;
                      });

                      return (
                        <div key={vol.id} className="border-2 border-green-200 rounded-lg p-4 hover:border-green-400 transition-colors bg-white">
                          <div className="flex justify-between items-center">
                            <div>
                              <h3 className="text-green-900 font-semibold text-lg">{vol.name}</h3>
                              <p className="text-green-600 text-sm mb-1">{vol.age} ans • {vol.phone}</p>
                              <p className="text-green-700 text-sm font-medium">
                                {vol.availabilities?.length || 0} disponibilités enregistrées • {assignedCount} créneaux assignés
                              </p>
                            </div>
                            <div className="flex gap-2">
                              <button onClick={() => handleEditVolunteer(vol)} className="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg transition-colors flex items-center gap-2 font-medium">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z"></path></svg>
                                Modifier
                              </button>
                              <button onClick={() => handleDeleteVolunteer(vol.id)} className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-colors flex items-center gap-2 font-medium">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18"></path><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path><line x1="10" x2="10" y1="11" y2="17"></line><line x1="14" x2="14" y1="11" y2="17"></line></svg>
                                Supprimer
                              </button>
                            </div>
                          </div>
                        </div>
                      )
                    }) : (
                      <div className="text-center py-8 text-gray-500 bg-gray-50 rounded-lg border border-dashed border-gray-300">
                        Aucun profil de bénévole n'a été créé.
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* MODALE ASSIGNATION BÉNÉVOLE */}
        {selectedShiftForAssign && (
          <div className="fixed inset-0 flex items-center justify-center z-[100] p-4 bg-black/40">
            <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full border-2 border-green-200">
              <h2 className="mb-4 text-green-900 text-xl font-semibold">Assigner un bénévole</h2>
              <div className="mb-6 p-4 bg-green-50 rounded-lg border-2 border-green-200">
                <p className="text-green-700 mb-2 font-medium">
                  <span className="px-3 py-1 bg-green-600 text-white rounded-full mr-2 text-sm">{selectedShiftForAssign.day}</span>
                  {selectedShiftForAssign.time}
                </p>
                <p className="text-green-800 text-lg"><strong>{selectedShiftForAssign.role.replace(/^[^\s]+\s/, '')}</strong></p>
              </div>

              <div className="space-y-2 mb-6 max-h-80 overflow-y-auto">
                {(() => {
                  const availableVols = volunteersData.filter(vol => {
                    const isAlreadyAssigned = selectedShiftForAssign.volunteers?.includes(vol.name);
                    if (isAlreadyAssigned) return false;
                    const isAvailable = vol.availabilities?.some(
                      a => a.day === selectedShiftForAssign.day && a.time === selectedShiftForAssign.time
                    );
                    return isAvailable;
                  });

                  if (availableVols.length === 0) {
                    return <p className="text-center text-gray-500 py-4">Aucun bénévole disponible pour ce créneau.</p>;
                  }

                  return availableVols.map(vol => (
                    <button
                      key={vol.id}
                      onClick={() => handleAssignVolunteer(vol.name)}
                      className="w-full p-4 bg-green-100 hover:bg-green-200 text-green-800 rounded-lg transition-colors text-left border-2 border-green-200 hover:border-green-400 font-medium"
                    >
                      {vol.name}
                    </button>
                  ));
                })()}
              </div>

              <button
                onClick={() => setSelectedShiftForAssign(null)}
                className="w-full px-4 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors font-medium shadow-md"
              >
                Annuler
              </button>
            </div>
          </div>
        )}

        {/* MODALE ÉDITION BÉNÉVOLE */}
        {editVol && (
          <div className="fixed inset-0 flex items-center justify-center z-[100] p-4 bg-black/40">
            <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-2xl w-full border-2 border-green-200 max-h-[90vh] overflow-y-auto">
              <h2 className="mb-6 text-green-900 text-2xl font-bold">Modifier le bénévole</h2>
              <div className="space-y-6">
                <div>
                  <label className="block mb-2 text-green-800 font-medium">Nom complet *</label>
                  <input
                    type="text"
                    value={editVol.name}
                    onChange={(e) => setEditVol({ ...editVol, name: e.target.value })}
                    className="w-full px-4 py-3 bg-green-50 border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                    placeholder="Nom et prénom"
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block mb-2 text-green-800 font-medium">Âge *</label>
                    <input
                      type="number"
                      value={editVol.age}
                      onChange={(e) => setEditVol({ ...editVol, age: e.target.value })}
                      className="w-full px-4 py-3 bg-green-50 border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                      placeholder="Âge"
                    />
                  </div>
                  <div>
                    <label className="block mb-2 text-green-800 font-medium">Téléphone *</label>
                    <input
                      type="tel"
                      value={editVol.phone}
                      onChange={(e) => setEditVol({ ...editVol, phone: e.target.value })}
                      className="w-full px-4 py-3 bg-green-50 border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                      placeholder="06 12 34 56 78"
                    />
                  </div>
                </div>
                <div>
                  <label className="block mb-3 text-green-800 font-medium">Disponibilités</label>
                  <div className="flex gap-3 mb-3">
                    <select
                      value={editAvailDay}
                      onChange={(e) => setEditAvailDay(e.target.value)}
                      className="flex-1 px-4 py-3 bg-green-50 border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                    >
                      <option value="">Sélectionner un jour</option>
                      <option value="Tous les jours">Tous les jours</option>
                      {DAYS.map(d => <option key={d} value={d}>{d}</option>)}
                    </select>
                    <select
                      value={editAvailTime}
                      onChange={(e) => setEditAvailTime(e.target.value)}
                      className="flex-1 px-4 py-3 bg-green-50 border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                    >
                      <option value="">Sélectionner un créneau</option>
                      <option value="Tous les créneaux">Tous les créneaux</option>
                      {TIME_SLOTS.map(t => <option key={t} value={t}>{t}</option>)}
                    </select>
                    <button
                      onClick={handleEditAddAvailability}
                      className="px-6 py-3 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg transition-colors"
                    >
                      Ajouter
                    </button>
                  </div>
                  <div className="space-y-2">
                    {editVol.availabilities && editVol.availabilities.length > 0 ? (
                      editVol.availabilities.map((avail, idx) => (
                        <div key={idx} className="bg-green-50 rounded-lg p-3 flex items-center justify-between border-2 border-green-200">
                          <div className="flex items-center gap-3">
                            <span className="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-medium">{avail.day}</span>
                            <span className="text-green-700 font-medium">{avail.time}</span>
                          </div>
                          <button onClick={() => setEditVol({ ...editVol, availabilities: editVol.availabilities.filter((_, i) => i !== idx) })} className="text-red-600 hover:text-red-800 font-bold text-lg">×</button>
                        </div>
                      ))
                    ) : (
                      <p className="text-green-600 text-sm">Aucune disponibilité ajoutée</p>
                    )}
                  </div>
                </div>
                <div className="flex gap-3">
                  <button
                    onClick={handleSaveEdit}
                    className="flex-1 px-6 py-4 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-lg transition-colors shadow-md"
                  >
                    Enregistrer les modifications
                  </button>
                  <button
                    onClick={() => setEditVol(null)}
                    className="px-6 py-4 bg-gray-500 hover:bg-gray-600 text-white font-semibold rounded-lg transition-colors shadow-md"
                  >
                    Annuler
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* MODALE INFORMATIONS BÉNÉVOLE */}
        {infoVolName && (() => {
          const fullVol = volunteersData.find(v => v.name === infoVolName);
          const assignedShifts = volunteersWithShifts.find(v => v.name === infoVolName)?.shifts || [];

          return (
            <div className="fixed inset-0 flex items-center justify-center z-[100] p-4 bg-black/40">
              <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-lg w-full border-2 border-green-200 max-h-[90vh] overflow-y-auto">
                <h2 className="mb-6 text-green-900 text-2xl font-bold">Informations Bénévole</h2>
                <div className="space-y-4 mb-6">
                  <div className="p-4 bg-green-50 rounded-lg border-2 border-green-200">
                    <p className="text-green-600 mb-1 font-medium">Nom</p>
                    <p className="text-green-900 font-bold text-lg">{infoVolName}</p>
                  </div>
                  <div className="p-4 bg-green-50 rounded-lg border-2 border-green-200">
                    <p className="text-green-600 mb-1 font-medium">Âge</p>
                    <p className="text-green-900 font-bold">{fullVol?.age ? `${fullVol.age} ans` : 'Non renseigné'}</p>
                  </div>
                  <div className="p-4 bg-green-50 rounded-lg border-2 border-green-200">
                    <p className="text-green-600 mb-1 font-medium">Téléphone</p>
                    <p className="text-green-900 font-bold">{fullVol?.phone || 'Non renseigné'}</p>
                  </div>
                  <div className="p-4 bg-green-50 rounded-lg border-2 border-green-200">
                    <p className="text-green-600 mb-2 font-medium">Disponibilités déclarées</p>
                    <div className="space-y-2">
                      {fullVol?.availabilities && fullVol.availabilities.length > 0 ? (
                        fullVol.availabilities.map((avail, idx) => (
                          <div key={idx} className="bg-white rounded-lg p-3 flex items-center gap-3 border border-green-300">
                            <span className="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-medium">{avail.day}</span>
                            <span className="text-green-700 font-medium">{avail.time}</span>
                          </div>
                        ))
                      ) : (
                        <p className="text-green-600">Aucune disponibilité renseignée</p>
                      )}
                    </div>
                  </div>
                  <div className="p-4 bg-green-50 rounded-lg border-2 border-green-200">
                    <p className="text-green-600 mb-2 font-medium">Créneaux assignés</p>
                    <div className="space-y-2">
                      {assignedShifts.length > 0 ? (
                        assignedShifts.map((shift, idx) => (
                          <div key={idx} className="bg-white rounded-lg p-3 flex items-center gap-3 border border-green-300">
                            <span className="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-medium">{shift.day}</span>
                            <span className="text-green-700 font-medium mr-2">{shift.time}</span>
                            <span className="text-green-900 font-semibold">{shift.role.replace(/^[^\s]+\s/, '')}</span>
                          </div>
                        ))
                      ) : (
                        <p className="text-green-600">Aucun créneau assigné</p>
                      )}
                    </div>
                  </div>
                </div>
                <button
                  onClick={() => setInfoVolName(null)}
                  className="w-full px-4 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors font-semibold shadow-md"
                >
                  Fermer
                </button>
              </div>
            </div>
          );
        })()}

{
  showNotifications && (
    <div className="fixed inset-0 flex items-center justify-center z-[100] p-4 bg-black/40">
      <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-lg w-full border-2 border-green-200 max-h-[90vh] overflow-y-auto">
        <h2 className="mb-6 text-green-900 text-2xl font-bold">Notifications</h2>
        <div className="space-y-3 mb-6">
          {notifications.length > 0 ? (
            notifications.map((notif, idx) => (
              <div key={idx} className={`p-4 rounded-lg border-2 ${notif.read ? 'bg-white border-gray-200' : 'bg-green-50 border-green-200'}`}>
                <p className={`mb-1 font-semibold ${notif.read ? 'text-gray-700' : 'text-green-800'}`}>{notif.title}</p>
                <p className={notif.read ? 'text-gray-600' : 'text-green-600'}>{notif.message}</p>
                <p className={`mt-2 text-sm ${notif.read ? 'text-gray-400' : 'text-green-500'}`}>{notif.time}</p>
              </div>
            ))
          ) : (
            <div className="text-center py-8 text-gray-500 bg-gray-50 rounded-lg border border-dashed border-gray-300">
              Aucune notification pour le moment.
            </div>
          )}
        </div>
        <button
          onClick={() => setShowNotifications(false)}
          className="w-full px-4 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors font-medium"
        >
          Fermer
        </button>
      </div>
    </div>
  )
}
      </main >
    </div >
  );
}

function AdminRoutes() {
  return (
    <Routes>
      <Route path="/" element={<AdminHome />} />
    </Routes>
  );
}

export default AdminRoutes;
