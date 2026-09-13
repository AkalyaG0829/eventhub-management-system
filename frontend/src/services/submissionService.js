import api from '../utils/axiosConfig';

const submissionService = {
  createSubmission: async (teamId, submissionData) => {
    const response = await api.post(`/api/teams/${teamId}/submissions`, submissionData);
    return response.data;
  },

  getTeamSubmissions: async (teamId) => {
    const response = await api.get(`/api/teams/${teamId}/submissions`);
    return response.data;
  },

  getMySubmissions: async () => {
    const response = await api.get('/api/submissions/my');
    return response.data;
  },

  getSubmissionById: async (submissionId) => {
    const response = await api.get(`/api/submissions/${submissionId}`);
    return response.data;
  },

  updateSubmission: async (submissionId, submissionData) => {
    const response = await api.put(`/api/submissions/${submissionId}`, submissionData);
    return response.data;
  },

  deleteSubmission: async (submissionId) => {
    const response = await api.delete(`/api/submissions/${submissionId}`);
    return response.data;
  }
};

export default submissionService;
