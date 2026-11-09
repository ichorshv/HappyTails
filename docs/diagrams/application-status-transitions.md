# HappyTails – Application Status Transitions

## Overview

Each adoption application has one of nine statuses.

All status changes are managed by the Queue module (Member 4).
Other modules must use the public QueueService interface.

## Allowed transitions

| From           | To             | Actor          | Condition                                    |
| -------------- | -------------- | -------------- | -------------------------------------------- |
| SUBMITTED      | WAITING        | System         | Validation passed, other applicants ahead    |
| SUBMITTED      | YOUR_TURN      | System         | Validation passed, first in queue            |
| SUBMITTED      | REJECTED       | System         | Validation failed                            |
| WAITING        | YOUR_TURN      | System         | Previous applicants leave the queue          |
| WAITING        | WITHDRAWN      | Applicant      | Applicant withdraws                          |
| WAITING        | REJECTED       | Staff / System | Staff rejection or animal adopted            |
| YOUR_TURN      | WALK_SCHEDULED | Applicant      | Walk booked within 48-hour deadline          |
| YOUR_TURN      | EXPIRED        | System         | 48 hours passed without a booking            |
| YOUR_TURN      | WITHDRAWN      | Applicant      | Applicant withdraws                          |
| YOUR_TURN      | REJECTED       | Staff          | Staff rejects application                    |
| WALK_SCHEDULED | APPROVED       | Staff          | Staff approves after the walk                |
| WALK_SCHEDULED | REJECTED       | Staff          | Staff rejects application                    |
| WALK_SCHEDULED | WITHDRAWN      | Applicant      | Applicant withdraws                          |
| WALK_SCHEDULED | YOUR_TURN      | Applicant      | Walk cancelled before original deadline      |
| APPROVED       | COMPLETED      | Staff          | Animal handed over                           |
| APPROVED       | WITHDRAWN      | Applicant      | Approved applicant withdraws before handover |

## Terminal statuses

The following statuses are terminal and have no outgoing transitions:

- COMPLETED
- REJECTED
- WITHDRAWN
- EXPIRED

Terminal applications do not count toward the limit of
three active applications per applicant.

## Business rules

### Queue ordering

- Each animal has a separate application queue.
- Applications are ordered by submission time.
- Queue positions are calculated dynamically.
- When an applicant leaves the queue, remaining positions update automatically.

### 48-hour deadline

- When an application becomes YOUR_TURN, a 48-hour deadline starts.
- If no walk is booked before the deadline, the application becomes EXPIRED.
- The next waiting applicant becomes YOUR_TURN and receives a new 48-hour deadline.
- The scheduled job checks for expired deadlines every minute.

### Walk cancellation

- Applicants may cancel a booked walk before their original 48-hour deadline.
- The application returns from WALK_SCHEDULED to YOUR_TURN.
- The original deadline remains unchanged.
- The cancelled walk slot is released.

### Approved adoption

- When an application becomes APPROVED, the animal becomes RESERVED.
- Other applicants remain WAITING.
- If the approved applicant withdraws, the application becomes WITHDRAWN.
- The animal becomes AVAILABLE again.
- The next waiting applicant becomes YOUR_TURN with a new 48-hour deadline.
- When adoption is COMPLETED, the animal becomes ADOPTED.
- Remaining applications become REJECTED with the reason:
  "Animal adopted by another applicant."

### Status history

Every successful status transition must be recorded in
application_status_history with:

- Application ID
- Previous status
- New status
- Timestamp
- Actor (applicant, staff, or system)

## Implementation notes

- Status transitions will be validated by QueueService.
- Invalid transitions must be rejected.
- Status changes and history records must be consistent.
- Concurrent modifications will be handled using optimistic locking.
- Notifications and other side effects will use events after commit.
