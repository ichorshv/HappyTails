import { Link } from 'react-router-dom'
import greenGlow from '../../assets/auth/bg-ellipse-green.svg'
import redGlow from '../../assets/auth/bg-ellipse-red.svg'
import logo from '../../assets/auth/logo.svg'

/** Background, back arrow to the home page, glass card, logo and title shared by the login and register screens. */
export default function AuthLayout({ children }) {
  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-page px-4 py-8">
      <img
        src={greenGlow}
        alt=""
        aria-hidden="true"
        className="pointer-events-none absolute -left-[410px] -top-[395px] h-[1156px] w-[1241px] max-w-none"
      />
      <img
        src={redGlow}
        alt=""
        aria-hidden="true"
        className="pointer-events-none absolute -bottom-[410px] -right-[330px] h-[1148px] w-[1284px] max-w-none"
      />
      <Link
        to="/"
        aria-label="Към началната страница"
        className="absolute top-4 left-4 z-10 flex size-12 items-center justify-center rounded-full bg-white/70 text-ink shadow-soft backdrop-blur-sm transition hover:bg-white sm:top-6 sm:left-6"
      >
        <svg aria-hidden="true" viewBox="0 0 24 24" className="size-6" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
          <path d="M19 12H5M11 18l-6-6 6-6" />
        </svg>
      </Link>
      <section className="relative w-full max-w-[801px] rounded-pill bg-glass/30 px-6 pb-10 pt-8 shadow-soft backdrop-blur-sm sm:px-[76px] sm:pt-14">
        <div className="mb-8 flex flex-col items-center gap-2 sm:flex-row sm:justify-center sm:gap-6">
          <img src={logo} alt="" width="143.494" height="143.494" className="size-24 sm:size-36" />
          <h1 className="text-4xl font-bold text-ink sm:text-5xl">Happy Tails</h1>
        </div>
        {children}
      </section>
    </main>
  )
}
