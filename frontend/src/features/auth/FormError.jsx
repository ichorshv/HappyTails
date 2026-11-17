export default function FormError({ message }) {
  if (!message) return null
  return (
    <p role="alert" className="mb-4 rounded-pill bg-white/60 px-5 py-2 text-center font-bold text-danger">
      {message}
    </p>
  )
}
