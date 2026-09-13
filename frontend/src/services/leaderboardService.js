import api from '../utils/axiosConfig';

const leaderboardService = {
  getLeaderboard: async (eventId) => {
    const response = await api.get(`/api/events/${eventId}/leaderboard`);
    return response.data;
  },

  publishResults: async (eventId) => {
    const response = await api.patch(`/api/events/${eventId}/results/publish`);
    return response.data;
  }
};

export default leaderboardService;
