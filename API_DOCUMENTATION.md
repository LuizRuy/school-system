# School System API Documentation

## Overview
Complete REST API for a single-teacher classroom management system built with Spring Boot 4.0.1 and PostgreSQL.

**Base URL:** `http://localhost:8080/api/v1`  
**API Version:** v1  
**Content-Type:** application/json  
**Authentication:** JWT Token-based (Bearer Token)

---

## Table of Contents
1. [Authentication](#authentication)
2. [Endpoints](#endpoints)
3. [Data Models](#data-models)
4. [Error Handling](#error-handling)
5. [Status Codes](#status-codes)

---

## Authentication

### Overview
The API uses JWT (JSON Web Token) authentication with access tokens and refresh tokens.

### Token Generation
- Access tokens expire in 15 minutes
- Refresh tokens expire in 7 days
- All protected endpoints require `Authorization: Bearer {token}` header

### Public Endpoints (No Token Required)
- `POST /auth/login` - User login
- `POST /auth/refresh` - Refresh access token
- `POST /users/register` - Register new user

### Rate Limiting (Public Endpoints)
- **Login:** 10 requests/minute per IP
- **Register:** 5 requests/minute per IP
- **Refresh Token:** 20 requests/minute per IP

---

## Endpoints

### Authentication Endpoints

#### 1. Login
```http
POST /auth/login
```

**Description:** Authenticate user and receive access and refresh tokens

**Request Body:**
```json
{
  "email": "teacher@school.com",
  "password": "SecurePass123"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "firstName": "João",
  "lastName": "Silva",
  "email": "teacher@school.com",
  "role": "USER"
}
```

**Error Responses:**
- `400` - Invalid credentials
- `429` - Too many login attempts

---

#### 2. Refresh Token
```http
POST /auth/refresh
```

**Description:** Get a new access token using refresh token

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**
- `404` - Refresh token not found
- `400` - Token expired
- `429` - Too many refresh attempts

---

### User Endpoints

#### 3. Register User
```http
POST /users/register
```

**Description:** Create a new user account

**Request Body:**
```json
{
  "firstName": "João",
  "lastName": "Silva",
  "email": "teacher@school.com",
  "password": "SecurePass123"
}
```

**Validation Rules:**
- firstName: Required, max 80 characters
- lastName: Required, max 80 characters
- email: Required, valid email format, max 120 characters
- password: Required, 8-100 characters

**Success Response (201):** Empty body

**Error Responses:**
- `400` - Validation error or invalid data
- `409` - Email already exists
- `429` - Too many registration attempts

---

#### 4. Update User
```http
PATCH /users/update
```

**Authentication:** Required (Bearer Token)

**Description:** Update user profile information

**Request Body:**
```json
{
  "firstName": "João",
  "lastName": "Silva",
  "email": "teacher@school.com"
}
```

**Success Response (200):**
```json
{
  "firstName": "João",
  "lastName": "Silva",
  "email": "teacher@school.com"
}
```

**Error Responses:**
- `400` - Validation error
- `401` - Unauthorized
- `404` - User not found
- `409` - Email already in use

---

#### 5. Change Password
```http
PATCH /users/change-password
```

**Authentication:** Required (Bearer Token)

**Description:** Change user password

**Request Body:**
```json
{
  "currentPassword": "OldPass123",
  "newPassword": "NewSecurePass456"
}
```

**Validation Rules:**
- currentPassword: Required, 8-100 characters
- newPassword: Required, 8-100 characters

**Success Response (200):** Empty body

**Error Responses:**
- `400` - Invalid current password or validation error
- `401` - Unauthorized
- `404` - User not found

---

#### 6. Disable User
```http
PATCH /users/disable/{userId}
```

**Authentication:** Required (ADMIN role only)

**Description:** Disable a user account

**Path Parameters:**
- `userId` (Long): User ID to disable

**Success Response (204):** Empty body

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied (not ADMIN)
- `404` - User not found

---

### Classroom Endpoints

#### 7. Create Classroom
```http
POST /classrooms
```

**Authentication:** Required (Bearer Token)

**Description:** Create a new classroom

**Request Body:**
```json
{
  "name": "Classe de Matemática 10º ano"
}
```

**Validation Rules:**
- name: Required, max 120 characters

**Success Response (201):** Empty body

**Error Responses:**
- `400` - Validation error
- `401` - Unauthorized

---

#### 8. Get All User Classrooms
```http
GET /classrooms/user-classrooms
```

**Authentication:** Required (Bearer Token)

**Description:** Get all classrooms for the authenticated user

**Success Response (200):**
```json
[
  {
    "id": 1,
    "name": "Classe de Matemática 10º ano"
  },
  {
    "id": 2,
    "name": "Classe de Ciências 11º ano"
  }
]
```

**Error Responses:**
- `401` - Unauthorized

---

#### 9. Get Classroom by ID
```http
GET /classrooms/{classroomId}
```

**Authentication:** Required (Bearer Token)

**Description:** Get classroom details with list of students

**Path Parameters:**
- `classroomId` (Long): Classroom ID

**Success Response (200):**
```json
{
  "id": 1,
  "name": "Classe de Matemática 10º ano",
  "students": [
    {
      "id": 10,
      "name": "Maria Silva"
    },
    {
      "id": 11,
      "name": "Pedro Santos"
    }
  ]
}
```

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied (not classroom owner)
- `404` - Classroom not found

---

#### 10. Update Classroom
```http
PATCH /classrooms/{classroomId}
```

**Authentication:** Required (Bearer Token)

**Description:** Update classroom name

**Path Parameters:**
- `classroomId` (Long): Classroom ID

**Request Body:**
```json
{
  "name": "Classe de Matemática 10º ano - Atualizado"
}
```

**Success Response (200):**
```json
{
  "id": 1,
  "name": "Classe de Matemática 10º ano - Atualizado"
}
```

**Error Responses:**
- `400` - Validation error
- `401` - Unauthorized
- `403` - Access denied
- `404` - Classroom not found

---

#### 11. Delete Classroom
```http
DELETE /classrooms/{classroomId}
```

**Authentication:** Required (Bearer Token)

**Description:** Delete a classroom

**Path Parameters:**
- `classroomId` (Long): Classroom ID

**Success Response (204):** Empty body

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Classroom not found

---

#### 12. Assign Student to Classroom
```http
POST /classrooms/{classroomId}/students/{studentId}
```

**Authentication:** Required (Bearer Token)

**Description:** Add a single student to a classroom

**Path Parameters:**
- `classroomId` (Long): Classroom ID
- `studentId` (Long): Student ID

**Success Response (200):** Empty body

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Classroom or student not found

---

#### 13. Assign Multiple Students to Classroom
```http
POST /classrooms/{classroomId}
```

**Authentication:** Required (Bearer Token)

**Description:** Add multiple students to a classroom

**Path Parameters:**
- `classroomId` (Long): Classroom ID

**Request Body:**
```json
{
  "students": [10, 11, 12]
}
```

**Success Response (200):** Empty body

**Error Responses:**
- `400` - Empty student list
- `401` - Unauthorized
- `403` - Access denied
- `404` - Classroom or student not found

---

#### 14. Remove Student from Classroom
```http
DELETE /classrooms/{classroomId}/students/{studentId}
```

**Authentication:** Required (Bearer Token)

**Description:** Remove a student from a classroom

**Path Parameters:**
- `classroomId` (Long): Classroom ID
- `studentId` (Long): Student ID

**Success Response (204):** Empty body

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Classroom or student not found

---

### Student Endpoints

#### 15. Create Student
```http
POST /students
```

**Authentication:** Required (Bearer Token)

**Description:** Create a new student record

**Request Body:**
```json
{
  "name": "Maria Silva",
  "dateOfBirth": "2008-05-15"
}
```

**Validation Rules:**
- name: Required, max 120 characters
- dateOfBirth: Required, must be in the past

**Success Response (201):** Empty body

**Error Responses:**
- `400` - Validation error or future date
- `401` - Unauthorized

---

#### 16. Get All User Students
```http
GET /students/user-students
```

**Authentication:** Required (Bearer Token)

**Description:** Get all students for the authenticated user

**Success Response (200):**
```json
[
  {
    "id": 10,
    "name": "Maria Silva",
    "dateOfBirth": "2008-05-15"
  },
  {
    "id": 11,
    "name": "Pedro Santos",
    "dateOfBirth": "2008-09-22"
  }
]
```

**Error Responses:**
- `401` - Unauthorized

---

#### 17. Get Student by ID
```http
GET /students/{studentId}
```

**Authentication:** Required (Bearer Token)

**Description:** Get student details

**Path Parameters:**
- `studentId` (Long): Student ID

**Success Response (200):**
```json
{
  "id": 10,
  "name": "Maria Silva",
  "dateOfBirth": "2008-05-15"
}
```

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Student not found

---

#### 18. Update Student
```http
PATCH /students/{studentId}
```

**Authentication:** Required (Bearer Token)

**Description:** Update student information

**Path Parameters:**
- `studentId` (Long): Student ID

**Request Body:**
```json
{
  "name": "Maria Silva",
  "dateOfBirth": "2008-05-15",
  "observations": "Aluna dedicada"
}
```

**Validation Rules:**
- name: Max 120 characters
- dateOfBirth: Must be in the past
- observations: Optional

**Success Response (200):**
```json
{
  "id": 10,
  "name": "Maria Silva",
  "dateOfBirth": "2008-05-15",
  "observations": "Aluna dedicada"
}
```

**Error Responses:**
- `400` - Validation error
- `401` - Unauthorized
- `403` - Access denied
- `404` - Student not found

---

#### 19. Delete Student
```http
DELETE /students/{studentId}
```

**Authentication:** Required (Bearer Token)

**Description:** Delete a student record

**Path Parameters:**
- `studentId` (Long): Student ID

**Success Response (204):** Empty body

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Student not found

---

### Task Endpoints

#### 20. Create Task
```http
POST /tasks
```

**Authentication:** Required (Bearer Token)

**Description:** Create a new task/assignment

**Request Body:**
```json
{
  "name": "Lista de Exercícios - Capítulo 5"
}
```

**Validation Rules:**
- name: Required, max 255 characters

**Success Response (201):** Empty body

**Error Responses:**
- `400` - Validation error
- `401` - Unauthorized

---

#### 21. Get All User Tasks
```http
GET /tasks/user-tasks
```

**Authentication:** Required (Bearer Token)

**Description:** Get all tasks for the authenticated user

**Success Response (200):**
```json
[
  {
    "id": 1,
    "name": "Lista de Exercícios - Capítulo 5"
  },
  {
    "id": 2,
    "name": "Prova de Análise Combinatória"
  }
]
```

**Error Responses:**
- `401` - Unauthorized

---

#### 22. Get Task by ID
```http
GET /tasks/{taskId}
```

**Authentication:** Required (Bearer Token)

**Description:** Get task details

**Path Parameters:**
- `taskId` (Long): Task ID

**Success Response (200):**
```json
{
  "id": 1,
  "name": "Lista de Exercícios - Capítulo 5"
}
```

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Task not found

---

#### 23. Delete Task
```http
DELETE /tasks/{taskId}
```

**Authentication:** Required (Bearer Token)

**Description:** Delete a task

**Path Parameters:**
- `taskId` (Long): Task ID

**Success Response (204):** Empty body

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Task not found

---

### Submission Endpoints

#### 24. Create Submission
```http
POST /submissions
```

**Authentication:** Required (Bearer Token)

**Description:** Mark a single student's task submission status

**Request Body:**
```json
{
  "studentId": 10,
  "taskId": 1,
  "submitted": true
}
```

**Validation Rules:**
- studentId: Required, must be positive
- taskId: Required, must be positive
- submitted: Required, boolean

**Success Response (201):** Empty body

**Error Responses:**
- `400` - Validation error
- `401` - Unauthorized
- `404` - Student or task not found

---

#### 25. Create Multiple Submissions
```http
POST /submissions/{taskId}
```

**Authentication:** Required (Bearer Token)

**Description:** Mark multiple students' task submission status

**Path Parameters:**
- `taskId` (Long): Task ID

**Request Body:**
```json
{
  "students": [10, 11, 12]
}
```

**Success Response (201):** Empty body

**Error Responses:**
- `400` - Empty student list or validation error
- `401` - Unauthorized
- `404` - Task or student not found

---

#### 26. Update Submission
```http
PATCH /submissions
```

**Authentication:** Required (Bearer Token)

**Description:** Update a submission status

**Request Body:**
```json
{
  "studentId": 10,
  "taskId": 1,
  "submitted": false
}
```

**Success Response (200):** Empty body

**Error Responses:**
- `400` - Validation error
- `401` - Unauthorized
- `404` - Submission, student, or task not found

---

#### 27. Get Task Submissions
```http
GET /submissions/{taskId}
```

**Authentication:** Required (Bearer Token)

**Description:** Get all submissions for a specific task

**Path Parameters:**
- `taskId` (Long): Task ID

**Success Response (200):**
```json
{
  "taskId": 1,
  "taskTitle": "Lista de Exercícios - Capítulo 5",
  "createdAt": "2026-04-10T15:30:00",
  "submissions": [
    {
      "studentId": 10,
      "studentName": "Maria Silva",
      "submitted": true
    },
    {
      "studentId": 11,
      "studentName": "Pedro Santos",
      "submitted": false
    }
  ]
}
```

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Task not found

---

### Attendance Endpoints

#### 28. Create Attendance Record
```http
POST /attendances
```

**Authentication:** Required (Bearer Token)

**Description:** Create attendance records for multiple students in a class session

**Request Body:**
```json
{
  "classSessionId": 1,
  "studentsPresence": {
    "10": true,
    "11": false,
    "12": true
  }
}
```

**Validation Rules:**
- classSessionId: Required, must be positive
- studentsPresence: Required, non-empty map
  - Key (studentId): Required, must be positive
  - Value (presence): Required, boolean

**Success Response (201):** Empty body

**Error Responses:**
- `400` - Validation error or empty attendance list
- `401` - Unauthorized
- `404` - Class session or student not found

---

#### 29. Create Single Attendance Record
```http
POST /attendances/{classSessionId}/{studentId}/{presence}
```

**Authentication:** Required (Bearer Token)

**Description:** Create a single attendance record

**Path Parameters:**
- `classSessionId` (Long): Class session ID
- `studentId` (Long): Student ID
- `presence` (boolean): Attendance status (true for present, false for absent)

**Success Response (201):** Empty body

**Error Responses:**
- `401` - Unauthorized
- `404` - Class session or student not found

---

#### 30. Update Attendance Record
```http
PATCH /attendances
```

**Authentication:** Required (Bearer Token)

**Description:** Update attendance records for a class session

**Request Body:**
```json
{
  "classSessionId": 1,
  "studentsPresence": {
    "10": false,
    "11": true,
    "12": true
  }
}
```

**Success Response (200):** Empty body

**Error Responses:**
- `400` - Validation error
- `401` - Unauthorized
- `404` - Class session or student not found

---

### Class Session Endpoints

#### 31. Create Class Session
```http
POST /class-sessions
```

**Authentication:** Required (Bearer Token)

**Description:** Create a new class session for today

**Success Response (201):** Empty body

**Error Responses:**
- `401` - Unauthorized

---

#### 32. Get All User Class Sessions
```http
GET /class-sessions/user-classSessions
```

**Authentication:** Required (Bearer Token)

**Description:** Get all class sessions for the authenticated user

**Success Response (200):**
```json
[
  {
    "id": 1,
    "createdAt": "2026-04-10T08:00:00"
  },
  {
    "id": 2,
    "createdAt": "2026-04-10T10:30:00"
  }
]
```

**Error Responses:**
- `401` - Unauthorized

---

#### 33. Get Class Session by ID
```http
GET /class-sessions/{classSessionId}
```

**Authentication:** Required (Bearer Token)

**Description:** Get class session details

**Path Parameters:**
- `classSessionId` (Long): Class session ID

**Success Response (200):**
```json
{
  "id": 1,
  "createdAt": "2026-04-10T08:00:00"
}
```

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Class session not found

---

#### 34. Delete Class Session
```http
DELETE /class-sessions/{classSessionId}
```

**Authentication:** Required (Bearer Token)

**Description:** Delete a class session

**Path Parameters:**
- `classSessionId` (Long): Class session ID

**Success Response (204):** Empty body

**Error Responses:**
- `401` - Unauthorized
- `403` - Access denied
- `404` - Class session not found

---

## Data Models

### User
```json
{
  "id": 1,
  "firstName": "João",
  "lastName": "Silva",
  "email": "teacher@school.com",
  "createdAt": "2026-04-10T10:00:00",
  "updatedAt": "2026-04-10T15:30:00",
  "status": "ENABLED",
  "role": "USER"
}
```

### Student
```json
{
  "id": 10,
  "name": "Maria Silva",
  "dateOfBirth": "2008-05-15",
  "observations": "Aluna dedicada",
  "createdAt": "2026-04-10T10:00:00",
  "updatedAt": "2026-04-10T15:30:00"
}
```

### Classroom
```json
{
  "id": 1,
  "name": "Classe de Matemática 10º ano",
  "createdAt": "2026-04-10T10:00:00",
  "updatedAt": "2026-04-10T15:30:00"
}
```

### Task
```json
{
  "id": 1,
  "name": "Lista de Exercícios - Capítulo 5",
  "createdAt": "2026-04-10T10:00:00"
}
```

### Submission
```json
{
  "id": 1,
  "studentId": 10,
  "taskId": 1,
  "submitted": true
}
```

### Attendance
```json
{
  "id": 1,
  "studentId": 10,
  "classSessionId": 1,
  "presence": true
}
```

### ClassSession
```json
{
  "id": 1,
  "userId": 1,
  "createdAt": "2026-04-10T08:00:00"
}
```

### Enumerations

**Role:**
- `ADMIN` - Administrator
- `USER` - Regular user (Teacher)

**Status:**
- `ENABLED` - User account is active
- `DISABLED` - User account is disabled

---

## Error Handling

### Error Response Format
```json
{
  "status": 400,
  "message": "Validation failed for object='userRequest'. Error count: 1",
  "timestamp": "2026-04-10T19:02:00",
  "errors": [
    {
      "field": "email",
      "message": "Email inválido"
    }
  ]
}
```

### Validation Error Example
```json
{
  "status": 400,
  "message": "Bad Request",
  "errors": [
    {
      "field": "firstName",
      "message": "Nome é obrigatorio"
    },
    {
      "field": "password",
      "message": "Senha deve ter entre 8 e 100 caracteres"
    }
  ]
}
```

---

## Status Codes

| Code | Meaning | Use Case |
|------|---------|----------|
| `200` | OK | Successful GET, PATCH, or POST request |
| `201` | Created | Successful resource creation |
| `204` | No Content | Successful DELETE or resource modification |
| `400` | Bad Request | Invalid input or validation error |
| `401` | Unauthorized | Missing or invalid JWT token |
| `403` | Forbidden | Insufficient permissions (e.g., trying to delete another user's data) |
| `404` | Not Found | Resource doesn't exist |
| `409` | Conflict | Resource already exists (e.g., duplicate email) |
| `429` | Too Many Requests | Rate limit exceeded on public endpoints |
| `500` | Internal Server Error | Server error |

---

## Common Implementation Notes

### JWT Token Usage
All protected endpoints require the `Authorization` header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Request Headers
```
Content-Type: application/json
Authorization: Bearer {access_token}
```

### Response Headers
- All responses include standard HTTP headers
- Rate-limited endpoints include `Retry-After` header

### Timezone
All timestamps are in ISO 8601 format (UTC): `YYYY-MM-DDTHH:mm:ss`

### Pagination
List endpoints return all records (pagination to be implemented in future versions).

### Data Ownership
All operations are scoped to the authenticated user. Users can only access their own classrooms, students, tasks, etc.

---

## Example Usage Flow

### 1. User Registration
```bash
POST /users/register
{
  "firstName": "João",
  "lastName": "Silva",
  "email": "teacher@school.com",
  "password": "SecurePass123"
}
→ 201 Created
```

### 2. User Login
```bash
POST /auth/login
{
  "email": "teacher@school.com",
  "password": "SecurePass123"
}
→ 200 OK
{
  "token": "...",
  "refreshToken": "...",
  "firstName": "João",
  "lastName": "Silva",
  "email": "teacher@school.com",
  "role": "USER"
}
```

### 3. Create Classroom
```bash
POST /classrooms
Authorization: Bearer {token}
{
  "name": "Classe de Matemática 10º ano"
}
→ 201 Created
```

### 4. Create Students
```bash
POST /students
Authorization: Bearer {token}
{
  "name": "Maria Silva",
  "dateOfBirth": "2008-05-15"
}
→ 201 Created
```

### 5. Assign Students to Classroom
```bash
POST /classrooms/1
Authorization: Bearer {token}
{
  "students": [10, 11, 12]
}
→ 200 OK
```

### 6. Create Task
```bash
POST /tasks
Authorization: Bearer {token}
{
  "name": "Lista de Exercícios - Capítulo 5"
}
→ 201 Created
```

### 7. Mark Submissions
```bash
POST /submissions/1
Authorization: Bearer {token}
{
  "students": [10, 11]
}
→ 201 Created
```

### 8. Create Class Session
```bash
POST /class-sessions
Authorization: Bearer {token}
→ 201 Created
```

### 9. Mark Attendance
```bash
POST /attendances
Authorization: Bearer {token}
{
  "classSessionId": 1,
  "studentsPresence": {
    "10": true,
    "11": true,
    "12": false
  }
}
→ 201 Created
```

---

## Frontend Implementation Checklist

- [ ] Implement JWT token storage (localStorage/sessionStorage)
- [ ] Add token refresh logic before expiration
- [ ] Handle 401/403 errors (redirect to login)
- [ ] Implement rate limiting feedback (429 handling)
- [ ] Add form validation matching backend constraints
- [ ] Display validation error messages to users
- [ ] Handle all documented error responses
- [ ] Format dates consistently (ISO 8601)
- [ ] Add loading states for async operations
- [ ] Implement logout (clear stored tokens)

---

## Support
For issues or questions about the API, contact the development team.

**Last Updated:** April 10, 2026  
**API Version:** 1.0  
**Framework:** Spring Boot 4.0.1  
**Database:** PostgreSQL 16  
