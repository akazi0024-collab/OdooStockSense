import axios, { type AxiosError } from 'axios'

const TOKEN_KEY = 'stocksense_access_token'
const USER_KEY = 'stocksense_user'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export function getAccessToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setAccessToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export function getStoredUser<T>() {
  const value = localStorage.getItem(USER_KEY)
  if (!value) return null
  try { return JSON.parse(value) as T } catch { localStorage.removeItem(USER_KEY); return null }
}

export function setStoredUser(user: unknown) {
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
  else localStorage.removeItem(USER_KEY)
}

export function clearSession() {
  setAccessToken(null)
  setStoredUser(null)
}

export function apiMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as Record<string, unknown> | undefined
    const message = data?.message ?? data?.error ?? data?.detail
    if (typeof message === 'string') return message
    if (error.response?.status === 401) return 'Your session has expired. Please sign in again.'
    if (error.response?.status === 403) return 'You do not have permission to complete this action.'
    if (!error.response) return 'Backend connection unavailable. Check the API URL and retry.'
  }
  return error instanceof Error ? error.message : 'Something went wrong. Please try again.'
}

export function isUnauthorized(error: unknown): boolean {
  return (error as AxiosError | undefined)?.response?.status === 401
}

export type PageResult<T> = { items: T[]; total: number }
export type Query = Record<string, string | number | boolean | undefined>

export function asList<T>(payload: unknown): T[] {
  if (Array.isArray(payload)) return payload as T[]
  if (payload && typeof payload === 'object') {
    const record = payload as Record<string, unknown>
    for (const key of ['content', 'items', 'results', 'data']) {
      if (Array.isArray(record[key])) return record[key] as T[]
    }
  }
  return []
}

export function asRecord<T>(payload: unknown): T {
  if (payload && typeof payload === 'object' && 'data' in payload) {
    return (payload as { data: T }).data
  }
  return payload as T
}
