import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

function Login() {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const res = await fetch('/api/user/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login, password }),
      });

      if (!res.ok) {
        const data = await res.json();
        setError(data.message || 'Identifiants incorrects');
        return;
      }

      const data = await res.json();
      localStorage.setItem('token', data.token);
      localStorage.setItem('username', login);

      const payload = JSON.parse(atob(data.token.split('.')[1]));
      const groups = payload.groups || [];

      if (groups.includes('admin')) {
        navigate('/admin');
      } else if (groups.includes('manager')) {
        navigate('/manager');
      } else {
        navigate('/home');
      }
    } catch {
      setError('Erreur de connexion au serveur');
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">
          <svg viewBox="0 0 24 24" width="40" height="40" fill="white">
            <path d="M17,8C8,10 5.9,16.17 3.82,21.34L5.71,22L6.66,19.7C7.14,19.87 7.64,20 8,20C19,20 22,3 22,3C21,5 14,5.25 9,6.25C4,7.25 2,11.5 2,13.5C2,15.5 3.75,17.25 3.75,17.25C7,8 17,8 17,8Z" />
          </svg>
        </div>

        <h1 className="login-title">Salon Décarbonisation</h1>
        <p className="login-subtitle">Eurexpo Lyon</p>

        {error && <p className="login-error">{error}</p>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Adresse mail</label>
            <input
              type="text"
              className="form-input"
              placeholder="email or phone number"
              value={login}
              onChange={(e) => setLogin(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Mot de passe</label>
            <input
              type="password"
              className="form-input"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <button type="submit" className="login-button">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="white" style={{ marginRight: 8 }}>
              <path d="M11 7L9.6 8.4l2.6 2.6H2v2h10.2l-2.6 2.6L11 17l5-5-5-5zm9 12h-8v2h8c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-8v2h8v14z" />
            </svg>
            Se connecter
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;
