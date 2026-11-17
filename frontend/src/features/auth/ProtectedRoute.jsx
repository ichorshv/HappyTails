import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'
import ForbiddenPage from './ForbiddenPage'

/**
 * Shows the child routes only to logged-in users. With `roles` only these roles get in;
 * the others see "Нямате достъп".
 */
export default function ProtectedRoute({ roles }) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return null
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  if (roles && !roles.includes(user.role)) {
    return <ForbiddenPage />
  }
  return <Outlet />
}
