export default function Button({ children, type = 'button', className = '', ...props }) {
  return (
    <button
      type={type}
      className={`inline-flex h-11 min-w-40 items-center justify-center rounded-pill bg-mint/70 px-8 text-lg font-bold text-ink shadow-soft transition hover:bg-mint focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink disabled:cursor-not-allowed disabled:opacity-60 ${className}`}
      {...props}
    >
      {children}
    </button>
  )
}
