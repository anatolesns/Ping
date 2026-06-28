
const API_BASE = '/api';

/**
 * Récupère le token stocké localement
 */
export const getToken = () => localStorage.getItem('token');

/**
 * Envoie la requête de connexion
 */
export const login = async (login, password) => {
  const response = await fetch(`${API_BASE}/user/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ login, password }),
  });

  if (!response.ok) {
    throw new Error('Identifiants incorrects');
  }

  const data = await response.json();
  if (data.token) {
    localStorage.setItem('token', data.token);
  }
  return data;
};

/**
 * Déconnecte l'utilisateur
 */
export const logout = () => {
  localStorage.removeItem('token');
};

