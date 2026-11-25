import { Link } from 'react-router-dom'

/** One site link. Links to home page sections are plain anchors, so they also work from other pages. */
export default function SiteNavLink({ label, to, onClick, className }) {
  return to.startsWith('/#') ? (
    <a href={to} onClick={onClick} className={className}>
      {label}
    </a>
  ) : (
    <Link to={to} onClick={onClick} className={className}>
      {label}
    </Link>
  )
}
