# API – Walks and Schedule (Member 5)

Errors use the common format described in `docs/api/applications.md` (`status`, `code`, `message`, `details`). Error codes of this module start with `WALK_`.

All times in requests and responses are in UTC (ISO 8601, e.g. `2026-11-12T10:00:00Z`). The working hours in the settings are local shelter time (Europe/Sofia).

## 1. Free slots

`GET /api/walks/slots?animalId=12` · role: **APPLICANT**

Returns the free walk slots for the animal for the next 14 days. A slot is free when all of these are true:

- it is inside the working hours and in the future;
- the animal has no other walk at that time;
- the number of walks at that time is less than the number of staff.

### Response `200 OK`

```json
{
  "animalId": 12,
  "slotMinutes": 60,
  "slots": [
    { "startsAt": "2026-11-12T07:00:00Z", "endsAt": "2026-11-12T08:00:00Z" },
    { "startsAt": "2026-11-12T08:00:00Z", "endsAt": "2026-11-12T09:00:00Z" }
  ]
}
```

| Status | When | Code |
|---|---|---|
| `200 OK` | Slots returned (the list can be empty) | – |
| `400 Bad Request` | `animalId` is missing | `VALIDATION_ERROR` |
| `401` / `403` | Not logged in / not an applicant | `UNAUTHORIZED` / `FORBIDDEN` |
| `404 Not Found` | The animal does not exist | `WALK_ANIMAL_NOT_FOUND` |

## 2. Book a walk

`POST /api/walks` · role: **APPLICANT**

Only the applicant whose application is first in the queue (status `YOUR_TURN`) can book a walk. The reservation is atomic: the database has a unique constraint on the animal and the start time, so when two applicants try to take the same slot at the same moment only one of them succeeds.

### Request body

```json
{
  "applicationId": 15,
  "startsAt": "2026-11-12T08:00:00Z"
}
```

### Responses

| Status | When | Body / code |
|---|---|---|
| `201 Created` | The walk is booked | `{ "id": 40, "applicationId": 15, "animal": { "id": 12, "name": "Рекс" }, "startsAt": "2026-11-12T08:00:00Z", "endsAt": "2026-11-12T09:00:00Z", "status": "BOOKED" }` |
| `400 Bad Request` | A field is missing | `VALIDATION_ERROR` |
| `401 Unauthorized` | Not logged in | `UNAUTHORIZED` |
| `403 Forbidden` | Not an applicant, or the application belongs to someone else | `FORBIDDEN` |
| `404 Not Found` | The application does not exist | `WALK_APPLICATION_NOT_FOUND` |
| `409 Conflict` | The applicant is not first in the queue | `WALK_NOT_YOUR_TURN` |
| `409 Conflict` | The application already has a booked walk | `WALK_ALREADY_BOOKED` |
| `409 Conflict` | The slot was taken by someone else | `WALK_SLOT_TAKEN` |
| `409 Conflict` | The applicant has another walk at the same time | `WALK_APPLICANT_BUSY` |
| `422 Unprocessable Entity` | The time is not a free slot (outside working hours, in the past, more than 14 days ahead, or not at the start of a slot) | `WALK_SLOT_NOT_AVAILABLE` |

After a successful booking:

- the queue module (Member 4) moves the application to `WALK_SCHEDULED`;
- the `WalkBooked` event is published and the applicant receives a confirmation email.

## 3. My walks

`GET /api/walks/my` · role: **APPLICANT**

### Response `200 OK`

```json
[
  {
    "id": 40,
    "applicationId": 15,
    "animal": { "id": 12, "name": "Рекс" },
    "startsAt": "2026-11-12T08:00:00Z",
    "endsAt": "2026-11-12T09:00:00Z",
    "status": "BOOKED"
  }
]
```

The list contains only the walks of the logged in applicant, ordered by start time. It is empty when there are no walks.

| Status | When | Code |
|---|---|---|
| `200 OK` | List returned | – |
| `401` / `403` | Not logged in / not an applicant | `UNAUTHORIZED` / `FORBIDDEN` |

## 4. Cancel a walk

`DELETE /api/walks/{id}` · role: **APPLICANT**

The slot is freed immediately and the `WalkCancelled` event is published. The applicant receives an email about the cancellation.

| Status | When | Code |
|---|---|---|
| `204 No Content` | The walk is cancelled | – |
| `401` / `403` | Not logged in / not the owner of the walk | `UNAUTHORIZED` / `FORBIDDEN` |
| `404 Not Found` | The walk does not exist | `WALK_NOT_FOUND` |
| `409 Conflict` | The walk has already taken place or is already cancelled | `WALK_NOT_CANCELLABLE` |

## 5. Daily schedule (staff)

`GET /api/admin/walks?date=2026-11-12` · role: **STAFF**, **ADMIN**

### Response `200 OK`

```json
[
  {
    "id": 40,
    "startsAt": "2026-11-12T08:00:00Z",
    "endsAt": "2026-11-12T09:00:00Z",
    "status": "BOOKED",
    "animal": { "id": 12, "name": "Рекс" },
    "applicant": { "id": 7, "fullName": "Мария Иванова" },
    "applicationId": 15
  }
]
```

| Status | When | Code |
|---|---|---|
| `200 OK` | Schedule returned | – |
| `400 Bad Request` | The date is missing or invalid | `VALIDATION_ERROR` |
| `401` / `403` | Not logged in / not staff | `UNAUTHORIZED` / `FORBIDDEN` |

## 6. Walk settings

`GET /api/admin/walk-settings` and `PUT /api/admin/walk-settings` · role: **ADMIN**

```json
{
  "workStart": "09:00",
  "workEnd": "17:00",
  "slotMinutes": 60,
  "staffCount": 2
}
```

- `workStart` / `workEnd` – working hours for walks (local time);
- `slotMinutes` – the length of one walk;
- `staffCount` – how many walks can take place at the same time (for different animals).

Changing the settings does not affect walks that are already booked.

| Status | When | Code |
|---|---|---|
| `200 OK` | Read / saved | – |
| `400 Bad Request` | Invalid values (`workStart` is not before `workEnd`, `slotMinutes` or `staffCount` is not positive) | `VALIDATION_ERROR` |
| `401` / `403` | Not logged in / not admin | `UNAUTHORIZED` / `FORBIDDEN` |

## To be agreed

**With Member 4 – queue.** The queue module must expose `QueueService` with: who is first in the queue, and the transition of an application to `WALK_SCHEDULED`. We also need to decide what happens to the application when the applicant cancels the walk (proposal: it goes back to `YOUR_TURN` with the remaining time of the 48 hours).

**With Member 2 – animals.** The walk module uses `AnimalQueryService` to check that the animal exists and is available.

**With Member 1 – roles.** `CurrentUserService` gives the id and the role of the logged in user. All `/api/admin/**` addresses are protected by the shared rule.