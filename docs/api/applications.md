# API – Applications and Validation (Member 3)

## Common error format

All errors in the project are returned in the same format:

```json
{ "status": 422, "code": "REQUIREMENT_NOT_MET", "message": "Животното има нужда от двор.", "details": [] }
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

| Status | When | Body / code |
| --- | --- | --- |
| `201 Created` | All rules pass | `{ "id": 15, "status": "WAITING" }` (or `YOUR_TURN` if first in the queue) |
| `202 Accepted` | Geocoding service is unavailable; the address check will be retried later | `{ "id": 15, "status": "SUBMITTED" }` |
| `400 Bad Request` | The questionnaire is incomplete | `VALIDATION_ERROR`, `details` lists the missing fields |
| `401 Unauthorized` | Not logged in | `UNAUTHORIZED` |
| `403 Forbidden` | Not an applicant (e.g. staff) | `FORBIDDEN` |
| `404 Not Found` | The animal does not exist or is not available for adoption | `ANIMAL_NOT_AVAILABLE` |
| `409 Conflict` | There is already an active application for the same animal | `APPLICATION_DUPLICATE` |
| `422 Unprocessable Entity` | The applicant already has 3 active applications | `APPLICATION_LIMIT_REACHED` |
| `422 Unprocessable Entity` | An answer conflicts with an animal requirement | `REQUIREMENT_NOT_MET` |
| `422 Unprocessable Entity` | The address is too far and not in an allowed municipality | `TOO_FAR` |

The queue position is not returned here – it comes from the queue module (Member 4).