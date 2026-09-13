import api from '../utils/axiosConfig';

const teamService = {
  createTeam: async (eventId, teamData) => {
    const response = await api.post(`/api/events/${eventId}/teams`, teamData);
    return response.data;
  },

  getTeamsByEvent: async (eventId) => {
    const response = await api.get(`/api/events/${eventId}/teams`);
    return response.data;
  },

  getMyTeams: async () => {
    const response = await api.get(`/api/teams/my`);
    return response.data;
  },

  getTeamById: async (teamId) => {
    const response = await api.get(`/api/teams/${teamId}`);
    return response.data;
  },

  joinTeam: async (teamId) => {
    const response = await api.post(`/api/teams/${teamId}/members`);
    return response.data;
  },

  leaveTeam: async (teamId) => {
    const response = await api.delete(`/api/teams/${teamId}/members/me`);
    return response.data;
  },

  removeMember: async (teamId, userId) => {
    const response = await api.delete(`/api/teams/${teamId}/members/${userId}`);
    return response.data;
  }
};

export default teamService;
