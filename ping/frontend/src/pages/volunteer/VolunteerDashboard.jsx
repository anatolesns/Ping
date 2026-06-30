import React, { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

const TABS = [
  { id: "register", label: "S'inscrire aux créneaux", color: "bg-green-600" },
  { id: "calendar", label: "Mon calendrier", color: "bg-blue-600" },
  { id: "availability", label: "Mes disponibilités", color: "bg-purple-600" },
  { id: "profile", label: "Mes informations", color: "bg-indigo-600" }
];

const FILTERS = [
  "Tous les postes",
  "🎫 Billetterie",
  "👋 Accueil",
  "☕ Buvette",
  "🧸 Coin Enfant",
  "🎤 Conférence"
];

const DAYS = ["Jeudi", "Vendredi", "Samedi", "Dimanche"];
const TIMES = ["08:00-12:00", "12:00-16:00", "16:00-20:00"];

const MOCK_SHIFTS = [
  { id: 1, day: "Vendredi", time: "08:00 - 12:00", role: "🎫 Billetterie" },
  { id: 2, day: "Vendredi", time: "12:00 - 16:00", role: "👋 Accueil" },
  { id: 3, day: "Vendredi", time: "16:00 - 20:00", role: "☕ Buvette" },
  { id: 4, day: "Samedi", time: "08:00 - 12:00", role: "🧸 Coin Enfant" },
  { id: 5, day: "Samedi", time: "12:00 - 16:00", role: "🎤 Conférence" },
  { id: 6, day: "Samedi", time: "16:00 - 20:00", role: "🎫 Billetterie" },
  { id: 7, day: "Dimanche", time: "08:00 - 12:00", role: "👋 Accueil" },
  { id: 8, day: "Dimanche", time: "12:00 - 16:00", role: "☕ Buvette" },
  { id: 9, day: "Dimanche", time: "16:00 - 20:00", role: "🧸 Coin Enfant" }
];

const MOCK_AVAILABILITIES = [
  { id: 1, day: "Vendredi", time: "08:00-12:00" },
  { id: 2, day: "Samedi", time: "12:00-16:00" }
];

function VolunteerDashboard() {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState("register");
  const [activeFilter, setActiveFilter] = useState("Tous les postes");

  const [reservedShifts, setReservedShifts] = useState([]);
  const [availabilities, setAvailabilities] = useState(MOCK_AVAILABILITIES);

  const [availabilityDay, setAvailabilityDay] = useState("");
  const [availabilityTime, setAvailabilityTime] = useState("");

  const [profile, setProfile] = useState({
    name: "benevole",
    age: "28",
    phone: "06 12 34 56 78",
    email: "exemple@email.com"
  });

  // TODO:
  // Remplacer MOCK_SHIFTS par les créneaux récupérés depuis le backend.
  // Exemple futur : GET /api/shifts ou autre route fournie par le backend.
  const filteredShifts = useMemo(() => {
    if (activeFilter === "Tous les postes") {
      return MOCK_SHIFTS;
    }

    return MOCK_SHIFTS.filter(shift => shift.role === activeFilter);
  }, [activeFilter]);

  function handleRegister(shift) {
    // TODO:
    // Envoyer l'inscription au backend.
    // Exemple futur : POST /api/volunteers/me/shifts avec l'id du créneau.

    const alreadyReserved = reservedShifts.some(item => item.id === shift.id);

    if (alreadyReserved) {
      alert("Vous êtes déjà inscrit sur ce créneau.");
      return;
    }

    setReservedShifts([...reservedShifts, shift]);
    alert("Inscription enregistrée.");
  }

  function handleAddAvailability() {
    // TODO:
    // Envoyer la disponibilité au backend.
    // Exemple futur : POST /api/volunteers/me/availabilities.

    if (!availabilityDay || !availabilityTime) {
      alert("Veuillez sélectionner un jour et un créneau.");
      return;
    }

    const exists = availabilities.some(
      item => item.day === availabilityDay && item.time === availabilityTime
    );

    if (exists) {
      alert("Cette disponibilité existe déjà.");
      return;
    }

    const newAvailability = {
      id: Date.now(),
      day: availabilityDay,
      time: availabilityTime
    };

    setAvailabilities([...availabilities, newAvailability]);
    setAvailabilityDay("");
    setAvailabilityTime("");
  }

  function handleRemoveAvailability(id) {
    // TODO:
    // Supprimer la disponibilité côté backend.
    // Exemple futur : DELETE /api/volunteers/me/availabilities/:id.

    setAvailabilities(availabilities.filter(item => item.id !== id));
  }

  function handleSaveProfile() {
    // TODO:
    // Envoyer les informations personnelles au backend.
    // Exemple futur : PUT /api/volunteers/me.

    alert("Modifications enregistrées.");
  }

  function getReservedShift(day, time) {
    return reservedShifts.find(shift => {
      const normalizedTime = shift.time.replaceAll(" ", "");
      return shift.day === day && normalizedTime === time;
    });
  }

  function renderTabs() {
    return (
      <div className="border-b border-gray-200 p-4 bg-gray-50">
        <div className="flex gap-4 flex-wrap">
          {TABS.map(tab => {
            const isActive = activeTab === tab.id;

            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-6 py-3 rounded-lg transition-all font-medium ${
                  isActive
                    ? `${tab.color} text-white shadow-md`
                    : "bg-white text-gray-700 hover:bg-gray-100 border border-gray-300"
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>
    );
  }

  function renderRegisterTab() {
    return (
      <div>
        <div className="mb-6">
          <h2 className="mb-4 text-gray-900 text-2xl font-semibold">
            S'inscrire aux créneaux
          </h2>

          <div className="flex flex-wrap gap-2">
            {FILTERS.map(filter => {
              const isActive = activeFilter === filter;

              return (
                <button
                  key={filter}
                  onClick={() => setActiveFilter(filter)}
                  className={`px-4 py-2 rounded-lg transition-all font-medium ${
                    isActive
                      ? "bg-gray-800 text-white shadow-md"
                      : "bg-green-50 text-green-900 hover:bg-green-600 hover:text-white border border-green-300"
                  }`}
                >
                  {filter}
                </button>
              );
            })}
          </div>
        </div>

        <div className="space-y-3">
          {filteredShifts.map(shift => (
            <div
              key={shift.id}
              className="flex items-center justify-between p-5 rounded-lg border transition-all bg-white border-gray-200 hover:border-gray-300 hover:shadow-sm"
            >
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-3">
                  <span className="px-3 py-1 bg-green-600 text-white rounded-full font-medium text-sm shadow-sm">
                    {shift.day}
                  </span>
                  <span className="text-gray-600 font-medium">{shift.time}</span>
                </div>

                <div className="flex items-center gap-2">
                  <span className="px-3 py-1.5 bg-green-600 text-white rounded-lg text-sm font-medium shadow-sm">
                    {shift.role}
                  </span>
                </div>
              </div>

              <button
                onClick={() => handleRegister(shift)}
                className="px-6 py-2.5 rounded-lg transition-all flex items-center gap-2 font-medium shadow-md bg-gradient-to-r from-green-600 to-green-600 hover:from-green-700 hover:to-green-700 text-white"
              >
                S'inscrire
              </button>
            </div>
          ))}
        </div>
      </div>
    );
  }

  function renderCalendarTab() {
    return (
      <div className="overflow-x-auto">
        <h2 className="mb-4 text-gray-900 text-2xl font-semibold">
          Mes créneaux réservés
        </h2>

        <table className="w-full border-collapse">
          <thead>
            <tr>
              <th className="border border-green-300 bg-green-600 text-white p-3 sticky left-0 z-10">
                Créneaux
              </th>
              {DAYS.map(day => (
                <th
                  key={day}
                  className="border border-green-300 bg-green-600 text-white p-3 min-w-[150px]"
                >
                  {day}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {TIMES.map(time => (
              <tr key={time}>
                <td className="border border-green-300 bg-green-50 p-3 sticky left-0 z-10">
                  <div className="text-green-900 font-medium">{time}</div>
                </td>

                {DAYS.map(day => {
                  const shift = getReservedShift(day, time);

                  return (
                    <td
                      key={`${day}-${time}`}
                      className="border border-gray-200 p-3 align-top bg-white"
                    >
                      {shift ? (
                        <div className="text-center">
                          <span className="px-3 py-1.5 bg-green-600 text-white rounded-lg text-sm font-medium shadow-sm">
                            {shift.role}
                          </span>
                        </div>
                      ) : (
                        <div className="text-gray-400 text-center">-</div>
                      )}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  }

  function renderAvailabilityTab() {
    return (
      <div>
        <h2 className="mb-6 text-gray-900 text-2xl font-semibold">
          Gérer mes disponibilités
        </h2>

        <div className="mb-6">
          <label className="block mb-3 text-gray-700 font-medium">
            Ajouter une disponibilité
          </label>

          <div className="flex gap-3 flex-col md:flex-row">
            <select
              value={availabilityDay}
              onChange={event => setAvailabilityDay(event.target.value)}
              className="flex-1 px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
            >
              <option value="">Sélectionner un jour</option>
              {DAYS.map(day => (
                <option key={day} value={day}>{day}</option>
              ))}
            </select>

            <select
              value={availabilityTime}
              onChange={event => setAvailabilityTime(event.target.value)}
              className="flex-1 px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
            >
              <option value="">Sélectionner un créneau</option>
              {TIMES.map(time => (
                <option key={time} value={time}>{time}</option>
              ))}
            </select>

            <button
              onClick={handleAddAvailability}
              className="px-6 py-3 bg-gradient-to-r from-green-600 to-green-600 hover:from-green-700 hover:to-green-700 text-white rounded-lg transition-all shadow-md font-medium"
            >
              Ajouter
            </button>
          </div>
        </div>

        <div>
          <h3 className="mb-3 text-gray-800 font-medium">
            Mes disponibilités déclarées
          </h3>

          <div className="space-y-2">
            {availabilities.map(item => (
              <div
                key={item.id}
                className="bg-green-50 rounded-lg p-4 flex items-center justify-between border border-green-200 shadow-sm"
              >
                <div className="flex items-center gap-3">
                  <span className="px-3 py-1 bg-green-600 text-white rounded-full font-medium shadow-sm">
                    {item.day}
                  </span>
                  <span className="text-green-700 font-medium">{item.time}</span>
                </div>

                <button
                  onClick={() => handleRemoveAvailability(item.id)}
                  className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-colors shadow-sm font-medium"
                >
                  Retirer
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  function renderProfileTab() {
    return (
      <div className="max-w-2xl mx-auto">
        <h2 className="mb-6 text-gray-900 text-2xl font-semibold">
          Mes informations personnelles
        </h2>

        <div className="space-y-6">
          <div>
            <label className="block mb-2 text-gray-700 font-medium">
              Nom complet
            </label>
            <input
              type="text"
              value={profile.name}
              onChange={event => setProfile({ ...profile, name: event.target.value })}
              className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
              placeholder="Nom et prénom"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block mb-2 text-gray-700 font-medium">Âge</label>
              <input
                type="number"
                value={profile.age}
                onChange={event => setProfile({ ...profile, age: event.target.value })}
                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                placeholder="Âge"
              />
            </div>

            <div>
              <label className="block mb-2 text-gray-700 font-medium">Téléphone</label>
              <input
                type="tel"
                value={profile.phone}
                onChange={event => setProfile({ ...profile, phone: event.target.value })}
                className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                placeholder="06 12 34 56 78"
              />
            </div>
          </div>

          <div>
            <label className="block mb-2 text-gray-700 font-medium">Email</label>
            <input
              type="email"
              value={profile.email}
              onChange={event => setProfile({ ...profile, email: event.target.value })}
              className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
              placeholder="votre.email@exemple.com"
            />
          </div>

          <div className="bg-green-50 border border-green-200 rounded-lg p-5 shadow-sm">
            <h3 className="text-green-800 mb-3 font-medium">Résumé</h3>
            <div className="space-y-2">
              <p className="text-green-700">
                <strong>Créneaux réservés :</strong> {reservedShifts.length}
              </p>
              <p className="text-green-700">
                <strong>Heures inscrites :</strong> {reservedShifts.length * 4}h
              </p>
              <p className="text-green-700">
                <strong>Disponibilités déclarées :</strong> {availabilities.length}
              </p>
            </div>
          </div>

          <button
            onClick={handleSaveProfile}
            className="w-full px-6 py-4 bg-gradient-to-r from-green-600 to-green-600 hover:from-green-700 hover:to-green-700 text-white rounded-lg transition-all shadow-md font-medium"
          >
            Enregistrer les modifications
          </button>
        </div>
      </div>
    );
  }

  function renderContent() {
    if (activeTab === "register") {
      return renderRegisterTab();
    }

    if (activeTab === "calendar") {
      return renderCalendarTab();
    }

    if (activeTab === "availability") {
      return renderAvailabilityTab();
    }

    return renderProfileTab();
  }

  return (
    <div className="min-h-screen bg-green-100">
      <header className="bg-white border-b border-gray-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="bg-gradient-to-br from-green-600 to-green-600 p-2 rounded-lg shadow-md">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="w-6 h-6 text-white"
              >
                <path d="M8 2v4"></path>
                <path d="M16 2v4"></path>
                <rect width="18" height="18" x="3" y="4" rx="2"></rect>
                <path d="M3 10h18"></path>
              </svg>
            </div>

            <div>
              <h1 className="text-gray-900 text-xl font-semibold">Espace Bénévole</h1>
              <p className="text-gray-600">benevole</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button className="flex items-center gap-2 px-4 py-2 bg-green-100 hover:bg-green-200 text-green-700 rounded-lg transition-colors relative">
              Notifications
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center shadow-md">
                3
              </span>
            </button>

            <button
              onClick={() => navigate("/login")}
              className="flex items-center gap-2 px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition-colors border border-gray-300"
            >
              Déconnexion
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow-md border border-gray-200">
          {renderTabs()}

          <div className="p-6">
            {renderContent()}
          </div>
        </div>
      </main>
    </div>
  );
}

export default VolunteerDashboard;
