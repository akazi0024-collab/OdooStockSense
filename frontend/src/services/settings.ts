import { api, asRecord } from './api'

export type WorkspaceSettings = { lowStockAlerts?: boolean; emailNotifications?: boolean; timezone?: string; currency?: string; [key: string]: unknown }
export const settingsService = {
  async get() { return asRecord<WorkspaceSettings>((await api.get('/settings')).data) },
  async update(payload: Partial<WorkspaceSettings>) { return asRecord<WorkspaceSettings>((await api.put('/settings', payload)).data) },
}
