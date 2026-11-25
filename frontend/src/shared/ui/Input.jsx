import { useId } from 'react'

export default function Input({ label, error, className = '', ...props }) {
  const id = useId()
  const errorId = `${id}-error`
  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1 block px-5 text-sm font-bold text-ink">
        {label}
      </label>
      <input
        id={id}
        aria-invalid={error ? true : undefined}
        aria-describedby={error ? errorId : undefined}
        className={`h-10 w-full rounded-pill bg-white/50 px-5 text-base text-ink shadow-soft outline-none focus-visible:ring-2 focus-visible:ring-ink ${error ? 'ring-2 ring-danger' : ''}`}
        {...props}
      />
      {error && (
        <p id={errorId} className="mt-1 px-5 text-sm font-bold text-danger">
          {error}
        </p>
      )}
    </div>
  )
}
