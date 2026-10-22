# ACM API

## Glossary

### Conventions

+ **Client** - Client application.
+ **Status** - HTTP status code of response.
+ All the possible responses are listed under ‘Responses’ for each method. Only one of them is issued per request
  server.
+ All responses are in JSON format.
+ All request parameters are mandatory unless explicitly marked as `[optional]`
+ The type of values accepted for a request parameter are shown the values column like this `[10|<any number>]` .The |
  symbol means OR. If the parameter is `[optional]`, the default value is shown in blue bold text, as 10 is written
  in `[10|<any number>]`.

### Status Codes

All status codes are standard HTTP status codes. The below ones are used in this API.

+ 2XX - Success of some kind
+ 4XX - Error occurred in client’s part
+ 5XX - Error occurred in server’s part

| Status Code | Description           |
|-------------|-----------------------|
| 200         | OK                    |
| 201         | Created               |
| 202         | Accepted (queued)     |
| 400         | Bad request           |
| 401         | Authentication Failed |
| 403         | Forbidden             |
| 404         | Not found             |
| 405         | Not allowed           |
| 409         | Conflict              |
| 412         | Precondition Failed   |
| 413         | Request too large     |
| 500         | Internal Server error |
| 501         | Not Implemented       |
| 503         | Service Unavailable   |

## Authentication

### Login

Authenticate the user with the system and obtain the auth_token

#### Request

| Method | URL             |
|--------|-----------------|
| POST   | `/user/login` |

| Type | Params     | Values |
|------|------------|--------|
| Body | `email`    | string |
| Body | `password` | string |

#### Response

| Status | Response                                          |
|--------|---------------------------------------------------|
| 200    | `{"jwtToken": <auth_key>, "user": <user object>}` |
| 400    | Bad request                                       |
| 401    | Incorect email or password                        |
| 404    | User with provided email not found                |
| 500    | Internal Server Error                             |

> auth_key (string) - all further API calls must have this key in header

### Register

Register new users to the platform

#### Request

| Method | URL       |
|--------|-----------|
| POST   | `/user` |

| Type | Params     | Values  |
|------|------------|---------|
| Body | `email`    | string  |
| Body | `password` | string  |
| Body | `name`     | string  |
| Body | `branch`   | string  |
| Body | `sid`      | integer |

#### Response

| Status | Response               |
|--------|------------------------|
| 200    | `<verification_token>` |
| 400    | Bad request            |
| 500    | Internal Server Error  |

> verification_token (string) - Token to verify email (will be sent via email by frontend SMTP)

### Verify

#### Request

| Method | URL             |
|--------|-----------------|
| POST   | `/user/login` |

| Type | Params     | Values |
|------|------------|--------|
| Body | `email`    | string |
| Body | `password` | string |

#### Response

| Status | Response                   |
|--------|----------------------------|
| 200    | `Verification Successful!` |
| 404    | Token not found            |
| 500    | Internal Server Error      |

## User

### Get User Info

### Get user by id

### Update user

### Get rank by score

### Get leaderboard

### Get leaderboard by batch

## Events

### Create Event

### Update Event

### Delete Event

### End Event

### Get all events

### Get ongoing events

### Get single event

### Get events by branch

### Get Event by role

### Get next event

## Attendance

Backend system supporting automated rotating QR-based attendance tracking and manual administrative check-ins for PEC ACM events.

### Security & Token Architecture

- **Rotating Token Window**: Tokens rotate every 5 seconds using an HMAC-SHA256 signature generated from the session's secret key, session ID, and current 5-second epoch window.
- **Grace Period**: Server accepts tokens from the current window and configurable recent windows (default 5-second grace period) to tolerate scanning latency and network jitter.
- **Constant-Time Verification**: Signatures are compared using `MessageDigest.isEqual` to prevent timing attacks.
- **Anti-Replay & Concurrency**: Uniqueness is enforced at the database level across `(event_id, user_id)` preventing duplicate records even across simultaneous concurrent requests.
- **Authentication**: Attendance is tied to the caller's JWT authentication token; client cannot impersonate other users.
- **Verification Rule**: The participant's account must have `verified = true`. Unverified users are rejected with `403 Forbidden`.
- **SID Validation**: Student IDs for manual entry must be valid 8-digit numbers (`10000000`–`99999999`) matching PEC standard format.

---

### Start Attendance Session

Starts a new attendance session for an event and generates a session secret. Any existing active session for the event is stopped.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| POST | `/attendance/sessions/start` | EB, Core, Admin |

| Type | Params | Type | Description |
|------|--------|------|-------------|
| Body | `eventId` | integer | Mandatory. Event ID to start session for. |
| Body | `durationMinutes` | integer | Optional (default: 15). Session validity in minutes. |
| Body | `tokenExpirySeconds` | integer | Optional (default: 5). QR rotation frequency in seconds. |
| Body | `gracePeriodSeconds` | integer | Optional (default: 5). Verification grace window in seconds. |
| Body | `participantXp` | integer | Optional (default: 1). Custom XP awarded to participants. |

#### Response

| Status | Description |
|--------|-------------|
| 201 | Created. Returns `AttendanceSessionResponse`. |
| 400 | Bad request (missing eventId or event ended). |
| 401 | Unauthorized. |
| 403 | Forbidden (insufficient permissions). |
| 404 | Event not found. |

---

### Stop Attendance Session

Manually stops an ongoing attendance session.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| POST | `/attendance/sessions/{sessionId}/stop` | EB, Core, Admin |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns updated `AttendanceSessionResponse` with `STOPPED` status. |
| 401 | Unauthorized. |
| 403 | Forbidden. |
| 404 | Session not found. |

---

### Get Session By ID

Fetches details and attendee count for a specific attendance session.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| GET | `/attendance/sessions/{sessionId}` | EB, Core, Admin |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns `AttendanceSessionResponse`. |
| 404 | Session not found. |

---

### Get Active Session for Event

Fetches the currently active attendance session for an event.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| GET | `/attendance/sessions/active/{eventId}` | EB, Core, Admin |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns active `AttendanceSessionResponse`. |
| 404 | No active session or session expired. |

---

### Get All Sessions for Event

Fetches all attendance sessions (active, stopped, expired) created for an event.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| GET | `/attendance/events/{eventId}/sessions` | EB, Core, Admin |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns list of `AttendanceSessionResponse`. |
| 404 | Event not found. |

---

### Get Rotating Attendance Token

Fetches the current rotating token for projection or display on the organizer screen. Rotates every ~5 seconds.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| GET | `/attendance/sessions/{sessionId}/token` | EB, Core, Admin |

#### Response

| Status | Response |
|--------|----------|
| 200 | `{"token": "<string>", "expiresInSeconds": 5, "sessionId": 100}` |
| 400 | Session inactive or expired. |
| 404 | Session not found. |

---

### Scan Attendance (QR Code)

Validates the scanned rotating token and marks attendance for the logged-in member.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| POST | `/attendance/scan` | Member and above |

| Type | Params | Type | Description |
|------|--------|------|-------------|
| Body | `token` | string | Mandatory. Token scanned from rotating QR code. |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns `AttendanceRecordResponse` (`method: QR_SCAN`). |
| 400 | Invalid, expired, or tampered token / session stopped. |
| 401 | Unauthorized (JWT missing or expired). |
| 403 | Forbidden (user account unverified). |
| 409 | Conflict (attendance already marked for this event). |

---

### Check Member Attendance Status

Checks if the authenticated member has already marked attendance for an event.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| GET | `/attendance/events/{eventId}/my-status` | Member and above |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns `AttendanceRecordResponse` with check-in timestamp and method. |
| 204 | No Content. User has not yet marked attendance for this event. |
| 401 | Unauthorized. |

---

### Manual Attendance Marking

Allows an administrator to manually record attendance for a participant via SID (8 digits) or email.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| POST | `/attendance/sessions/{sessionId}/manual` | EB, Core, Admin |

| Type | Params | Type | Description |
|------|--------|------|-------------|
| Body | `sid` | integer | Optional (must be 8-digit PEC format e.g. `21103001` if supplied). |
| Body | `email` | string | Optional if SID supplied. Student email address. |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns `AttendanceRecordResponse` (`method: MANUAL_ADMIN`, with `markedByAdminId`). |
| 400 | Bad request (invalid SID length, missing SID/email, or student unverified). |
| 404 | Student or session not found. |
| 409 | Conflict (attendance already recorded for this event). |

---

### Get Session Attendees (Paginated)

Retrieves paginated list of attendees marked in a specific session.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| GET | `/attendance/sessions/{sessionId}/attendees` | EB, Core, Admin |

| Type | Params | Default | Description |
|------|--------|---------|-------------|
| Query | `offset` | 0 | Page index (0-based). |
| Query | `pageSize` | 20 | Page size. |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns Spring `Page<AttendanceRecordResponse>`. |
| 404 | Session not found. |

---

### Search Participants

Autocomplete search for participants by name, email, or SID prefix.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| GET | `/attendance/participants/search?query={query}` | EB, Core, Admin |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns `List<EventUserDetails>` (`id`, `name`, `email`, `dp`). |

---

### Export Attendee Emails

Exports a deduplicated list of attendee email addresses for an event.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| GET | `/attendance/events/{eventId}/attendee-emails` | EB, Core, Admin |

#### Response

| Status | Description |
|--------|-------------|
| 200 | OK. Returns `List<String>` of unique email addresses. |

---

### Award Participant XP

Awards XP in bulk to all event attendees who have not yet received XP. Idempotent.

#### Request

| Method | URL | Auth Role |
|--------|-----|-----------|
| POST | `/attendance/events/{eventId}/award-xp` | Core, Admin |

| Type | Params | Default | Description |
|------|--------|---------|-------------|
| Query | `xpAmount` | [session XP / 1] | Optional override XP amount per participant. |

#### Response

| Status | Response |
|--------|----------|
| 200 | `{"awardedCount": <int>, "message": "Attendance XP successfully awarded"}` |
| 400 | Bad request (invalid XP amount). |
| 404 | Event not found. |

