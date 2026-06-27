import React from 'react';
import { Routes, Route, useNavigate } from 'react-router-dom';

function VolunteerHome() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-green-100 flex items-center justify-center p-4">
      <div className="bg-white rounded-xl shadow-md border border-gray-200 p-8 max-w-lg text-center">
        <h1 className="text-2xl font-semibold text-gray-900 mb-4">Espace Bénévole</h1>
        <p className="text-gray-600 mb-6">
          Cette partie sera développée par mon collègue. 
          L'interface et la logique de gestion des créneaux (récupération, inscription) 
          seront implémentées ici.
        </p>
        <button 
          onClick={() => navigate('/')} 
          className="px-6 py-2.5 rounded-lg font-medium bg-gray-100 text-gray-700 hover:bg-gray-200"
        >
          Retour à l'accueil
        </button>
      </div>
    </div>
  );
}

function VolunteerRoutes() {
  return (
    <Routes>
      <Route path="/" element={<VolunteerHome />} />
    </Routes>
  );
}

export default VolunteerRoutes;
