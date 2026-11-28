import { createBrowserRouter } from 'react-router-dom'
import authRoutes from '../features/auth/routes'
import walkRoutes from '../features/walks/routes'
import HomePage from './HomePage'
import SiteLayout from './SiteLayout'

// Each feature exports its own routes; this file only collects them.
// Public pages go under <SiteLayout /> (navigation, layout grid container, footer).
// Pages for logged-in users go under <ProtectedRoute /> (features/auth/ProtectedRoute.jsx),
// e.g. { element: <ProtectedRoute roles={['STAFF', 'ADMIN']} />, children: adminRoutes }.
const router = createBrowserRouter([
  {
    // Public pages share the navigation and the footer
    element: <SiteLayout />,
    children: [
      { path: '/', element: <HomePage /> },
      // Temporary empty catalog page; replaced by the real catalog from #24
      { path: '/animals', element: <div className="min-h-[60vh]" /> },
    ],
  },
  ...authRoutes,
  ...walkRoutes,
])

export default router