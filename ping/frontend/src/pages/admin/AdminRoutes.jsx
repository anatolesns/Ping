import React from 'react';
import { Routes, Route, Link } from 'react-router-dom';

function AdminHome() {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Dashboard Administrateur</h1>
      <p>Ici la Personne A pourra coder les vues de gestion (création de créneaux, liste des bénévoles...).</p>
      <Link to="/">Retour à la connexion</Link>
    </div>
  );
}

function AdminRoutes() {
  return (
    <Routes>
      <Route path="/" element={<AdminHome />} />
      {/* Ajoutez d'autres sous-routes ici, ex: */}
      {/* <Route path="/users" element={<ManageUsers />} /> */}
    </Routes>
  );
}

export default AdminRoutes;
