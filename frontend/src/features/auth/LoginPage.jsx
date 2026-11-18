import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { toFormError } from '../../shared/apiClient'
import Button from '../../shared/ui/Button'
import Input from '../../shared/ui/Input'
import AuthLayout from './AuthLayout'
import { useAuth } from './AuthContext'
import FormError from './FormError'
import { onlyErrors } from './validation'

export default function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = location.state?.from?.pathname ?? '/'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  if (user) {
    return <Navigate to={from} replace />
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const found = onlyErrors({
      email: email.trim() ? null : 'Въведете имейл.',
      password: password ? null : 'Въведете парола.',
    })
    setErrors(found)
    setFormError(null)
    if (Object.keys(found).length > 0) return

    setSubmitting(true)
    try {
      await login(email.trim(), password)
      navigate(from, { replace: true })
    } catch (error) {
      const { message, fields } = toFormError(error)
      setFormError(message)
      setErrors(fields)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout>
      <form noValidate onSubmit={handleSubmit} className="flex flex-col gap-8">
        <FormError message={formError} />
        <Input
          label="Имейл:"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          error={errors.email}
        />
        <Input
          label="Парола:"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          error={errors.password}
        />
        <div className="flex flex-col items-center gap-4">
          <Button type="submit" disabled={submitting}>
            Вход
          </Button>
          <Link to="/register" className="text-sm font-bold text-ink underline">
            Нямаш акаунт? Регистрирай се
          </Link>
        </div>
      </form>
    </AuthLayout>
  )
}
