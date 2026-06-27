import { useNavigate } from 'react-router-dom';
import './UserDashboard.css';

function UserDashboard() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="user-page">
      <div className="user-card">
        <div className="user-icon">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="white">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
          </svg>
        </div>
        <h1 className="user-title">Bienvenue, Bénévole</h1>
        <p className="user-subtitle">Vous êtes connecté en tant que bénévole</p>
        <button className="user-logout" onClick={handleLogout}>
          Se déconnecter
        </button>
      </div>
    </div>
  );
}

export default UserDashboard;
