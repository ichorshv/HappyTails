// The same rules as the server (docs/api/auth.md), so the user sees the error before sending.

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function validateEmail(email) {
  if (!email.trim()) return 'Въведете имейл.'
  if (!EMAIL.test(email.trim())) return 'Невалиден имейл.'
  return null
}

export function validatePassword(password) {
  if (!password) return 'Въведете парола.'
  if (password.length < 8 || password.length > 72) return 'Паролата трябва да е от 8 до 72 знака.'
  if (!/\p{L}/u.test(password) || !/\d/.test(password)) {
    return 'Паролата трябва да има поне една буква и една цифра.'
  }
  return null
}

export function validateFullName(fullName) {
  const length = fullName.trim().length
  if (length === 0) return 'Въведете име и фамилия.'
  if (length < 2 || length > 100) return 'Името трябва да е от 2 до 100 знака.'
  return null
}

export function validatePlace(value, emptyMessage) {
  const length = value.trim().length
  if (length === 0) return emptyMessage
  if (length > 100) return 'Най-много 100 знака.'
  return null
}

/** Keeps only the fields that have an error. */
export function onlyErrors(errors) {
  return Object.fromEntries(Object.entries(errors).filter(([, value]) => value))
}
