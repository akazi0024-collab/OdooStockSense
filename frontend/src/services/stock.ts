import { api, asList } from './api'
import type { Entity } from './types'

export const stockService = {
  async list(params?: { locationId?: string | number; productId?: string | number }) { return asList<Entity>((await api.get('/stock', { params })).data) },
  async lowStock() { return asList<Entity>((await api.get('/stock/low')).data) },
}
