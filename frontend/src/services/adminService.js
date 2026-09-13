import api from '../utils/axiosConfig';

const adminService = {
  getSystemStats: async () => {
    const response = await api.get('/api/admin/stats');
    return response.data;
  },

  searchUsers: async (params) => {
    const response = await api.get('/api/admin/users', { params });
    return response.data;
  },

  updateUserStatus: async (userId, enabled) => {
    const response = await api.patch(`/api/admin/users/${userId}/${enabled ? 'activate' : 'deactivate'}`);
    return response.data;
  }
};

export default adminService;
