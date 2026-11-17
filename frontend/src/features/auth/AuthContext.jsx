import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import apiClient from '../../shared/apiClient'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // On page load: who is logged in? The call also sets the XSRF-TOKEN cookie.
  useEffect(() => {
    apiClient
      .get('/auth/me')
      .then((response) => setUser(response.data))
      .catch(() => setUser(null))
      .finally(() => setLoading(false))
  }, [])

  const login = useCallback(async (email, password) => {
    const response = await apiClient.post('/auth/login', { email, password })
    setUser(response.data)
    return response.data
  }, [])

  const register = useCallback(async (fullName, email, password) => {
    const response = await apiClient.post('/auth/register', { fullName, email, password })
    setUser(response.data)
    return response.data
  }, [])

  const logout = useCallback(async () => {
    try {
      await apiClient.post('/auth/logout')
    } finally {
      setUser(null)
    }
  }, [])

  const value = useMemo(
    () => ({ user, loading, login, register, logout }),
    [user, loading, login, register, logout],
  )
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside <AuthProvider>')
  }
  return context
}
