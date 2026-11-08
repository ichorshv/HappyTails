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