# ADR-0003: Server session instead of JWT for login

## Problem

The server must remember which user is logged in and with what role (applicant, staff, admin). The architecture leaves open whether to use a session or JWT (section 13, question 1).

## Assumptions

- Nginx serves the React app and `/api` from the same address.
- There is one backend server and no mobile app.
- The admin must be able to deactivate a staff account, and that user must lose access at once.

## Motivation

We want the simplest secure solution that Spring Security supports out of the box, so that more time is left for the business rules.

## Alternatives

1. **Session.** The server keeps who is logged in; the browser gets only an `HttpOnly` cookie with the session ID.
2. **JWT.** The server gives a signed token with the user and role; the frontend sends it with every request.

## Decision

We use a **server session** from Spring Security:
- after login the server sets an `HttpOnly` cookie `JSESSIONID`;
- passwords are stored as BCrypt hashes;
- CSRF protection is on: the token is in the `XSRF-TOKEN` cookie, and Axios sends it in the `X-XSRF-TOKEN` header;
- the session expires after 30 minutes without activity.

## Rationale

- JWT is useful for mobile apps, many servers or external clients; we have none of these.
- A session needs less code. JWT needs creating, checking and refreshing tokens.
- Logout and deactivation take effect at once; a JWT stays valid until it expires.
- JavaScript cannot read an `HttpOnly` cookie; a JWT in `localStorage` can be stolen.
- Downside: sessions are kept in the server memory, so a restart logs everyone out. This is acceptable for a course project with one server.