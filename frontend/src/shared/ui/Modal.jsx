import { useEffect, useId, useRef } from 'react'

/**
 * Window over the page (Figma "Modal"): white card in the middle, the page behind is dimmed.
 * Built on <dialog>, so focus stays inside while it is open and Escape closes it; a click on
 * the dimmed area or on × closes it too.
 *
 * Usage: <Modal open={open} onClose={() => setOpen(false)} title="Нов служител">…</Modal>
 */
export default function Modal({ open, onClose, title, children, className = '' }) {
  const dialogRef = useRef(null)
  const titleId = useId()

  useEffect(() => {
    const dialog = dialogRef.current
    if (open && !dialog.open) dialog.showModal()
    if (!open && dialog.open) dialog.close()
  }, [open])

  // The page behind does not scroll while the window is open
  useEffect(() => {
    if (!open) return
    const previous = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = previous
    }
  }, [open])

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby={title ? titleId : undefined}
      onClose={onClose}
      // The dialog itself is only hit outside the card, on the dimmed area
      onClick={(event) => event.target === event.currentTarget && onClose()}
      className={`m-auto max-h-[calc(100svh-2rem)] w-[calc(100%-2rem)] max-w-[720px] overflow-y-auto rounded-3xl bg-white p-0 text-deep shadow-soft backdrop:bg-black/45 ${className}`}
    >
      <div className="relative p-6 sm:p-10">
        <button
          type="button"
          aria-label="Затвори"
          onClick={onClose}
          className="absolute top-4 right-4 flex size-10 cursor-pointer items-center justify-center rounded-full text-2xl leading-none transition hover:bg-mint"
        >
          ×
        </button>
        {title && (
          <h2 id={titleId} className="mb-6 pr-10 text-2xl font-bold">
            {title}
          </h2>
        )}
        {children}
      </div>
    </dialog>
  )
}
