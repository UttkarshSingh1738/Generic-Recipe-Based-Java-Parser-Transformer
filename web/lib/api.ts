import axios from 'axios'

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

export default api

// Recipe API
export const recipeApi = {
  getAll: () => api.get('/api/recipes'),
  getById: (id: number) => api.get(`/api/recipes/${id}`),
  create: (data: any) => api.post('/api/recipes', data),
  update: (id: number, data: any) => api.put(`/api/recipes/${id}`, data),
  delete: (id: number) => api.delete(`/api/recipes/${id}`),
  search: (query: string) => api.get(`/api/recipes/search?q=${encodeURIComponent(query)}`),
  generate: (intent: string) => api.post('/api/recipes/generate', { intent }),
  generateAndSave: (data: { intent: string; author?: string; category?: string }) =>
    api.post('/api/recipes/generate/save', data),
  // Recipe discovery from resources
  getDiscovered: () => api.get('/api/recipes/discovery'),
  getDiscoveredByName: (name: string) => api.get(`/api/recipes/discovery/${name}`),
  getDiscoveredContent: (name: string) => api.get(`/api/recipes/discovery/${name}/content`, {
    responseType: 'text', // Get raw text response for JSON content
  }),
}

// Project API
export const projectApi = {
  getAll: () => api.get('/api/projects'),
  getById: (id: number) => api.get(`/api/projects/${id}`),
  create: (data: any) => api.post('/api/projects', data),
  update: (id: number, data: any) => api.put(`/api/projects/${id}`, data),
  delete: (id: number) => api.delete(`/api/projects/${id}`),
  upload: (id: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post(`/api/projects/${id}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

// Job API
export const jobApi = {
  getAll: () => api.get('/api/jobs'),
  getById: (id: number) => api.get(`/api/jobs/${id}`),
  create: (data: any) => api.post('/api/jobs', data),
  getByProject: (projectId: number) => api.get(`/api/jobs/project/${projectId}`),
  getByStatus: (status: string) => api.get(`/api/jobs/status/${status}`),
  updateStatus: (id: number, status: string) =>
    api.patch(`/api/jobs/${id}/status?status=${status}`),
  // Job diffs
  getDiffs: (jobId: number) => api.get(`/api/jobs/${jobId}/diffs`),
  getDiff: (jobId: number, recipeName: string) => api.get(`/api/jobs/${jobId}/diffs/${recipeName}`),
  // Job outputs
  getRecipeOutputs: (jobId: number) => api.get(`/api/jobs/${jobId}/output/recipes`),
  getRecipeFiles: (jobId: number, recipeName: string) => api.get(`/api/jobs/${jobId}/output/recipes/${recipeName}/files`),
  getFile: (jobId: number, recipeName: string, path: string) => 
    api.get(`/api/jobs/${jobId}/output/recipes/${recipeName}`, { params: { path } }),
  getLog: (jobId: number, recipeName: string) => api.get(`/api/jobs/${jobId}/output/logs/${recipeName}`),
}

