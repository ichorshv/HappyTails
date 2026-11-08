# API – Applications and Validation (Member 3)

## Common error format

All errors in the project are returned in the same format:

```json
{
  "status": 422,
  "code": "REQUIREMENT_NOT_MET",
  "message": "Животното има нужда от двор.",
  "details": []
}
```

- `status` – the HTTP status code (400, 403, 404, 409, 422…).
- `code` – a short error code the frontend uses to know what happened.
- `message` – the text shown to the user, in Bulgarian.
- `details` – extra information, e.g. which fields are missing.

## 1. Submit an application

`POST /api/applications` · role: **APPLICANT**

### Request body

```json
{
  "animalId": 12,
  "answers": {
    "housingType": "HOUSE",
    "hasYard": true,
    "hasChildrenUnder6": false,
    "hasCats": false,
    "hasDogs": true,
    "hasExperience": true,
    "hoursAlonePerDay": 4,
    "motivation": "Искаме куче, което да ни придружава на разходки."
  },
  "address": {
    "street": "ул. Витоша 10",
    "city": "София",
    "municipality": "Столична"
  }
}
```

- `housingType`: `APARTMENT` | `HOUSE`
- `hasCats` / `hasDogs` – whether cats / dogs already live in the home (regardless of which animal is being adopted).

### Responses

| Status                     | When                                                                      | Body / code                                                                |
|----------------------------|---------------------------------------------------------------------------|----------------------------------------------------------------------------|
| `201 Created`              | All rules pass                                                            | `{ "id": 15, "status": "WAITING" }` (or `YOUR_TURN` if first in the queue) |
| `202 Accepted`             | Geocoding service is unavailable; the address check will be retried later | `{ "id": 15, "status": "SUBMITTED" }`                                      |
| `400 Bad Request`          | The questionnaire is incomplete                                           | `VALIDATION_ERROR`, `details` lists the missing fields                     |
| `401 Unauthorized`         | Not logged in                                                             | `UNAUTHORIZED`                                                             |
| `403 Forbidden`            | Not an applicant (e.g. staff)                                             | `FORBIDDEN`                                                                |
| `404 Not Found`            | The animal does not exist or is not available for adoption                | `ANIMAL_NOT_AVAILABLE`                                                     |
| `409 Conflict`             | There is already an active application for the same animal                | `APPLICATION_DUPLICATE`                                                    |
| `422 Unprocessable Entity` | The applicant already has 3 active applications                           | `APPLICATION_LIMIT_REACHED`                                                |
| `422 Unprocessable Entity` | An answer conflicts with an animal requirement                            | `REQUIREMENT_NOT_MET`                                                      |
| `422 Unprocessable Entity` | The address is too far and not in an allowed municipality                 | `TOO_FAR`                                                                  |

The queue position is not returned here – it comes from the queue module (Member 4).

Missing fields are all returned together in `details` (400). Business rules are checked in this order and only the first
failed rule is returned: duplicate → limit of 3 → animal requirements → distance.

## 2. View an application (staff)

`GET /api/admin/applications/{id}` · role: **STAFF**, **ADMIN**

### Response `200 OK`

```json
{
  "id": 15,
  "applicant": {
    "id": 7,
    "fullName": "Мария Иванова",
    "email": "maria@example.com"
  },
  "animal": {
    "id": 12,
    "name": "Рекс"
  },
  "status": "WAITING",
  "submittedAt": "2026-11-12T10:15:00Z",
  "answers": {
    "housingType": "HOUSE",
    "hasYard": true,
    "hasChildrenUnder6": false,
    "hasCats": false,
    "hasDogs": true,
    "hasExperience": true,
    "hoursAlonePerDay": 4,
    "motivation": "Искаме куче, което да ни придружава на разходки."
  },
  "address": {
    "street": "ул. Витоша 10",
    "city": "София",
    "municipality": "Столична"
  },
  "distanceKm": 12.4,
  "rejectionReason": null
}
```

| Status        | When                           | Code                         |
|---------------|--------------------------------|------------------------------|
| `401` / `403` | Not logged in / not staff      | `UNAUTHORIZED` / `FORBIDDEN` |
| `404`         | The application does not exist | `APPLICATION_NOT_FOUND`      |

## 3. Distance settings

`GET /api/admin/geo-settings` and `PUT /api/admin/geo-settings` · role: **ADMIN**

```json
{
  "shelterLat": 42.6977,
  "shelterLon": 23.3219,
  "maxDistanceKm": 50,
  "allowedMunicipalities": [
    "Столична",
    "Божурище"
  ]
}
```

| Status        | When                                    | Code                         |
|---------------|-----------------------------------------|------------------------------|
| `200 OK`      | Read / saved                            | –                            |
| `400`         | Invalid values (e.g. negative distance) | `VALIDATION_ERROR`           |
| `401` / `403` | Not logged in / not admin               | `UNAUTHORIZED` / `FORBIDDEN` |

## 4. Map of adopted animals (sprint 5)

`GET /api/admin/adopted-map` · role: **STAFF**, **ADMIN**

```json
[
  {
    "animalId": 3,
    "animalName": "Бети",
    "lat": 42.70,
    "lon": 23.32
  }
]
```

Coordinates are rounded to 2 decimal places (about 1 km), so the adopter's exact address is not shown.

## To be agreed

**With Member 2 – animal requirements.** Each requirement is checked against exactly one questionnaire answer:

| Animal requirement  | Meaning                           | Checked with        |
|---------------------|-----------------------------------|---------------------|
| `YARD`              | needs a yard                      | `hasYard`           |
| `NO_CATS`           | does not get along with cats      | `hasCats`           |
| `NO_DOGS`           | does not get along with dogs      | `hasDogs`           |
| `NO_SMALL_CHILDREN` | not suitable for children under 6 | `hasChildrenUnder6` |
| `EXPERIENCE`        | requires an experienced owner     | `hasExperience`     |

**With Member 4 – queue position.** Proposal: the applicant gets status and position from `GET /api/applications/my`
(queue module).