// Configuration globale de l'API

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
  // On stocke le token pour les prochaines requêtes
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

/**
 * Fonction utilitaire pour lire un fichier texte (JSON) depuis l'API de fichiers
 */
export const getJsonFile = async (path) => {
  const token = getToken();
  const response = await fetch(`${API_BASE}/files?path=${encodeURIComponent(path)}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    if (response.status === 404) return null; // Le fichier n'existe pas encore
    throw new Error('Erreur lors de la lecture du fichier');
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
};

/**
 * Fonction utilitaire pour sauvegarder un fichier JSON via l'API d'upload
 */
export const saveJsonFile = async (path, jsonContent) => {
  const token = getToken();
  
  // L'API attend de l'application/octet-stream
  const blob = new Blob([JSON.stringify(jsonContent, null, 2)], { type: 'application/octet-stream' });

  const response = await fetch(`${API_BASE}/files/upload?path=${encodeURIComponent(path)}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: blob
  });

  if (!response.ok) {
    throw new Error('Erreur lors de la sauvegarde du fichier');
  }

  return true;
};
