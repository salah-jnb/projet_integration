import axios from 'axios';

export const API_BASE_URL = 'http://localhost:5079';

export const api = axios.create({
  baseURL: API_BASE_URL,
});

// Add JWT token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jnb_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    if (config.data instanceof FormData) {
      if (config.headers) {
        delete (config.headers as any)['Content-Type'];
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle 401 errors (token expired)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jnb_token');
      localStorage.removeItem('jnb_user');
      const path = window.location.pathname || '/';
      const isPublicPath =
        path === '/' ||
        path.startsWith('/login') ||
        path.startsWith('/register');
      if (!isPublicPath) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);


// Auth API
export const authApi = {
  register: (data) => api.post('/api/Auth/register', data),
  login: (email, motDePasse) => api.post('/api/Auth/login', { email, motDePasse }),
  changePassword: (utilisateurId, ancienMotDePasse, nouveauMotDePasse) =>
    api.post(`/api/Auth/change-password/${utilisateurId}`, { ancienMotDePasse, nouveauMotDePasse }),
};


// Users API
export const usersApi = {
  getAll: () => api.get(`/api/Utilisateurs`),
  getById: (id) => api.get(`/api/Utilisateurs/${id}`),
  getByEmail: (email) => api.get(`/api/Utilisateurs/by-email/${email}`),
  update: (id, data) => api.put(`/api/Utilisateurs/${id}`, data),
  uploadPhoto: (id, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.put(`/api/Utilisateurs/${id}/photo`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  getPhoto: (id) => api.get(`/api/Utilisateurs/${id}/photo`, { responseType: 'blob' }),
  delete: (id) => api.delete(`/api/Utilisateurs/${id}`),
  create: (data) => api.post(`/api/Utilisateurs`, data),              // AJOUT - Création utilisateur
  updateStatus: (id, statut) => api.put(`/api/Utilisateurs/${id}/statut/${statut}`),
  updateNewsletter: (id, abonne) => api.put(`/api/Utilisateurs/${id}/newsletter/${abonne}`),
};


// Clients API
export const clientsApi = {
  getByUserId: (utilisateurId) => api.get(`/api/Clients/${utilisateurId}`),
  getAll: () => api.get('/api/Clients'),
  getByParrainageCode: (codeParrainage) => 
    api.get(`/api/Clients/parrainage/${codeParrainage}`), // AJOUT
};



// Coachs API
export const coachsApi = {
  getAll: () => api.get('/api/Coachs'),
  getById: (utilisateurId) => api.get(`/api/Coachs/${utilisateurId}`),
  getDetails: (utilisateurId) => api.get(`/api/Coachs/${utilisateurId}/details`),
  updateProfile: (utilisateurId, data) => api.put(`/api/Coachs/${utilisateurId}`, data),
};


// Abonnements API
export const abonnementsApi = {
  getAll: () => api.get('/api/Abonnements'),
  getAllActive: () => api.get('/api/Abonnements/actifs'),
  create: (data) => api.post('/api/Abonnements', data),
  getByClient: (clientId) => api.get(`/api/Abonnements/client/${clientId}`),
  getActiveByClient: (clientId) => api.get(`/api/Abonnements/client/${clientId}/actifs`),
  getById: (id) => api.get(`/api/Abonnements/${id}`),
  cancel: (id) => api.delete(`/api/Abonnements/${id}`),
  getTypes: () => api.get('/api/Abonnements/types'),                  // AJOUT - Types d'abonnement
  createType: (data) => api.post('/api/Abonnements/types', data),     // AJOUT
  updateType: (id, data) => api.put(`/api/Abonnements/types/${id}`, data),
  deleteType: (id) => api.delete(`/api/Abonnements/types/${id}`),
};


// Reservations Coaching API
export const reservationsCoachingApi = {
  create: (data) => api.post('/api/ReservationsCoaching', data),
  getByClient: (clientId) => api.get(`/api/ReservationsCoaching/client/${clientId}`),
  getByCoach: (coachId) => api.get(`/api/ReservationsCoaching/coach/${coachId}`),
  getById: (id) => api.get(`/api/ReservationsCoaching/${id}`),        // AJOUT
  cancel: (id) => api.delete(`/api/ReservationsCoaching/${id}`),
  complete: (id) => api.put(`/api/ReservationsCoaching/${id}/complete`),      // AJOUT
  checkAvailability: (coachId, dateSeance, dureeMinutes) =>           // AJOUT
    api.get(`/api/ReservationsCoaching/check-availability?coachId=${coachId}&dateSeance=${dateSeance}&dureeMinutes=${dureeMinutes}`),
};


// Cours Collectifs API
export const coursCollectifsApi = {
  getAll: () => api.get('/api/CoursCollectifs'),
  getById: (id) => api.get(`/api/CoursCollectifs/${id}`),
  getAvailableSessions: () => api.get('/api/CoursCollectifs/seances/disponibles'),
  getAvailableSessionsByCoach: (coachId) => api.get(`/api/CoursCollectifs/coach/${coachId}/seances/disponibles`),
  getAllSessions: () => api.get('/api/CoursCollectifs/seances'),
  create: (data) => api.post('/api/CoursCollectifs', data),
  update: (id, data) => api.put(`/api/CoursCollectifs/${id}`, data),
  delete: (id) => api.delete(`/api/CoursCollectifs/${id}`),
  createSeance: (data) => api.post('/api/CoursCollectifs/seances', data),
  updateSeance: (id, data) => api.put(`/api/CoursCollectifs/seances/${id}`, data),
  cancelSeance: (id) => api.put(`/api/CoursCollectifs/seances/${id}/annuler`),
  deleteSeance: (id) => api.delete(`/api/CoursCollectifs/seances/${id}`),
};


// Reservations Cours API
export const reservationsCoursApi = {
  create: (data) => api.post('/api/ReservationsCours', data),
  getByClient: (clientId) => api.get(`/api/ReservationsCours/client/${clientId}`),
  getBySeance: (seanceId) => api.get(`/api/ReservationsCours/seance/${seanceId}`),
  cancel: (id) => api.delete(`/api/ReservationsCours/${id}`),
};

// Notations (Avis) API
export const notationsApi = {
  create: (data) => api.post('/api/Notations', data),
  getByCoach: (coachId) => api.get(`/api/Notations/coach/${coachId}`),
  getByReservation: (reservationId) => api.get(`/api/Notations/reservation/${reservationId}`),
};


// Paiements API
export const paiementsApi = {
  getAll: () => api.get('/api/Paiements'),
  create: (data) => api.post('/api/Paiements', data),
  getByClient: (clientId) => api.get(`/api/Paiements/client/${clientId}`),
};


// Cartes API
export const cartesApi = {
  getAll: () => api.get('/api/Cartes'),
  getByUser: (utilisateurId) => api.get(`/api/Cartes/utilisateur/${utilisateurId}`),
  recharger: (carteId, montant) =>
    api.post(`/api/Cartes/${carteId}/recharger`, montant, { headers: { 'Content-Type': 'application/json' } }),
  diminuer: (carteId, montant) =>
    api.post(`/api/Cartes/${carteId}/diminuer`, montant, { headers: { 'Content-Type': 'application/json' } }),     // AJOUT
  transferer: (data) => api.post(`/api/Cartes/transferer`, data),                         // AJOUT
};


// Transferts API
export const transfertsApi = {
  create: (data) => api.post('/api/Transferts', data),
  getByCarte: (carteId) => api.get(`/api/Transferts/carte/${carteId}`),
};


// Articles API
export const articlesApi = {
  getAll: () => api.get('/api/Articles'),
  getPending: () => api.get('/api/Articles/en-attente'),
  getById: (id) => api.get(`/api/Articles/${id}`),
  getByCoach: (coachId) => api.get(`/api/Articles/coach/${coachId}`),
  create: (data) => api.post('/api/Articles', data),
  update: (id, data) => api.put(`/api/Articles/${id}`, data),
  validate: (id, data) => api.put(`/api/Articles/${id}/valider`, data),
  submitForValidation: (id) => api.put(`/api/Articles/${id}/soumettre`),
  delete: (id) => api.delete(`/api/Articles/${id}`),
};


// Notifications API
export const notificationsApi = {
  getByUser: (utilisateurId) => api.get(`/api/Notifications/utilisateur/${utilisateurId}`),
  getUnreadByUser: (utilisateurId) => api.get(`/api/Notifications/utilisateur/${utilisateurId}/non-lues`),
  create: (data) => api.post('/api/Notifications', data),
  markAsRead: (id) => api.put(`/api/Notifications/${id}/lire`),
};


// Parrainages API
export const parrainagesApi = {
  getByParrain: (parrainId) => api.get(`/api/Parrainages/parrain/${parrainId}`),
  getCount: (parrainId) => api.get(`/api/Parrainages/parrain/${parrainId}/count`),
  getByFilleul: (filleulId) => api.get(`/api/Parrainages/filleul/${filleulId}`),           // AJOUT
  getById: (id) => api.get(`/api/Parrainages/${id}`),                                     // AJOUT
  validate: (id) => api.put(`/api/Parrainages/${id}/valider`),                            // AJOUT
  getCode: (clientId) => api.get(`/api/Parrainages/client/${clientId}/code`),             // AJOUT
  create: (data) => api.post(`/api/Parrainages`, data),                                   // AJOUT
};


// Produits API
export const produitsApi = {
  getAll: () => api.get('/api/Produits'),
  getAllAdmin: () => api.get('/api/Produits/all'),
  create: (data) => {
    if (data instanceof FormData) {
      return api.post('/api/Produits', data);
    }
    return api.post('/api/Produits', data);
  },
  update: (id, data) => {
    if (data instanceof FormData) {
      return api.put(`/api/Produits/${id}`, data);
    }
    return api.put(`/api/Produits/${id}`, data);
  },
  delete: (id) => api.delete(`/api/Produits/${id}`),
};


// Disponibilités API
export const disponibilitesApi = {
  getByCoach: (coachId) => api.get(`/api/Disponibilites/coach/${coachId}`),
  create: (coachId, data) => api.post(`/api/Disponibilites/coach/${coachId}`, data),
  update: (id, data) => api.put(`/api/Disponibilites/${id}`, data),
  delete: (id) => api.delete(`/api/Disponibilites/${id}`),
};
