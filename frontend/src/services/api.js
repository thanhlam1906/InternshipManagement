import axiosClient from './axiosClient'

export const authApi = {
  login: (data) => axiosClient.post('/auth/login', data),
  register: (data) => axiosClient.post('/auth/register', data),
  me: () => axiosClient.get('/auth/me'),
  changePassword: (data) => axiosClient.put('/auth/change-password', data),
  logout: () => axiosClient.post('/auth/logout'),
}

export const userApi = {
  getAll: (params, config) => axiosClient.get('/users', { params, ...config }),
  getById: (id) => axiosClient.get(`/users/${id}`),
  create: (data) => axiosClient.post('/users', data),
  update: (id, data) => axiosClient.put(`/users/${id}`, data),
  updateStatus: (id, data) => axiosClient.put(`/users/${id}/status`, data),
  updateRole: (id, data) => axiosClient.put(`/users/${id}/role`, data),
  delete: (id) => axiosClient.delete(`/users/${id}`),
}

export const studentApi = {
  getAll: (params, config) => axiosClient.get('/students', { params, ...config }),
  getById: (id) => axiosClient.get(`/students/${id}`),
  create: (data) => axiosClient.post('/students', data),
  update: (id, data) => axiosClient.put(`/students/${id}`, data),
  delete: (id) => axiosClient.delete(`/students/${id}`),
}

export const mentorApi = {
  getAll: (params, config) => axiosClient.get('/mentors', { params, ...config }),
  getById: (id) => axiosClient.get(`/mentors/${id}`),
  create: (data) => axiosClient.post('/mentors', data),
  update: (id, data) => axiosClient.put(`/mentors/${id}`, data),
  delete: (id) => axiosClient.delete(`/mentors/${id}`),
}

export const phaseApi = {
  getAll: (params, config) => axiosClient.get('/internship-phases', { params, ...config }),
  getById: (id) => axiosClient.get(`/internship-phases/${id}`),
  create: (data) => axiosClient.post('/internship-phases', data),
  update: (id, data) => axiosClient.put(`/internship-phases/${id}`, data),
  delete: (id) => axiosClient.delete(`/internship-phases/${id}`),
}

export const assignmentApi = {
  getAll: (params, config) => axiosClient.get('/internship-assignments', { params, ...config }),
  getById: (id) => axiosClient.get(`/internship-assignments/${id}`),
  create: (data) => axiosClient.post('/internship-assignments', data),
  update: (id, data) => axiosClient.put(`/internship-assignments/${id}`, data),
  updateStatus: (id, data) => axiosClient.put(`/internship-assignments/${id}/status`, data),
  delete: (id) => axiosClient.delete(`/internship-assignments/${id}`),
}

export const roundApi = {
  getAll: (params, config) => axiosClient.get('/assessment-rounds', { params, ...config }),
  getById: (id) => axiosClient.get(`/assessment-rounds/${id}`),
  create: (data) => axiosClient.post('/assessment-rounds', data),
  update: (id, data) => axiosClient.put(`/assessment-rounds/${id}`, data),
  delete: (id) => axiosClient.delete(`/assessment-rounds/${id}`),
}

export const criterionApi = {
  getAll: (params, config) => axiosClient.get('/evaluation-criteria', { params, ...config }),
  getById: (id) => axiosClient.get(`/evaluation-criteria/${id}`),
  create: (data) => axiosClient.post('/evaluation-criteria', data),
  update: (id, data) => axiosClient.put(`/evaluation-criteria/${id}`, data),
  delete: (id) => axiosClient.delete(`/evaluation-criteria/${id}`),
}

export const roundCriterionApi = {
  getAll: (params, config) => axiosClient.get('/round-criteria', { params, ...config }),
  getById: (id) => axiosClient.get(`/round-criteria/${id}`),
  create: (data) => axiosClient.post('/round-criteria', data),
  update: (id, data) => axiosClient.put(`/round-criteria/${id}`, data),
  delete: (id) => axiosClient.delete(`/round-criteria/${id}`),
}

export const resultApi = {
  getAll: (params, config) => axiosClient.get('/assessment-results', { params, ...config }),
  getById: (id) => axiosClient.get(`/assessment-results/${id}`),
  create: (data) => axiosClient.post('/assessment-results', data),
  update: (id, data) => axiosClient.put(`/assessment-results/${id}`, data),
  delete: (id) => axiosClient.delete(`/assessment-results/${id}`),
}

export const cvReviewApi = {
  reviewWithGemini: (file, apiKey) => {
    const formData = new FormData()
    formData.append('file', file)
    // Do NOT set Content-Type manually — axios auto-detects FormData
    // and sets the correct multipart/form-data header WITH boundary.
    return axiosClient.post('/cv/review', formData, {
      headers: {
        'X-Gemini-Api-Key': apiKey,
      },
    })
  },
  reviewWithOpenAi: (file, apiKey) => {
    const formData = new FormData()
    formData.append('file', file)
    // Do NOT set Content-Type manually — axios auto-detects FormData
    // and sets the correct multipart/form-data header WITH boundary.
    return axiosClient.post('/cv/review/openai', formData, {
      headers: {
        'X-OpenAI-Api-Key': apiKey,
      },
    })
  },
}

export const jobSearchApi = {
  search: (params, config) => axiosClient.get('/jobs/search', { params, ...config }),
  searchByMajor: (params, config) => axiosClient.get('/jobs/search/by-major', { params, ...config }),
}

export const dashboardApi = {
  getStats: () => axiosClient.get('/dashboard'),
}
