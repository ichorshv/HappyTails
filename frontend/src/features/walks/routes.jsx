import ProtectedRoute from '../auth/ProtectedRoute'
import WalkCalendarPage from './WalkCalendarPage'

const walkRoutes = [
    {
        element: <ProtectedRoute roles={['APPLICANT']} />,
        children: [{ path: '/walks/book', element: <WalkCalendarPage /> }],
    },
]

export default walkRoutes