import api from '../utils/axiosConfig';

const eventService = {
  getPublishedEvents: async (params = {}) => {
    let url = '/api/events?';
    if (params.page !== undefined) url += `page=${params.page}&`;
    if (params.size !== undefined) url += `size=${params.size}&`;
    if (params.search) url += `search=${params.search}&`;
    if (params.type) url += `type=${params.type}&`;
    if (params.registrationType) url += `registrationType=${params.registrationType}&`;
    if (params.sortBy) url += `sortBy=${params.sortBy}&`;
    if (params.sortDir) url += `sortDir=${params.sortDir}&`;
    
    // Remove trailing '&' or '?'
    url = url.endsWith('&') || url.endsWith('?') ? url.slice(0, -1) : url;

    const response = await api.get(url);
    return {
      events: response.data.content || [],
      totalElements: response.data.totalElements || 0,
      totalPages: response.data.totalPages || 0,
      currentPage: response.data.number || 0
    };
  },
  
  getOrganizerEvents: async (organizerId) => {
    const response = await api.get(`/api/events?organizerId=${organizerId}&size=100`);
    return {
      events: response.data.content || [],
      totalElements: response.data.totalElements || 0,
      totalPages: response.data.totalPages || 0,
      currentPage: response.data.number || 0
    };
  },
  
  getEventById: async (id) => {
    const response = await api.get(`/api/events/${id}`);
    return response.data;
  },
  
  createEvent: async (eventData) => {
    const response = await api.post('/api/events', eventData);
    return response.data;
  },

  updateEvent: async (id, eventData) => {
    const response = await api.put(`/api/events/${id}`, eventData);
    return response.data;
  },

  deleteEvent: async (id) => {
    const response = await api.delete(`/api/events/${id}`);
    return response.data;
  },
  
  publishEvent: async (id) => {
    const response = await api.patch(`/api/events/${id}/publish`);
    return response.data;
  }
};

export default eventService;
