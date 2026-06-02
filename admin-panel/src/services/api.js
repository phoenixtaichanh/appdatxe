import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('admin_token');
      localStorage.removeItem('admin_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: (email, password) => api.post('/admin-auth/login', { email, password }),
};

export const supportAPI = {
  // Chat conversations
  conversations: (params) => api.get('/admin-support/conversations', { params }),
  conversation: (id) => api.get(`/admin-support/conversations/${id}`),
  messages: (id) => api.get(`/admin-support/conversations/${id}/messages`),
  reply: (id, message) => api.post(`/admin-support/conversations/${id}/reply`, { message }),
  resolve: (id) => api.put(`/admin-support/conversations/${id}/resolve`),
  assign: (id, consultantId) => api.put(`/admin-support/conversations/${id}/assign`, { consultant_id: consultantId }),
  // FAQs
  faqs: () => api.get('/admin-support/faqs'),
};

export const adminAPI = {
  dashboard: () => api.get('/admin/dashboard'),
  users: (params) => api.get('/admin/users', { params }),
  updateUserStatus: (id, is_active, reason) => api.put(`/admin/users/${id}/status`, { is_active, reason }),
  rides: (params) => api.get('/admin/rides', { params }),
  updateRideStatus: (id, status) => api.put(`/admin/rides/${id}/status`, { status }),
  drivers: (params) => api.get('/admin/drivers', { params }),
  statsDaily: (days) => api.get('/admin/stats/daily', { params: { days } }),
  statsRevenue: () => api.get('/admin/stats/revenue'),
};

export default api;
