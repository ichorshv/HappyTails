import { Link } from 'react-router-dom'
import { useAuth } from '../features/auth/AuthContext'

/** Temporary start page until the catalog (#24) is ready. */
export default function HomePage() {
  const { user, logout } = useAuth()
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-page p-4 text-ink">
      <h1 className="text-3xl font-bold">Happy Tails</h1>
      {user ? (
        <>
          <p>Здравей, {user.fullName}!</p>
          <button type="button" onClick={logout} className="font-bold underline">
            Изход
          </button>
        </>
      ) : (
        <Link to="/login" className="font-bold underline">
          Вход
        </Link>
      )}
    </main>
  )
}
