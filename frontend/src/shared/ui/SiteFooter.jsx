import logo from '../../assets/home/logo.svg'
import Container from './Container'
import NAV_LINKS from './navLinks'
import SiteNavLink from './SiteNavLink'

// Example data until the shelter gives its real contacts
const CONTACTS = {
  address: 'ул. „Примерна“ 1, София',
  phone: '+359 2 000 0000',
  email: 'info@example.com',
  hours: ['Пн–Пт: 10:00–18:00', 'Сб: 10:00–14:00'],
}

const HEADING = 'mb-3 text-lg font-bold'

/**
 * Site footer: logo, links and the shelter contacts on a peach-to-coral panel.
 * While the footer scrolls into view two glows move in from the sides, meet in the middle and
 * dissolve into the panel (footer-* classes in index.css). Without that animation the glows
 * stay hidden and only the panel is shown.
 */
export default function SiteFooter() {
  return (
    <footer className="site-footer relative pt-32 text-deep sm:pt-40">
      <div aria-hidden="true" className="footer-orb-left absolute bottom-0 left-1/2 h-full aspect-square -translate-x-[85%] rounded-full bg-peach opacity-0 blur-3xl" />
      <div aria-hidden="true" className="footer-orb-right absolute right-1/2 bottom-0 h-full aspect-square translate-x-[85%] rounded-full bg-coral opacity-0 blur-3xl" />
      {/* Solid at the bottom, fading out softly towards the top (no blur, so the bottom edge stays full) */}
      <div
        aria-hidden="true"
        className="footer-panel absolute inset-0 bg-linear-to-r from-peach to-coral [mask-image:linear-gradient(to_top,#000_55%,rgb(0_0_0/0.7)_72%,rgb(0_0_0/0.25)_88%,transparent)]"
      />

      <Container className="relative">
        <div className="grid gap-8 pb-8 md:grid-cols-3 md:gap-6">
          <div>
            <div className="mb-3 flex items-center gap-2 text-2xl font-bold">
              <img src={logo} alt="" width="28" height="33" />
              Happy Tails
            </div>
            <p className="max-w-xs leading-relaxed">
              Приют за кучета и котки. Намери своя нов приятел и му дай дом.
            </p>
          </div>

          <nav aria-label="Страници">
            <h2 className={HEADING}>Сайт</h2>
            <ul className="flex flex-col gap-2">
              {NAV_LINKS.map((link) => (
                <li key={link.to}>
                  <SiteNavLink {...link} className="hover:underline" />
                </li>
              ))}
            </ul>
          </nav>

          <div>
            <h2 className={HEADING}>Контакти</h2>
            <address className="flex flex-col gap-2 not-italic">
              <span>{CONTACTS.address}</span>
              <a href={`tel:${CONTACTS.phone.replaceAll(' ', '')}`} className="hover:underline">
                {CONTACTS.phone}
              </a>
              <a href={`mailto:${CONTACTS.email}`} className="hover:underline">
                {CONTACTS.email}
              </a>
              {CONTACTS.hours.map((line) => (
                <span key={line}>{line}</span>
              ))}
            </address>
          </div>
        </div>

        <p className="border-t border-deep/20 py-5 text-center text-sm">© 2026 Happy Tails</p>
      </Container>
    </footer>
  )
}
