# HappyTails – Queue & Status API Contract

## 1. Overview

This document describes the REST API contract for the
Queue & Status Management module (Member 4).

Base path: `/api`

All endpoints require authentication.

The Queue module manages application statuses, queue
positions, withdrawals and staff decisions.

This document defines the planned API contract.
The REST controllers will be implemented in later sprints.

## 2. Application Statuses

Possible values:

- SUBMITTED
- WAITING
- YOUR_TURN
- WALK_SCHEDULED
- APPROVED
- COMPLETED
- REJECTED
- WITHDRAWN
- EXPIRED

## 3. GET /api/applications/my

**Purpose:** Retrieve the authenticated user's applications,
including their current status and queue position.

**Method:** GET

**Role:** APPLICANT

**Request:** No request body.

**Response: 200 OK**

```json
[
  {
    "id": 15,
    "animalId": 7,
    "status": "WAITING",
    "queuePosition": 2,
    "deadline": null
  },
  {
    "id": 18,
    "animalId": 9,
    "status": "YOUR_TURN",
    "queuePosition": 1,
    "deadline": "2026-11-11T14:00:00Z"
  }
]
```

**Errors:**

| Code | Meaning                              |
| ---- | ------------------------------------ |
| 401  | User is not authenticated            |
| 403  | User does not have the required role |

**Integration note:**

This endpoint belongs to the Applications module (Member 3).
The Queue module provides queue position and status information.
No duplicate controller should be created.

## 4. GET /api/applications/{id}/status

**Purpose:** Retrieve the status and queue position of
a specific application.

**Method:** GET

**Role:** APPLICANT (owner), STAFF, ADMIN

**Path parameter:** `id` – Application ID.

**Request:** No request body.

**Response: 200 OK**

```json
{
  "applicationId": 15,
  "status": "WAITING",
  "queuePosition": 2,
  "deadline": null
}
```

**Errors:**

| Code | Meaning                                      |
| ---- | -------------------------------------------- |
| 400  | Invalid application ID                       |
| 401  | User is not authenticated                    |
| 403  | User is not allowed to view this application |
| 404  | Application not found                        |

## 5. POST /api/applications/{id}/withdraw

**Purpose:** Withdraw an adoption application.

**Method:** POST

**Role:** APPLICANT (owner)

**Path parameter:** `id` – Application ID.

**Request:** No request body.

**Response: 200 OK**

```json
{
  "applicationId": 15,
  "status": "WITHDRAWN",
  "message": "Application withdrawn successfully."
}
```

**Errors:**

| Code | Meaning                                           |
| ---- | ------------------------------------------------- |
| 400  | Invalid application ID                            |
| 401  | User is not authenticated                         |
| 403  | User is not the application owner                 |
| 404  | Application not found                             |
| 409  | Withdrawal is not allowed from the current status |

**Business rules:**

- Only the application owner can withdraw it.
- Withdrawal is allowed from WAITING, YOUR_TURN,
  WALK_SCHEDULED and APPROVED.
- WITHDRAWN is a terminal status.
- If the withdrawn application was first in the queue,
  the next eligible applicant is promoted.
- If the application was APPROVED, the animal becomes
  AVAILABLE again and the queue resumes.

## 6. POST /api/staff/applications/{id}/decision

**Purpose:** Approve or reject an adoption application.

**Method:** POST

**Role:** STAFF, ADMIN

**Path parameter:** `id` – Application ID.

**Request body:**

```json
{
  "decision": "APPROVE",
  "reason": "Applicant meets adoption requirements."
}
```

Allowed decision values:

- APPROVE
- REJECT

**Response: 200 OK**

```json
{
  "applicationId": 15,
  "status": "APPROVED",
  "message": "Application approved successfully."
}
```

For REJECT, the resulting status is REJECTED.

**Errors:**

| Code | Meaning                                            |
| ---- | -------------------------------------------------- |
| 400  | Invalid request or decision                        |
| 401  | User is not authenticated                          |
| 403  | User does not have STAFF or ADMIN role             |
| 404  | Application not found                              |
| 409  | Decision is not allowed from the current status    |
| 422  | Application does not satisfy business requirements |

**Business rules:**

- APPROVE is allowed from WALK_SCHEDULED.
- REJECT is allowed from WAITING, YOUR_TURN and WALK_SCHEDULED.
- When approved, the animal becomes RESERVED.
- Other applications for the same animal remain WAITING
  until adoption is completed.

## 7. Common Error Response

All errors should follow a consistent JSON format.

Example:

```json
{
  "timestamp": "2026-11-09T14:00:00Z",
  "status": 409,
  "error": "CONFLICT",
  "message": "Invalid application status transition.",
  "path": "/api/applications/15/withdraw"
}
```

## 8. Integration Notes

- Member 3 owns application submission and validation.
- Member 4 owns queue ordering and status transitions.
- Member 5 owns walk scheduling.
- Member 1 owns authentication and authorization.
- Member 2 owns animal status updates.

The Queue module exposes QueueService for internal
communication between backend modules.

The REST API should not duplicate functionality
already owned by other modules.

## 9. Implementation Status

Sprint 1: API contract only.

REST controllers, persistence, validation and
business logic will be implemented in later sprints.

The final endpoint structure must be reviewed
with the Applications and Walks modules.
