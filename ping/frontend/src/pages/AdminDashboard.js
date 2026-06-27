import { useNavigate } from 'react-router-dom';
import './AdminDashboard.css';

function AdminDashboard() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="admin-page">
      <div className="admin-card">
        <div className="admin-icon">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="white">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
          </svg>
        </div>
        <h1 className="admin-title">Bienvenue, Administrateur</h1>
        <p className="admin-subtitle">Vous êtes connecté en tant qu'admin</p>
        <button className="admin-logout" onClick={handleLogout}>
          Se déconnecter
        </button>
      </div>
    </div>
  );
}

export default AdminDashboard;
