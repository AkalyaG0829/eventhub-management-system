import api from '../utils/axiosConfig';

const registrationService = {
  registerForEvent: async (eventId, requestData = null) => {
    const response = await api.post(`/api/events/${eventId}/register`, requestData);
    return response.data;
  },

  getMyRegistrations: async () => {
    const response = await api.get('/api/registrations/my');
    return response.data;
  },

  getEventRegistrations: async (eventId) => {
    const response = await api.get(`/api/events/${eventId}/registrations`);
    return response.data;
  },

  simulatePayment: async (eventId, registrationId, success) => {
    const response = await api.post(`/api/events/${eventId}/registrations/${registrationId}/test-payment?success=${success}`);
    return response.data;
  }
};

export default registrationService;
