import { createBrowserRouter } from 'react-router-dom'
import authRoutes from '../features/auth/routes'
import HomePage from './HomePage'

// Each feature exports its own routes; this file only collects them.
// Pages for logged-in users go under <ProtectedRoute /> (features/auth/ProtectedRoute.jsx),
// e.g. { element: <ProtectedRoute roles={['STAFF', 'ADMIN']} />, children: adminRoutes }.
const router = createBrowserRouter([{ path: '/', element: <HomePage /> }, ...authRoutes])

export default router
