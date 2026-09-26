import { api, clearSession, setAccessToken, setStoredUser } from './api'
import type { User } from './types'

type AuthResult = { token: string; id: number; name: string; email: string; roles: string[] }
type ForgotResult = { message: string; devOtp?: string | null }

function toUser(result: AuthResult): User {
  return { id: result.id, name: result.name, email: result.email, role: result.roles?.[0] }
}

function saveSession(result: AuthResult) {
  setAccessToken(result.token)
  const user = toUser(result)
  setStoredUser(user)
  return user
}

export const authService = {
  async login(credentials: { email: string; password: string }) {
    const { data } = await api.post<AuthResult>('/auth/login', credentials)
    return { token: data.token, user: saveSession(data) }
  },
  async register(payload: { name: string; email: string; password: string }) {
    const { data } = await api.post<AuthResult>('/auth/register', payload)
    return { token: data.token, user: saveSession(data) }
  },
  logout() {
    clearSession()
  },
  async forgotPassword(email: string) {
    const { data } = await api.post<ForgotResult>('/auth/forgot-password', { email })
    return data
  },
  verifyOtp(payload: { email: string; otp: string }) { return api.post('/auth/verify-otp', payload) },
  resetPassword(payload: { email: string; otp: string; newPassword: string }) { return api.post('/auth/reset-password', payload) },
}
