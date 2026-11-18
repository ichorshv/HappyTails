import axios from 'axios'

// The session cookie and the XSRF-TOKEN cookie come from the same address (Vite proxy / Nginx).
// Axios copies XSRF-TOKEN into the X-XSRF-TOKEN header by default.
const apiClient = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

/** Turns an error from the server into { message, fields } for the forms. */
export function toFormError(error, fallback = 'Нещо се обърка. Опитайте отново.') {
  const data = error?.response?.data
  if (!data?.message) {
    return { message: fallback, fields: {} }
  }
  const fields = {}
  for (const detail of data.details ?? []) {
    const [field, ...rest] = String(detail).split(':')
    if (rest.length > 0 && !fields[field]) {
      fields[field.trim()] = rest.join(':').trim()
    }
  }
  return { message: data.message, fields }
}

export default apiClient
