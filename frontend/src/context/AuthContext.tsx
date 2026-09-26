import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authService } from '../services/auth'
import { getAccessToken, getStoredUser, setStoredUser } from '../services/api'
import type { User } from '../services/types'

type AuthContextValue = {
  user: User | null
  ready: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => getStoredUser<User>())
  const [ready, setReady] = useState(false)

  useEffect(() => {
    if (!getAccessToken()) setStoredUser(null)
    else if (!getStoredUser<User>()) authService.logout()
    setReady(true)
  }, [])

  const value = useMemo<AuthContextValue>(() => ({
    user, ready,
    async login(email, password) {
      const result = await authService.login({ email, password })
      setUser(result.user)
    },
    async register(name, email, password) {
      const result = await authService.register({ name, email, password })
      setUser(result.user)
    },
    async logout() {
      authService.logout()
      setUser(null)
    },
  }), [user, ready])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
