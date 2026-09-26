import { api, asList, asRecord } from './api'
import type { Entity } from './types'

export const productService = {
  async list() { return asList<Entity>((await api.get('/products')).data) },
  async get(id: string | number) { return asRecord<Entity>((await api.get(`/products/${id}`)).data) },
  async create(payload: Record<string, unknown>) { return asRecord<Entity>((await api.post('/products', payload)).data) },
  async update(id: string | number, payload: Record<string, unknown>) { return asRecord<Entity>((await api.put(`/products/${id}`, payload)).data) },
  async remove(id: string | number) { await api.delete(`/products/${id}`) },
}
