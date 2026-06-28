import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/auth/Login';
import AdminRoutes from './pages/admin/AdminRoutes';
import VolunteerRoutes from './pages/volunteer/VolunteerRoutes';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Racine redirige vers login par défaut, ou selon votre logique */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        
        {/* Page de connexion commune */}
        <Route path="/login" element={<Login />} />
        
        {/* L'Espace Admin */}
        <Route path="/admin/*" element={<AdminRoutes />} />
        
        {/* L'Espace Bénévole */}
        <Route path="/benevole/*" element={<VolunteerRoutes />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
