import axiosClient from './axiosClient'

export const authApi = {
  login: (data) => axiosClient.post('/auth/login', data),
  me: () => axiosClient.get('/auth/me'),
  changePassword: (data) => axiosClient.put('/auth/change-password', data),
  logout: () => axiosClient.post('/auth/logout'),
}

export const userApi = {
  getAll: (params) => axiosClient.get('/users', { params }),
  getById: (id) => axiosClient.get(`/users/${id}`),
  create: (data) => axiosClient.post('/users', data),
  update: (id, data) => axiosClient.put(`/users/${id}`, data),
  updateStatus: (id, data) => axiosClient.put(`/users/${id}/status`, data),
  updateRole: (id, data) => axiosClient.put(`/users/${id}/role`, data),
  delete: (id) => axiosClient.delete(`/users/${id}`),
}

export const studentApi = {
  getAll: () => axiosClient.get('/students'),
  getById: (id) => axiosClient.get(`/students/${id}`),
  create: (data) => axiosClient.post('/students', data),
  update: (id, data) => axiosClient.put(`/students/${id}`, data),
  delete: (id) => axiosClient.delete(`/students/${id}`),
}

export const mentorApi = {
  getAll: () => axiosClient.get('/mentors'),
  getById: (id) => axiosClient.get(`/mentors/${id}`),
  create: (data) => axiosClient.post('/mentors', data),
  update: (id, data) => axiosClient.put(`/mentors/${id}`, data),
  delete: (id) => axiosClient.delete(`/mentors/${id}`),
}

export const phaseApi = {
  getAll: (params) => axiosClient.get('/internship-phases', { params }),
  getById: (id) => axiosClient.get(`/internship-phases/${id}`),
  create: (data) => axiosClient.post('/internship-phases', data),
  update: (id, data) => axiosClient.put(`/internship-phases/${id}`, data),
  delete: (id) => axiosClient.delete(`/internship-phases/${id}`),
}

export const assignmentApi = {
  getAll: (params) => axiosClient.get('/internship_assignments', { params }),
  getById: (id) => axiosClient.get(`/internship_assignments/${id}`),
  create: (data) => axiosClient.post('/internship_assignments', data),
  update: (id, data) => axiosClient.put(`/internship_assignments/${id}`, data),
  updateStatus: (id, data) => axiosClient.put(`/internship_assignments/${id}/status`, data),
  delete: (id) => axiosClient.delete(`/internship_assignments/${id}`),
}

export const roundApi = {
  getAll: (params) => axiosClient.get('/assessment-rounds', { params }),
  getById: (id) => axiosClient.get(`/assessment-rounds/${id}`),
  create: (data) => axiosClient.post('/assessment-rounds', data),
  update: (id, data) => axiosClient.put(`/assessment-rounds/${id}`, data),
  delete: (id) => axiosClient.delete(`/assessment-rounds/${id}`),
}

export const criterionApi = {
  getAll: () => axiosClient.get('/evaluation-criteria'),
  getById: (id) => axiosClient.get(`/evaluation-criteria/${id}`),
  create: (data) => axiosClient.post('/evaluation-criteria', data),
  update: (id, data) => axiosClient.put(`/evaluation-criteria/${id}`, data),
  delete: (id) => axiosClient.delete(`/evaluation-criteria/${id}`),
}

export const roundCriterionApi = {
  getAll: (params) => axiosClient.get('/round_criteria', { params }),
  getById: (id) => axiosClient.get(`/round_criteria/${id}`),
  create: (data) => axiosClient.post('/round_criteria', data),
  update: (id, data) => axiosClient.put(`/round_criteria/${id}`, data),
  delete: (id) => axiosClient.delete(`/round_criteria/${id}`),
}

export const resultApi = {
  getAll: (params) => axiosClient.get('/assessment_results', { params }),
  getById: (id) => axiosClient.get(`/assessment_results/${id}`),
  create: (data) => axiosClient.post('/assessment_results', data),
  update: (id, data) => axiosClient.put(`/assessment_results/${id}`, data),
  delete: (id) => axiosClient.delete(`/assessment_results/${id}`),
}

export const cvReviewApi = {
  reviewWithGemini: (file, apiKey) => {
    const formData = new FormData()
    formData.append('file', file)
    return axiosClient.post('/cv/review', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'X-Gemini-Api-Key': apiKey,
      },
    })
  },
  reviewWithOpenAi: (file, apiKey) => {
    const formData = new FormData()
    formData.append('file', file)
    return axiosClient.post('/cv/review/openai', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'X-OpenAI-Api-Key': apiKey,
      },
    })
  },
}

export const jobSearchApi = {
  search: (params) => axiosClient.get('/jobs/search', { params }),
  searchByMajor: (params) => axiosClient.get('/jobs/search/by-major', { params }),
}
