import { api, asList } from './api'
import type { Movement } from './types'

export const ledgerService = {
  async list(params?: Record<string, string | number | undefined>) { return asList<Movement>((await api.get('/ledger', { params })).data) },
}
