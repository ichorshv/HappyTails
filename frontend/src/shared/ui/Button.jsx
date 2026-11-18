export default function Button({ children, type = 'button', className = '', ...props }) {
  return (
    <button
      type={type}
      className={`inline-flex h-16 min-w-44 items-center justify-center rounded-pill bg-mint/70 px-10 text-2xl font-bold text-ink shadow-soft transition hover:bg-mint focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink disabled:cursor-not-allowed disabled:opacity-60 ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}
