import api from '../utils/axiosConfig';

const evaluationService = {
  evaluateSubmission: async (submissionId, evaluationData) => {
    const response = await api.post(`/api/submissions/${submissionId}/evaluations`, evaluationData);
    return response.data;
  },

  getEvaluationsForSubmission: async (submissionId) => {
    const response = await api.get(`/api/submissions/${submissionId}/evaluations`);
    return response.data;
  }
};

export default evaluationService;
