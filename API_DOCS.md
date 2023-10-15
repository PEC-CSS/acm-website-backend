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
| POST   | `v1/user/login` |

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
| POST   | `v1/user` |

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
| POST   | `v1/user/login` |

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

