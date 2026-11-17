import { Link } from 'react-router-dom'

export default function ForbiddenPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-page p-4 text-center text-ink">
      <h1 className="text-3xl font-bold">Нямате достъп</h1>
      <p>Тази страница е само за служители на приюта.</p>
      <Link to="/" className="font-bold underline">
        Към началото
      </Link>
    </main>
  )
}
