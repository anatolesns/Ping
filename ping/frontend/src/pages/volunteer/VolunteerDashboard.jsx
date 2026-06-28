import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getJsonFile, saveJsonFile } from "../../services/api";

const TABS = [
  { id: "agenda", label: "Mon Agenda", icon: "calendar" },
  { id: "slots", label: "Créneaux Disponibles", icon: "clock" },
  { id: "profile", label: "Mes Informations", icon: "user" }
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
  Billetterie: "🎫 Billetterie",
  Accueil: "👋 Accueil",
  Buvette: "☕ Buvette",
  "Coin Enfant": "🧸 Coin Enfant",
  Conférence: "🎤 Conférence"
};

function cleanRole(role) {
  return role.replace(/^[^\s]+\s/, "");
}

function makeLogin(name) {
  return name.toLowerCase().trim().replace(/\s+/g, ".");
}

function VolunteerDashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState("agenda");
  const [activeFilter, setActiveFilter] = useState("Tous les postes");
  const [shiftsData, setShiftsData] = useState([]);
  const [volunteersData, setVolunteersData] = useState([]);
  const [currentVolunteer, setCurrentVolunteer] = useState(null);
  const [editVol, setEditVol] = useState(null);
  const [editAvailDay, setEditAvailDay] = useState("");
  const [editAvailTime, setEditAvailTime] = useState("");

  useEffect(() => {
    async function fetchData() {
      try {
        const shifts = await getJsonFile("shifts.json");
        const volunteers = await getJsonFile("volunteers.json");

        let finalShifts = shifts;

        if (!finalShifts || finalShifts.length === 0) {
          finalShifts = [];
          let id = 1;

          for (const day of DAYS) {
            for (const time of TIME_SLOTS) {
              for (const role of ROLES) {
                finalShifts.push({
                  id: id++,
                  day,
                  time,
                  role: ROLE_EMOJIS[role],
                  volunteers: []
                });
              }
            }
          }

          await saveJsonFile("shifts.json", finalShifts);
        }

        let finalVolunteers = volunteers;

        if (!finalVolunteers || finalVolunteers.length === 0) {
          finalVolunteers = [
            {
              id: 1,
              name: "Bénévole Test",
              age: "20",
              phone: "06 12 34 56 78",
              login: "benevole.test",
              availabilities: [
                { day: "Jeudi", time: "08:00 - 12:00" },
                { day: "Vendredi", time: "12:00 - 16:00" },
                { day: "Samedi", time: "16:00 - 20:00" },
                { day: "Dimanche", time: "08:00 - 12:00" }
              ]
            }
          ];

          await saveJsonFile("volunteers.json", finalVolunteers);
        }

        const currentLogin = localStorage.getItem("currentVolunteerLogin");

        let volunteer = finalVolunteers.find(vol => {
          const volunteerLogin = vol.login || makeLogin(vol.name);
          return volunteerLogin === currentLogin;
        });

        if (!volunteer) {
          volunteer = finalVolunteers[0];
        }

        setShiftsData(finalShifts);
        setVolunteersData(finalVolunteers);
        setCurrentVolunteer(volunteer);
        setEditVol({ ...volunteer });
      } catch (err) {
  console.error("Erreur de chargement", err);

  const fallbackShifts = [];
  let id = 1;

  for (const day of DAYS) {
    for (const time of TIME_SLOTS) {
      for (const role of ROLES) {
        fallbackShifts.push({
          id: id++,
          day,
          time,
          role: ROLE_EMOJIS[role],
          volunteers: []
        });
      }
    }
  }

  const fallbackVolunteer = {
    id: 1,
    name: "Bénévole Test",
    age: "20",
    phone: "06 12 34 56 78",
    login: "benevole.test",
    availabilities: [
      { day: "Jeudi", time: "08:00 - 12:00" },
      { day: "Vendredi", time: "12:00 - 16:00" },
      { day: "Samedi", time: "16:00 - 20:00" },
      { day: "Dimanche", time: "08:00 - 12:00" }
    ]
  };

  setShiftsData(fallbackShifts);
  setVolunteersData([fallbackVolunteer]);
  setCurrentVolunteer(fallbackVolunteer);
  setEditVol({ ...fallbackVolunteer });
}
    }

    fetchData();
  }, []);

  const myShifts = useMemo(() => {
    if (!currentVolunteer) {
      return [];
    }

    return shiftsData.filter(shift =>
      shift.volunteers?.includes(currentVolunteer.name)
    );
  }, [shiftsData, currentVolunteer]);

  const availableShifts = useMemo(() => {
    if (!currentVolunteer) {
      return [];
    }

    return shiftsData.filter(shift => {
      const role = cleanRole(shift.role);
      const isFiltered = activeFilter === "Tous les postes" || activeFilter === role;
      const alreadyRegistered = shift.volunteers?.includes(currentVolunteer.name);
      const isAvailable = currentVolunteer.availabilities?.some(
        availability =>
          availability.day === shift.day && availability.time === shift.time
      );

      return isFiltered && isAvailable && !alreadyRegistered;
    });
  }, [shiftsData, currentVolunteer, activeFilter]);

  async function registerToShift(shiftId) {
    if (!currentVolunteer) {
      return;
    }

    const updatedShifts = shiftsData.map(shift => {
      if (shift.id !== shiftId) {
        return shift;
      }

      const volunteers = shift.volunteers || [];

      if (volunteers.includes(currentVolunteer.name)) {
        return shift;
      }

      return {
        ...shift,
        volunteers: [...volunteers, currentVolunteer.name]
      };
    });

    try {
      setShiftsData(updatedShifts);
      await saveJsonFile("shifts.json", updatedShifts);
      alert("Inscription enregistrée.");
    } catch (err) {
      alert("Erreur lors de l'inscription.");
    }
  }

  async function unregisterFromShift(shiftId) {
    if (!currentVolunteer) {
      return;
    }

    if (!window.confirm("Voulez-vous vraiment quitter ce créneau ?")) {
      return;
    }

    const updatedShifts = shiftsData.map(shift => {
      if (shift.id !== shiftId) {
        return shift;
      }

      return {
        ...shift,
        volunteers: (shift.volunteers || []).filter(name => name !== currentVolunteer.name)
      };
    });

    try {
      setShiftsData(updatedShifts);
      await saveJsonFile("shifts.json", updatedShifts);
      alert("Créneau retiré de votre agenda.");
    } catch (err) {
      alert("Erreur lors de la modification.");
    }
  }

  function addAvailability() {
    if (!editAvailDay || !editAvailTime || !editVol) {
      return;
    }

    const days = editAvailDay === "Tous les jours" ? DAYS : [editAvailDay];
    const times = editAvailTime === "Tous les créneaux" ? TIME_SLOTS : [editAvailTime];
    const newAvails = [...(editVol.availabilities || [])];

    days.forEach(day => {
      times.forEach(time => {
        const exists = newAvails.some(
          availability => availability.day === day && availability.time === time
        );

        if (!exists) {
          newAvails.push({ day, time });
        }
      });
    });

    setEditVol({ ...editVol, availabilities: newAvails });
    setEditAvailDay("");
    setEditAvailTime("");
  }

  async function saveProfile() {
  if (!editVol.name || !editVol.age || !editVol.phone) {
    alert("Veuillez remplir les informations obligatoires.");
    return;
  }

  const oldName = currentVolunteer.name;
  const newVolunteer = {
    ...editVol,
    login: editVol.login || makeLogin(editVol.name)
  };

  const updatedVolunteers = volunteersData.map(volunteer => {
    if (volunteer.id === newVolunteer.id) {
      return newVolunteer;
    }

    return volunteer;
  });

  const updatedShifts = shiftsData.map(shift => {
    const volunteers = shift.volunteers || [];

    if (!volunteers.includes(oldName)) {
      return shift;
    }

    return {
      ...shift,
      volunteers: volunteers.map(name => name === oldName ? newVolunteer.name : name)
    };
  });

  setVolunteersData(updatedVolunteers);
  setShiftsData(updatedShifts);
  setCurrentVolunteer(newVolunteer);
  setEditVol({ ...newVolunteer });

  try {
    await saveJsonFile("volunteers.json", updatedVolunteers);
    await saveJsonFile("shifts.json", updatedShifts);
  } catch (err) {
    console.error("Erreur de sauvegarde serveur", err);
  }

  alert("Profil mis à jour avec succès.");
}

  function renderIcon(icon) {
    if (icon === "calendar") {
      return (
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-5 h-5">
          <path d="M8 2v4"></path>
          <path d="M16 2v4"></path>
          <rect width="18" height="18" x="3" y="4" rx="2"></rect>
          <path d="M3 10h18"></path>
        </svg>
      );
    }

    if (icon === "clock") {
      return (
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-5 h-5">
          <circle cx="12" cy="12" r="10"></circle>
          <polyline points="12 6 12 12 16 14"></polyline>
        </svg>
      );
    }

    return (
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-5 h-5">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
        <circle cx="12" cy="7" r="4"></circle>
      </svg>
    );
  }

  if (!currentVolunteer || !editVol) {
    return (
      <div className="min-h-screen bg-green-100 flex items-center justify-center p-4">
        <div className="bg-white rounded-xl shadow-lg border-2 border-green-200 p-8 max-w-lg text-center">
          <h1 className="text-2xl font-semibold text-green-900 mb-4">Espace Bénévole</h1>
          <p className="text-green-700 mb-6">Chargement de votre espace bénévole...</p>
          <button onClick={() => navigate("/")} className="px-6 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg font-medium">
            Retour à l'accueil
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-green-100">
      <header className="bg-white border-b-2 border-green-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="bg-green-600 p-2 rounded-lg">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6 text-white">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            <div>
              <h1 className="text-green-900 font-semibold text-lg">Espace Bénévole</h1>
              <p className="text-green-600 text-sm">{currentVolunteer.name}</p>
            </div>
          </div>

          <button onClick={() => navigate("/")} className="flex items-center gap-2 px-4 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-lg transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="w-5 h-5">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" x2="9" y1="12" y2="12"></line>
            </svg>
            Déconnexion
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow-lg border-2 border-green-200">
          <div className="border-b-2 border-green-200 p-4">
            <div className="flex gap-4 flex-wrap">
              {TABS.map(tab => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`px-6 py-3 rounded-lg transition-colors flex items-center gap-2 ${
                    activeTab === tab.id
                      ? "bg-purple-500 text-white shadow-md"
                      : "bg-white text-gray-700 hover:bg-gray-50 border-2 border-gray-200"
                  }`}
                >
                  {renderIcon(tab.icon)}
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

          <div className="p-6">
            {activeTab === "agenda" && (
              <div>
                <h2 className="mb-4 text-green-900 text-xl font-semibold">Mon agenda</h2>

                <div className="mb-6 grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-green-50 border-2 border-green-200 rounded-xl p-5">
                    <p className="text-green-600 font-medium">Créneaux inscrits</p>
                    <p className="text-green-900 text-3xl font-bold">{myShifts.length}</p>
                  </div>
                  <div className="bg-green-50 border-2 border-green-200 rounded-xl p-5">
                    <p className="text-green-600 font-medium">Heures prévues</p>
                    <p className="text-green-900 text-3xl font-bold">{myShifts.length * 4}h</p>
                  </div>
                  <div className="bg-green-50 border-2 border-green-200 rounded-xl p-5">
                    <p className="text-green-600 font-medium">Disponibilités</p>
                    <p className="text-green-900 text-3xl font-bold">{currentVolunteer.availabilities?.length || 0}</p>
                  </div>
                </div>

                <div className="space-y-3">
                  {myShifts.length > 0 ? myShifts.map(shift => (
                    <div key={shift.id} className="border-2 border-green-200 rounded-lg p-4 hover:border-green-400 transition-colors bg-white">
                      <div className="flex justify-between items-center gap-4">
                        <div>
                          <div className="flex items-center gap-3 mb-2">
                            <span className="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-medium">{shift.day}</span>
                            <span className="text-green-700 font-medium">{shift.time}</span>
                          </div>
                          <h3 className="text-green-900 font-bold text-lg">{cleanRole(shift.role)}</h3>
                          <p className="text-green-600 text-sm">{shift.volunteers?.length || 0} bénévole(s) inscrit(s)</p>
                        </div>
                        <button onClick={() => unregisterFromShift(shift.id)} className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-colors font-medium">
                          Quitter
                        </button>
                      </div>
                    </div>
                  )) : (
                    <div className="text-center py-12 text-gray-500 bg-gray-50 rounded-lg border border-dashed border-gray-300">
                      Vous n'êtes inscrit sur aucun créneau.
                    </div>
                  )}
                </div>
              </div>
            )}

            {activeTab === "slots" && (
              <div>
                <h2 className="mb-4 text-green-900 text-xl font-semibold">Créneaux disponibles</h2>

                <div className="flex flex-wrap gap-2 mb-6">
                  {["Tous les postes", ...ROLES].map(filter => (
                    <button
                      key={filter}
                      onClick={() => setActiveFilter(filter)}
                      className={`px-4 py-2 rounded-lg transition-colors ${
                        activeFilter === filter
                          ? "bg-gray-700 text-white shadow-md"
                          : "bg-green-100 text-green-700 hover:bg-green-600 hover:text-white border border-gray-300"
                      }`}
                    >
                      {filter}
                    </button>
                  ))}
                </div>

                <div className="space-y-3">
                  {availableShifts.length > 0 ? availableShifts.map(shift => (
                    <div key={shift.id} className="border-2 border-green-200 rounded-lg p-4 hover:border-green-400 transition-colors bg-white">
                      <div className="flex justify-between items-center gap-4">
                        <div>
                          <div className="flex items-center gap-3 mb-2">
                            <span className="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-medium">{shift.day}</span>
                            <span className="text-green-700 font-medium">{shift.time}</span>
                          </div>
                          <h3 className="text-green-900 font-bold text-lg">{cleanRole(shift.role)}</h3>
                          <p className="text-green-600 text-sm">{shift.volunteers?.length || 0} bénévole(s) déjà inscrit(s)</p>
                        </div>
                        <button onClick={() => registerToShift(shift.id)} className="px-5 py-3 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors font-semibold shadow-md">
                          S'inscrire
                        </button>
                      </div>
                    </div>
                  )) : (
                    <div className="text-center py-12 text-gray-500 bg-gray-50 rounded-lg border border-dashed border-gray-300">
                      Aucun créneau disponible ne correspond à vos disponibilités.
                    </div>
                  )}
                </div>
              </div>
            )}

            {activeTab === "profile" && (
              <div>
                <h2 className="mb-6 text-green-900 text-xl font-semibold">Mes informations personnelles</h2>

                <div className="bg-green-50 border-2 border-green-200 rounded-xl p-6">
                  <div className="space-y-6">
                    <div>
                      <label className="block mb-2 text-green-800 font-medium">Nom complet *</label>
                      <input
                        type="text"
                        value={editVol.name}
                        onChange={event => setEditVol({ ...editVol, name: event.target.value })}
                        className="w-full px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        placeholder="Nom et prénom"
                      />
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="block mb-2 text-green-800 font-medium">Âge *</label>
                        <input
                          type="number"
                          value={editVol.age}
                          onChange={event => setEditVol({ ...editVol, age: event.target.value })}
                          className="w-full px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                          placeholder="Âge"
                        />
                      </div>
                      <div>
                        <label className="block mb-2 text-green-800 font-medium">Téléphone *</label>
                        <input
                          type="tel"
                          value={editVol.phone}
                          onChange={event => setEditVol({ ...editVol, phone: event.target.value })}
                          className="w-full px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                          placeholder="06 12 34 56 78"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block mb-3 text-green-800 font-medium">Mes disponibilités</label>

                      <div className="flex flex-col md:flex-row gap-3 mb-3">
                        <select
                          value={editAvailDay}
                          onChange={event => setEditAvailDay(event.target.value)}
                          className="flex-1 px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        >
                          <option value="">Sélectionner un jour</option>
                          <option value="Tous les jours">Tous les jours</option>
                          {DAYS.map(day => (
                            <option key={day} value={day}>{day}</option>
                          ))}
                        </select>

                        <select
                          value={editAvailTime}
                          onChange={event => setEditAvailTime(event.target.value)}
                          className="flex-1 px-4 py-3 bg-white border-2 border-green-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                        >
                          <option value="">Sélectionner un créneau</option>
                          <option value="Tous les créneaux">Tous les créneaux</option>
                          {TIME_SLOTS.map(time => (
                            <option key={time} value={time}>{time}</option>
                          ))}
                        </select>

                        <button onClick={addAvailability} className="px-6 py-3 bg-green-600 hover:bg-green-700 text-white font-medium rounded-lg transition-colors">
                          Ajouter
                        </button>
                      </div>

                      <div className="space-y-2">
                        {editVol.availabilities && editVol.availabilities.length > 0 ? editVol.availabilities.map((availability, index) => (
                          <div key={index} className="bg-white rounded-lg p-3 flex items-center justify-between border-2 border-green-200">
                            <div className="flex items-center gap-3">
                              <span className="px-3 py-1 bg-green-600 text-white rounded-full text-sm font-medium">{availability.day}</span>
                              <span className="text-green-700 font-medium">{availability.time}</span>
                            </div>
                            <button
                              onClick={() => setEditVol({
                                ...editVol,
                                availabilities: editVol.availabilities.filter((_, i) => i !== index)
                              })}
                              className="text-red-600 hover:text-red-800 font-bold text-lg"
                            >
                              ×
                            </button>
                          </div>
                        )) : (
                          <p className="text-green-600 text-sm">Aucune disponibilité ajoutée</p>
                        )}
                      </div>
                    </div>

                    <button onClick={saveProfile} className="w-full px-6 py-4 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-lg transition-colors shadow-md">
                      Enregistrer mes informations
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default VolunteerDashboard;