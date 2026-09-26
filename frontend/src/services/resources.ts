import { api, asList, asRecord } from './api'
import type { Entity } from './types'

export function resourceService(resource: string) {
  return {
    async list(params?: Record<string, string | number | undefined>) {
      const response = await api.get(`/${resource}`, { params })
      return asList<Entity>(response.data)
    },
    async get(id: string | number) {
      const response = await api.get(`/${resource}/${id}`)
      return asRecord<Entity>(response.data)
    },
    async create(payload: Record<string, unknown>) {
      const response = await api.post(`/${resource}`, payload)
      return asRecord<Entity>(response.data)
    },
    async update(id: string | number, payload: Record<string, unknown>) {
      const response = await api.put(`/${resource}/${id}`, payload)
      return asRecord<Entity>(response.data)
    },
    async validate(id: string | number) {
      const response = await api.post(`/${resource}/${id}/validate`)
      return asRecord<Entity>(response.data)
    },
    async pick(id: string | number) {
      const response = await api.post(`/${resource}/${id}/pick`)
      return asRecord<Entity>(response.data)
    },
    async pack(id: string | number) {
      const response = await api.post(`/${resource}/${id}/pack`)
      return asRecord<Entity>(response.data)
    },
    async remove(id: string | number) { await api.delete(`/${resource}/${id}`) },
  }
}
