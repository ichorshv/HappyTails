import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { toFormError } from '../../shared/apiClient'
import Button from '../../shared/ui/Button'
import Input from '../../shared/ui/Input'
import AuthLayout from './AuthLayout'
import { useAuth } from './AuthContext'
import FormError from './FormError'
import { onlyErrors, validateEmail, validateFullName, validatePassword } from './validation'

export default function RegisterPage() {
  const { user, register } = useAuth()
  const navigate = useNavigate()

  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  if (user) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const found = onlyErrors({
      fullName: validateFullName(fullName),
      email: validateEmail(email),
      password: validatePassword(password),
    })
    setErrors(found)
    setFormError(null)
    if (Object.keys(found).length > 0) return

    setSubmitting(true)
    try {
      await register(fullName.trim(), email.trim(), password)
      navigate('/', { replace: true })
    } catch (error) {
      const { message, fields } = toFormError(error)
      if (error?.response?.data?.code === 'AUTH_EMAIL_TAKEN') {
        setErrors({ email: message })
      } else {
        setFormError(message)
        setErrors(fields)
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout>
      <form noValidate onSubmit={handleSubmit} className="flex flex-col gap-8">
        <FormError message={formError} />
        <Input
          label="Име и Фамилия:"
          autoComplete="name"
          value={fullName}
          onChange={(event) => setFullName(event.target.value)}
          error={errors.fullName}
        />
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
          autoComplete="new-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          error={errors.password}
        />
        <div className="flex flex-col items-center gap-4">
          <Button type="submit" disabled={submitting}>
            Регистрация
          </Button>
          <Link to="/login" className="text-sm font-bold text-ink underline">
            Имаш акаунт? Влез
          </Link>
        </div>
      </form>
    </AuthLayout>
  )
}
