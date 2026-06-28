const API_BASE = "/api";

export const getToken = () => localStorage.getItem("token");

export const login = async (login, password) => {
  const response = await fetch(`${API_BASE}/user/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ login, password })
  });

  if (!response.ok) {
    throw new Error("Identifiants incorrects");
  }

  const data = await response.json();

  if (data.token) {
    localStorage.setItem("token", data.token);
  }

  return data;
};

export const logout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("currentVolunteerLogin");
};

export const getJsonFile = async (path) => {
  const token = getToken();

  if (!token) {
    return null;
  }

  const response = await fetch(`${API_BASE}/files?path=${encodeURIComponent(path)}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  if (!response.ok) {
    if (response.status === 404 || response.status === 401) {
      return null;
    }

    throw new Error("Erreur lors de la lecture du fichier");
  }

  const text = await response.text();

  if (!text) {
    return null;
  }

  return JSON.parse(text);
};

export const saveJsonFile = async (path, jsonContent) => {
  const token = getToken();

  if (!token) {
    return false;
  }

  const blob = new Blob([JSON.stringify(jsonContent, null, 2)], {
    type: "application/octet-stream"
  });

  const response = await fetch(`${API_BASE}/files/upload?path=${encodeURIComponent(path)}`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`
    },
    body: blob
  });

  if (!response.ok) {
    if (response.status === 401) {
      return false;
    }

    throw new Error("Erreur lors de la sauvegarde du fichier");
  }

  return true;
};