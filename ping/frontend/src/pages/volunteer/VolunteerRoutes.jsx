import React from 'react';
import { Routes, Route, Link } from 'react-router-dom';

function VolunteerHome() {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Espace Bénévole</h1>
      <p>Ici la Personne B pourra coder les vues du bénévole (Onglets, Inscription aux créneaux, Calendrier...).</p>
      <Link to="/">Retour à la connexion</Link>
    </div>
  );
}

function VolunteerRoutes() {
  return (
    <Routes>
      <Route path="/" element={<VolunteerHome />} />
      {/* Ajoutez d'autres sous-routes ici, ex: */}
      {/* <Route path="/calendrier" element={<MyCalendar />} /> */}
    </Routes>
  );
}

export default VolunteerRoutes;
