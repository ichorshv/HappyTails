# API – Users, Login and Roles

Errors use the common error format (`status`, `code`, `message`, `details`). Member 1 owns this format; see `docs/api/applications.md` for an example. Error codes of this module start with `AUTH_` or `USER_`.

## How login works

Login uses a **server session** (see ADR-0003). After a successful login the server sets an `HttpOnly` cookie `JSESSIONID`. The browser sends it automatically with every request, so the frontend does not store any token.

Requests that change data (`POST`, `PUT`, `DELETE`) also need a CSRF token. The server sends it in the `XSRF-TOKEN` cookie, and Axios copies it into the `X-XSRF-TOKEN` header automatically.

Roles: `APPLICANT`, `STAFF`, `ADMIN`. Everything under `/api/admin/**` is for `STAFF` and `ADMIN` only.

## 1. Register

`POST /api/auth/register` · role: **everyone**

Creates an applicant account. Staff accounts are created by the admin (section 5).

### Request body

```json
{
  "email": "maria@example.com",
  "password": "Parola123",
  "fullName": "Мария Иванова"
}
```

- `password` – at least 8 characters, with at least one letter and one digit.
- The password is stored only as a BCrypt hash.

### Responses

| Status | When | Body / code |
|---|---|---|
| `201 Created` | The account is created and the user is logged in | `{ "id": 5, "email": "maria@example.com", "fullName": "Мария Иванова", "role": "APPLICANT" }` |
| `400 Bad Request` | A field is missing or invalid | `VALIDATION_ERROR` |
| `409 Conflict` | The email is already registered | `AUTH_EMAIL_TAKEN` |

## 2. Log in

`POST /api/auth/login` · role: **everyone**

### Request body

```json
{
  "email": "maria@example.com",
  "password": "Parola123"
}
```

### Responses

| Status | When | Body / code |
|---|---|---|
| `200 OK` | Correct email and password | Same body as `GET /api/auth/me` |
| `400 Bad Request` | A field is missing | `VALIDATION_ERROR` |
| `401 Unauthorized` | Wrong email or password | `AUTH_BAD_CREDENTIALS` |
| `403 Forbidden` | The account is deactivated | `AUTH_ACCOUNT_DISABLED` |

For security the message does not say whether the email or the password is wrong.

## 3. Log out

`POST /api/auth/logout` · role: **logged in**

Ends the session. Response: `204 No Content`.

## 4. Current user

`GET /api/auth/me` · role: **logged in**

The frontend calls it on page load to know who is logged in.

### Responses

| Status | When | Body / code |
|---|---|---|
| `200 OK` | Logged in | `{ "id": 5, "email": "maria@example.com", "fullName": "Мария Иванова", "role": "APPLICANT" }` |
| `401 Unauthorized` | Not logged in | `UNAUTHORIZED` |

## 5. Staff accounts (admin)

### List users

`GET /api/admin/users?role=STAFF&page=0&size=20` · role: **ADMIN**

`role` is optional.

```json
{
  "content": [
    { "id": 2, "email": "ivan@happytails.bg", "fullName": "Иван Петров", "role": "STAFF", "active": true }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
```

### Create a staff account

`POST /api/admin/users` · role: **ADMIN**

```json
{
  "email": "ivan@happytails.bg",
  "password": "Vremenna123",
  "fullName": "Иван Петров",
  "role": "STAFF"
}
```

`role` is `STAFF` or `ADMIN`.

### Activate or deactivate

`PUT /api/admin/users/{id}/active` · role: **ADMIN**

```json
{ "active": false }
```

A deactivated user is logged out at once and cannot log in.

### Responses

| Status | When | Code |
|---|---|---|
| `200 OK` / `201 Created` | Success | – |
| `400 Bad Request` | Invalid field | `VALIDATION_ERROR` |
| `401` / `403` | Not logged in / not an admin | `UNAUTHORIZED` / `FORBIDDEN` |
| `404 Not Found` | The user does not exist | `USER_NOT_FOUND` |
| `409 Conflict` | The email is already registered | `AUTH_EMAIL_TAKEN` |
| `409 Conflict` | The admin tries to deactivate themselves | `USER_CANNOT_DEACTIVATE_SELF` |

## For other modules

Other modules do not call these endpoints. They use `CurrentUserService` from `users.api` to get the logged-in user. Until login is ready, the stub returns a fixed test user. In tests use `@WithMockUser`.