import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import AdminDashboard from './pages/AdminDashboard';
import UserDashboard from './pages/UserDashboard';

// Protège une route en vérifiant la présence d'un token et, si un rôle est précisé, l'appartenance de l'utilisateur à ce groupe.
function ProtectedRoute({ children, role }) {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" />;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const groups = payload.groups || [];
    if (role && !groups.includes(role)) return <Navigate to="/login" />;
  } catch {
    return <Navigate to="/login" />;
  }

  return children;
}

// Protège une route réservée aux utilisateurs du groupe "manager".
function ManagerRoute({ children }) {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" />;

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const groups = payload.groups || [];
    if (!groups.includes('manager')) return <Navigate to="/login" />;
  } catch {
    return <Navigate to="/login" />;
  }

  return children;
}

// Composant racine de l'application : définit les routes principales et associe chaque page à sa protection d'accès.
function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/admin"
          element={
            <ProtectedRoute role="admin">
              <AdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/home"
          element={
            <ProtectedRoute role="user">
              <UserDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/manager"
          element={
            <ManagerRoute>
              <AdminDashboard />
            </ManagerRoute>
          }
        />
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
