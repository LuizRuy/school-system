# School System API - Quick Reference Guide

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication
All endpoints except login, register, and refresh require:
```
Authorization: Bearer {access_token}
```

---

## Public Endpoints (No Auth Required)

### Authentication
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/auth/login` | Login and get tokens |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/users/register` | Create new account |

---

## Protected Endpoints (Auth Required)

### Users
| Method | Endpoint | Purpose |
|--------|----------|---------|
| PATCH | `/users/update` | Update profile |
| PATCH | `/users/change-password` | Change password |
| PATCH | `/users/disable/{userId}` | Disable user (ADMIN only) |

### Classrooms
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/classrooms` | Create classroom |
| GET | `/classrooms/user-classrooms` | List all classrooms |
| GET | `/classrooms/{classroomId}` | Get classroom with students |
| PATCH | `/classrooms/{classroomId}` | Update classroom |
| DELETE | `/classrooms/{classroomId}` | Delete classroom |
| POST | `/classrooms/{classroomId}/students/{studentId}` | Add student |
| POST | `/classrooms/{classroomId}` | Add multiple students |
| DELETE | `/classrooms/{classroomId}/students/{studentId}` | Remove student |

### Students
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/students` | Create student |
| GET | `/students/user-students` | List all students |
| GET | `/students/{studentId}` | Get student details |
| PATCH | `/students/{studentId}` | Update student |
| DELETE | `/students/{studentId}` | Delete student |

### Tasks
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/tasks` | Create task |
| GET | `/tasks/user-tasks` | List all tasks |
| GET | `/tasks/{taskId}` | Get task |
| DELETE | `/tasks/{taskId}` | Delete task |

### Submissions
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/submissions` | Mark single submission |
| POST | `/submissions/{taskId}` | Mark multiple submissions |
| PATCH | `/submissions` | Update submission |
| GET | `/submissions/{taskId}` | Get task submissions |

### Attendance
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/attendances` | Mark attendance batch |
| POST | `/attendances/{classSessionId}/{studentId}/{presence}` | Mark single attendance |
| PATCH | `/attendances` | Update attendance |

### Class Sessions
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/class-sessions` | Create session |
| GET | `/class-sessions/user-classSessions` | List sessions |
| GET | `/class-sessions/{classSessionId}` | Get session |
| DELETE | `/class-sessions/{classSessionId}` | Delete session |

---

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success (GET/PATCH/POST response) |
| 201 | Created (POST resource created) |
| 204 | No Content (DELETE/successful operation) |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found (resource doesn't exist) |
| 409 | Conflict (duplicate resource) |
| 429 | Too Many Requests (rate limited) |
| 500 | Server Error |

---

## Common Request Examples

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "teacher@school.com",
    "password": "SecurePass123"
  }'
```

### Create Classroom (with token)
```bash
curl -X POST http://localhost:8080/api/v1/classrooms \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "Classe de Matemática 10º ano"
  }'
```

### List Classrooms (with token)
```bash
curl -X GET http://localhost:8080/api/v1/classrooms/user-classrooms \
  -H "Authorization: Bearer {token}"
```

### Create Student
```bash
curl -X POST http://localhost:8080/api/v1/students \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "Maria Silva",
    "dateOfBirth": "2008-05-15"
  }'
```

---

## Validation Rules

### User Registration
- **firstName**: Required, max 80 chars
- **lastName**: Required, max 80 chars
- **email**: Required, valid email, max 120 chars
- **password**: Required, 8-100 chars

### Student Creation
- **name**: Required, max 120 chars
- **dateOfBirth**: Required, must be in the past

### Classroom
- **name**: Required, max 120 chars

### Task
- **name**: Required, max 255 chars

### Submission
- **studentId**: Required, positive number
- **taskId**: Required, positive number
- **submitted**: Required, boolean

### Attendance
- **classSessionId**: Required, positive number
- **studentsPresence**: Required, map of studentId→boolean

---

## Token Management

### Access Token
- **Expiration**: 15 minutes
- **Format**: JWT
- **Usage**: Include in `Authorization: Bearer {token}` header

### Refresh Token
- **Expiration**: 7 days
- **Usage**: Send to `/auth/refresh` to get new access token

### Token Refresh Flow
```
1. Access token expires (401 Unauthorized)
2. Send refresh token to POST /auth/refresh
3. Receive new access token and refresh token
4. Update stored tokens
5. Retry original request
```

---

## Rate Limiting (Public Endpoints)

| Endpoint | Limit | Window |
|----------|-------|--------|
| POST /auth/login | 10 requests | 1 minute |
| POST /auth/refresh | 20 requests | 1 minute |
| POST /users/register | 5 requests | 1 minute |

**Response**: 429 Too Many Requests with `Retry-After` header

---

## Error Response Format

### Validation Error
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email inválido"
    }
  ]
}
```

### Not Found
```json
{
  "status": 404,
  "message": "Classroom not found with id: 999"
}
```

### Unauthorized
```json
{
  "status": 401,
  "message": "Unauthorized"
}
```

### Forbidden
```json
{
  "status": 403,
  "message": "You do not have permission to access this classroom"
}
```

---

## Frontend Integration Checklist

- [ ] Store JWT token after login
- [ ] Include token in all protected request headers
- [ ] Implement token refresh before expiration
- [ ] Handle 401 errors (redirect to login)
- [ ] Handle 403 errors (show permission message)
- [ ] Handle validation errors (show field messages)
- [ ] Handle 429 rate limiting (show retry message)
- [ ] Format dates as YYYY-MM-DD
- [ ] Convert timestamps to local time
- [ ] Show loading states during requests

---

## Development Notes

- **Framework**: Spring Boot 4.0.1
- **Database**: PostgreSQL 16
- **Authentication**: JWT (15 min access, 7 day refresh)
- **Validation**: Jakarta Bean Validation
- **Language**: Portuguese error messages
- **Timezone**: UTC (ISO 8601 format)

---

## API Documentation
See `API_DOCUMENTATION.md` for complete endpoint documentation with all request/response examples.

**Last Updated**: April 10, 2026
