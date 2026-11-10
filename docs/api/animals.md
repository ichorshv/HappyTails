# API – Animals and Catalog (Member 2)

Errors use the common format described in `docs/api/applications.md` (`status`, `code`, `message`, `details`). Error codes of this module start with `ANIMAL_`.

## Values

| Field | Values |
|---|---|
| `species` | `DOG`, `CAT` |
| `size` | `SMALL`, `MEDIUM`, `LARGE` |
| `status` | `DRAFT`, `AVAILABLE`, `RESERVED`, `ADOPTED` |
| `requirements` | `YARD`, `NO_CATS`, `NO_DOGS`, `NO_SMALL_CHILDREN`, `EXPERIENCE` |
| `age` (catalog filter only) | `PUPPY` (0–11 months), `YOUNG` (12–35 months), `ADULT` (36+ months) |

The frontend translates these codes into Bulgarian labels.

A new animal is always `DRAFT`. It becomes `AVAILABLE` when staff publish it. `RESERVED` and `ADOPTED` are set only by the queue module (Member 4) through `AnimalStatusService` – there is no REST endpoint for them.

## 1. Catalog

`GET /api/animals?species=DOG&age=PUPPY&size=SMALL&page=0` · role: **everyone** (no login)

Returns only animals with status `AVAILABLE`, newest first. All filters are optional and can be combined. Pages start at `0` and always have 12 animals (the page size is fixed, because `size` is the animal size filter).

The filters and the `page` parameter are added in #23. Until then the endpoint returns the first page of all available animals.

### Response `200 OK`

```json
{
  "content": [
    {
      "id": 6,
      "name": "Базил",
      "species": "DOG",
      "ageMonths": 4,
      "size": "SMALL",
      "vaccinated": true,
      "neutered": false,
      "photoUrl": null
    }
  ],
  "page": 0,
  "totalElements": 7,
  "totalPages": 1
}
```

- `photoUrl` – the first photo of the animal, or `null` if it has none (photos are added in #25).

| Status | When | Code |
|---|---|---|
| `200 OK` | Animals returned (the list can be empty) | – |
| `400 Bad Request` | A filter has an unknown value or `page` is negative | `VALIDATION_ERROR` |

## 2. Animal details

`GET /api/animals/{id}` · role: **everyone** (no login)

Works for animals with status `AVAILABLE`, `RESERVED` and `ADOPTED`, so the link in the Facebook post keeps working after the animal finds a home. A `DRAFT` animal is not public and returns `404`.

### Response `200 OK`

```json
{
  "id": 5,
  "name": "Джак Спароу",
  "species": "DOG",
  "ageMonths": 3,
  "size": "MEDIUM",
  "description": "Весело и любопитно кученце, което учи бързо.",
  "vaccinated": true,
  "neutered": false,
  "medicalNotes": "Липсва му едното око и има къса опашка.",
  "requirements": [],
  "status": "AVAILABLE",
  "photos": []
}
```

- `medicalNotes` – can be `null`.
- `photos` – list of photo URLs in display order (added in #25).

| Status | When | Code |
|---|---|---|
| `200 OK` | The animal is public | – |
| `404 Not Found` | The animal does not exist or is a draft | `ANIMAL_NOT_FOUND` |

## 3. All animals (staff)

`GET /api/admin/animals?status=DRAFT&page=0` · role: **STAFF**, **ADMIN**

Returns animals with any status, newest first, 12 per page. `status` is optional.

### Response `200 OK`

Same structure as the catalog, with `status` added to every animal.

| Status | When | Code |
|---|---|---|
| `200 OK` | Animals returned | – |
| `400 Bad Request` | Unknown `status` or negative `page` | `VALIDATION_ERROR` |
| `401` / `403` | Not logged in / not staff | `UNAUTHORIZED` / `FORBIDDEN` |

## 4. One animal (staff)

`GET /api/admin/animals/{id}` · role: **STAFF**, **ADMIN**

Same response as the public details (section 2), but also works for `DRAFT`. Used by the edit form.

| Status | When | Code |
|---|---|---|
| `200 OK` | Animal returned | – |
| `401` / `403` | Not logged in / not staff | `UNAUTHORIZED` / `FORBIDDEN` |
| `404 Not Found` | The animal does not exist | `ANIMAL_NOT_FOUND` |

## 5. Create an animal

`POST /api/admin/animals` · role: **STAFF**, **ADMIN**

### Request body

```json
{
  "name": "Рекс",
  "species": "DOG",
  "ageMonths": 24,
  "size": "MEDIUM",
  "description": "Спокойно и дружелюбно куче.",
  "vaccinated": true,
  "neutered": true,
  "medicalNotes": null,
  "requirements": ["YARD", "NO_CATS"]
}
```

- `name` – required, up to 100 characters.
- `species`, `size` – required.
- `ageMonths` – required, 0 or more.
- `description` – required.
- `vaccinated`, `neutered` – required.
- `medicalNotes` – optional, up to 1000 characters.
- `requirements` – optional, empty list when the animal has none; each value at most once.

The status is not sent: a new animal is always `DRAFT` and is not shown in the catalog.

| Status | When | Body / code |
|---|---|---|
| `201 Created` | The animal is created | Same as the details (section 2), with `"status": "DRAFT"` |
| `400 Bad Request` | A required field is missing or a value is invalid | `VALIDATION_ERROR`, `details` lists the fields |
| `401` / `403` | Not logged in / not staff | `UNAUTHORIZED` / `FORBIDDEN` |

## 6. Edit an animal

`PUT /api/admin/animals/{id}` · role: **STAFF**, **ADMIN**

The request body and the validation are the same as in section 5. All fields are replaced. The status does not change.

| Status | When | Body / code |
|---|---|---|
| `200 OK` | The animal is updated | Same as the details (section 2) |
| `400 Bad Request` | A required field is missing or a value is invalid | `VALIDATION_ERROR` |
| `401` / `403` | Not logged in / not staff | `UNAUTHORIZED` / `FORBIDDEN` |
| `404 Not Found` | The animal does not exist | `ANIMAL_NOT_FOUND` |

## 7. Publish an animal

`POST /api/admin/animals/{id}/publish` · role: **STAFF**, **ADMIN**

Changes the status from `DRAFT` to `AVAILABLE`. The animal appears in the catalog and the Facebook post is published (after the change is saved – if Facebook is unavailable, the status still changes and the post is retried later).

No request body.

| Status | When | Body / code |
|---|---|---|
| `200 OK` | The animal is published | Same as the details (section 2), with `"status": "AVAILABLE"` |
| `401` / `403` | Not logged in / not staff | `UNAUTHORIZED` / `FORBIDDEN` |
| `404 Not Found` | The animal does not exist | `ANIMAL_NOT_FOUND` |
| `409 Conflict` | The animal is not a draft | `ANIMAL_NOT_DRAFT` |

## Interfaces for other modules

These are Java interfaces in the package `animals.api`, not REST endpoints.

- `AnimalQueryService` – returns an animal's id, name, status and requirements, and whether it is available for adoption. Used by Applications (Member 3, requirements rule), Queue (Member 4) and Walks (Member 5).
- `AnimalStatusService` – `markReserved(animalId)`, `markAdopted(animalId)`, `markAvailable(animalId)` (when an approved adoption falls through). Used only by Queue (Member 4).
- Event `AnimalStatusChanged` (animal id, old status, new status) – published after the change is saved. Used by the Facebook publisher (Member 2).