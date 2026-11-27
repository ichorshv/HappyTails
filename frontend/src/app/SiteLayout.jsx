import { Outlet } from 'react-router-dom'
import glowTop from '../assets/home/glow-top.svg'
import glowBottomLeft from '../assets/home/glow-bottom-left.svg'
import glowBottomRight from '../assets/home/glow-bottom-right.svg'
import { useAuth } from '../features/auth/AuthContext'
import Container from '../shared/ui/Container'
import SiteFooter from '../shared/ui/SiteFooter'
import SiteHeader from '../shared/ui/SiteHeader'

/**
 * Frame of the public site: color glows in the background, navigation on top, the page in
 * the layout grid container and the footer at the bottom. Login and the admin panel have
 * their own layouts.
 */
export default function SiteLayout() {
  const { user, logout } = useAuth()

  return (
    // overflow-clip (not hidden) so the sticky header and cards still stick
    <div className="relative min-h-screen overflow-clip bg-paper pt-4 font-display text-deep">
      {/* Blurred color glows stay fixed to the screen; the glass surfaces pick up their color */}
      <div aria-hidden="true" className="pointer-events-none fixed inset-0">
        <img src={glowTop} alt="" width="1473" height="913" className="absolute -top-[661px] left-1/2 max-w-none -translate-x-1/2" />
        <img src={glowBottomRight} alt="" width="1000" height="951" className="absolute -right-[620px] -bottom-[560px] max-w-none" />
        <img src={glowBottomLeft} alt="" width="1000" height="951" className="absolute -bottom-[580px] -left-[640px] max-w-none" />
      </div>

      <SiteHeader user={user} onLogout={logout} />

      <Container as="main" className="relative pt-8">
        <Outlet />
      </Container>

      <SiteFooter />
    </div>
  )
}
