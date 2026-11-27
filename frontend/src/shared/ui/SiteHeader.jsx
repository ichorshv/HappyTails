import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import logo from '../../assets/home/logo.svg'
import menuIcon from '../../assets/home/menu.png'
import NAV_LINKS from './navLinks'
import SiteNavLink from './SiteNavLink'

/**
 * Glass pills: the logo on the left; on the right the links and login, strung together
 * on a thin glass rail. Below lg the links move into the ☰ menu. A logged-in user sees a
 * pill with their name and ☰ instead of "Вход"; it opens the menu with "Изход".
 */
export default function SiteHeader({ user, onLogout }) {
  const [open, setOpen] = useState(false)

  useEffect(() => {
    if (!open) return
    const onKey = (e) => e.key === 'Escape' && setOpen(false)
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open])

  const pill =
    'flex h-11 cursor-pointer items-center rounded-pill border border-white/70 px-6 text-base font-bold whitespace-nowrap text-ink shadow-soft backdrop-blur-md transition'
  const linkPill = `${pill} bg-white/60 hover:bg-white/90`
  const loginPill = `${pill} bg-mint/80 px-7 hover:bg-mint`

  return (
    // transform-gpu: own layer, so Safari keeps painting the header above the blurred cards
    <header className="sticky top-4 z-50 mx-4 transform-gpu font-sans md:mx-6 lg:mx-8">
      <div className="relative flex items-center justify-between gap-4">
        <Link
          to="/"
          className="flex h-11 items-center gap-2 rounded-pill border border-white/70 bg-white/60 px-5 text-lg font-bold whitespace-nowrap text-deep shadow-soft backdrop-blur-md"
        >
          <img src={logo} alt="" width="28" height="33" className="h-6 w-auto" />
          Happy Tails
        </Link>

        <div className="relative flex items-center gap-4">
          {/* The rail the pills sit on */}
          <div aria-hidden="true" className="absolute inset-x-4 top-1/2 h-3 -translate-y-1/2 rounded-full bg-white/50 shadow-soft backdrop-blur-sm" />
          {NAV_LINKS.map((link) => (
            <SiteNavLink key={link.to} {...link} className={`relative hidden lg:flex ${linkPill}`} />
          ))}
          {user ? (
            <button
              type="button"
              aria-label={open ? 'Затвори менюто' : `Меню на ${user.fullName}`}
              aria-expanded={open}
              aria-controls="site-menu"
              onClick={() => setOpen((v) => !v)}
              className={`relative gap-3 ${linkPill}`}
            >
              <span className="max-w-32 truncate sm:max-w-48">{user.fullName}</span>
              <img src={menuIcon} alt="" className="size-5" />
            </button>
          ) : (
            <>
              <Link to="/login" className={`relative ${loginPill}`}>
                Вход
              </Link>
              <button
                type="button"
                aria-label={open ? 'Затвори менюто' : 'Меню'}
                aria-expanded={open}
                aria-controls="site-menu"
                onClick={() => setOpen((v) => !v)}
                className={`relative !px-0 size-11 justify-center lg:hidden ${linkPill}`}
              >
                <img src={menuIcon} alt="" className="size-5" />
              </button>
            </>
          )}
        </div>

        {open && (
          <>
            {/* Click outside closes the menu */}
            <button type="button" aria-hidden="true" tabIndex={-1} onClick={() => setOpen(false)} className="fixed inset-0 -z-10 cursor-default" />
            <nav
              id="site-menu"
              className={`absolute top-full right-0 mt-3 flex w-64 flex-col rounded-3xl border border-white/70 bg-white/80 p-2 shadow-soft backdrop-blur-xl ${user ? '' : 'lg:hidden'}`}
            >
              {/* The links are in the header from lg up, so the menu repeats them only below lg */}
              {NAV_LINKS.map((link) => (
                <SiteNavLink
                  key={link.to}
                  {...link}
                  onClick={() => setOpen(false)}
                  className={`rounded-2xl px-4 py-3 text-base text-deep transition hover:bg-mint/60 ${user ? 'lg:hidden' : ''}`}
                />
              ))}
              {user && (
                <>
                  <div aria-hidden="true" className="mx-4 my-1 h-px bg-deep/15 lg:hidden" />
                  <button
                    type="button"
                    onClick={() => {
                      setOpen(false)
                      onLogout()
                    }}
                    className="cursor-pointer rounded-2xl px-4 py-3 text-left text-base font-bold text-deep transition hover:bg-mint/60"
                  >
                    Изход
                  </button>
                </>
              )}
            </nav>
          </>
        )}
      </div>
    </header>
  )
}
