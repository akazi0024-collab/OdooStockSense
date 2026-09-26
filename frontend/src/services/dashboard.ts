import { api, asList, asRecord } from './api'
import type { DashboardStats, Entity, Movement } from './types'

export const dashboardService = {
  async stats() { return asRecord<DashboardStats>((await api.get('/dashboard/stats')).data) },
  async movements() { return asList<Movement>((await api.get('/dashboard/movements')).data) },
  async categories() { return asList<{ name?: string; categoryName?: string; quantity?: number }>((await api.get('/dashboard/category-stock')).data) },
  async lowStock() { return asList<Entity>((await api.get('/dashboard/low-stock')).data) },
  async recentOperations() { return asList<Movement>((await api.get('/dashboard/movements')).data) },
  async pendingOperations() {
    const kinds = ['receipts', 'deliveries', 'transfers', 'adjustments'] as const
    const groups = await Promise.all(kinds.map(async (kind) => ({ kind, items: asList<Entity>((await api.get(`/${kind}`)).data) })))
    return groups.flatMap(({ kind, items }) => items.filter((item) => !['VALIDATED', 'CANCELLED'].includes(String(item.status).toUpperCase())).map((item) => ({
      ...item,
      type: kind,
      productName: item.productName ?? item.items?.map((line) => line.productName).filter(Boolean).join(', '),
      quantity: item.quantity ?? item.quantityDelta ?? item.items?.reduce((sum, line) => sum + Number(line.quantity ?? 0), 0),
    }))) as Movement[]
  },
}
