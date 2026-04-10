# API Endpoints Summary

## All 34 Endpoints

### Authentication (3)
1. `POST /auth/login` - User login
2. `POST /auth/refresh` - Refresh token
3. `POST /users/register` - Register user

### Users (3)
4. `PATCH /users/update` - Update profile
5. `PATCH /users/change-password` - Change password
6. `PATCH /users/disable/{userId}` - Disable user

### Classrooms (8)
7. `POST /classrooms` - Create classroom
8. `GET /classrooms/user-classrooms` - List classrooms
9. `GET /classrooms/{classroomId}` - Get classroom with students
10. `PATCH /classrooms/{classroomId}` - Update classroom
11. `DELETE /classrooms/{classroomId}` - Delete classroom
12. `POST /classrooms/{classroomId}/students/{studentId}` - Add student
13. `POST /classrooms/{classroomId}` - Add multiple students
14. `DELETE /classrooms/{classroomId}/students/{studentId}` - Remove student

### Students (5)
15. `POST /students` - Create student
16. `GET /students/user-students` - List students
17. `GET /students/{studentId}` - Get student
18. `PATCH /students/{studentId}` - Update student
19. `DELETE /students/{studentId}` - Delete student

### Tasks (4)
20. `POST /tasks` - Create task
21. `GET /tasks/user-tasks` - List tasks
22. `GET /tasks/{taskId}` - Get task
23. `DELETE /tasks/{taskId}` - Delete task

### Submissions (4)
24. `POST /submissions` - Create submission
25. `POST /submissions/{taskId}` - Create multiple submissions
26. `PATCH /submissions` - Update submission
27. `GET /submissions/{taskId}` - Get task submissions

### Attendance (3)
28. `POST /attendances` - Mark batch attendance
29. `POST /attendances/{classSessionId}/{studentId}/{presence}` - Mark single attendance
30. `PATCH /attendances` - Update attendance

### Class Sessions (4)
31. `POST /class-sessions` - Create session
32. `GET /class-sessions/user-classSessions` - List sessions
33. `GET /class-sessions/{classSessionId}` - Get session
34. `DELETE /class-sessions/{classSessionId}` - Delete session

---

## Endpoint Details Matrix

| # | Method | Endpoint | Auth | Public | Status |
|----|--------|----------|------|--------|--------|
| 1 | POST | /auth/login | No | Yes | 200 |
| 2 | POST | /auth/refresh | No | Yes | 200 |
| 3 | POST | /users/register | No | Yes | 201 |
| 4 | PATCH | /users/update | Yes | No | 200 |
| 5 | PATCH | /users/change-password | Yes | No | 200 |
| 6 | PATCH | /users/disable/{userId} | Yes | No | 204 |
| 7 | POST | /classrooms | Yes | No | 201 |
| 8 | GET | /classrooms/user-classrooms | Yes | No | 200 |
| 9 | GET | /classrooms/{classroomId} | Yes | No | 200 |
| 10 | PATCH | /classrooms/{classroomId} | Yes | No | 200 |
| 11 | DELETE | /classrooms/{classroomId} | Yes | No | 204 |
| 12 | POST | /classrooms/{classroomId}/students/{studentId} | Yes | No | 200 |
| 13 | POST | /classrooms/{classroomId} | Yes | No | 200 |
| 14 | DELETE | /classrooms/{classroomId}/students/{studentId} | Yes | No | 204 |
| 15 | POST | /students | Yes | No | 201 |
| 16 | GET | /students/user-students | Yes | No | 200 |
| 17 | GET | /students/{studentId} | Yes | No | 200 |
| 18 | PATCH | /students/{studentId} | Yes | No | 200 |
| 19 | DELETE | /students/{studentId} | Yes | No | 204 |
| 20 | POST | /tasks | Yes | No | 201 |
| 21 | GET | /tasks/user-tasks | Yes | No | 200 |
| 22 | GET | /tasks/{taskId} | Yes | No | 200 |
| 23 | DELETE | /tasks/{taskId} | Yes | No | 204 |
| 24 | POST | /submissions | Yes | No | 201 |
| 25 | POST | /submissions/{taskId} | Yes | No | 201 |
| 26 | PATCH | /submissions | Yes | No | 200 |
| 27 | GET | /submissions/{taskId} | Yes | No | 200 |
| 28 | POST | /attendances | Yes | No | 201 |
| 29 | POST | /attendances/{classSessionId}/{studentId}/{presence} | Yes | No | 201 |
| 30 | PATCH | /attendances | Yes | No | 200 |
| 31 | POST | /class-sessions | Yes | No | 201 |
| 32 | GET | /class-sessions/user-classSessions | Yes | No | 200 |
| 33 | GET | /class-sessions/{classSessionId} | Yes | No | 200 |
| 34 | DELETE | /class-sessions/{classSessionId} | Yes | No | 204 |

---

## Request Body Types

| Endpoint | Request Type | Has Body |
|----------|--------------|----------|
| POST /auth/login | AuthRequest | Yes |
| POST /auth/refresh | RefreshTokenRequest | Yes |
| POST /users/register | UserRequest | Yes |
| PATCH /users/update | UserUpdate | Yes |
| PATCH /users/change-password | ChangePasswordRequest | Yes |
| PATCH /users/disable/{userId} | None | No |
| POST /classrooms | ClassroomRequest | Yes |
| GET /classrooms/user-classrooms | None | No |
| GET /classrooms/{classroomId} | None | No |
| PATCH /classrooms/{classroomId} | ClassroomRequest | Yes |
| DELETE /classrooms/{classroomId} | None | No |
| POST /classrooms/{classroomId}/students/{studentId} | None | No |
| POST /classrooms/{classroomId} | AddStudentsRequest | Yes |
| DELETE /classrooms/{classroomId}/students/{studentId} | None | No |
| POST /students | CreateStudentRequest | Yes |
| GET /students/user-students | None | No |
| GET /students/{studentId} | None | No |
| PATCH /students/{studentId} | UpdateStudent | Yes |
| DELETE /students/{studentId} | None | No |
| POST /tasks | CreateTaskRequest | Yes |
| GET /tasks/user-tasks | None | No |
| GET /tasks/{taskId} | None | No |
| DELETE /tasks/{taskId} | None | No |
| POST /submissions | SubmissionRequest | Yes |
| POST /submissions/{taskId} | SubmissionsRequest | Yes |
| PATCH /submissions | SubmissionRequest | Yes |
| GET /submissions/{taskId} | None | No |
| POST /attendances | AttendanceRequest | Yes |
| POST /attendances/{classSessionId}/{studentId}/{presence} | None (URL params) | No |
| PATCH /attendances | UpdateAttendanceRequest | Yes |
| POST /class-sessions | None | No |
| GET /class-sessions/user-classSessions | None | No |
| GET /class-sessions/{classSessionId} | None | No |
| DELETE /class-sessions/{classSessionId} | None | No |

---

## Response Types

| Endpoint | Response Type | Status |
|----------|---------------|--------|
| POST /auth/login | AuthResponse | 200 |
| POST /auth/refresh | RefreshTokenResponse | 200 |
| POST /users/register | Empty | 201 |
| PATCH /users/update | UserResponse | 200 |
| PATCH /users/change-password | Empty | 200 |
| PATCH /users/disable/{userId} | Empty | 204 |
| POST /classrooms | Empty | 201 |
| GET /classrooms/user-classrooms | List<ClassroomResponse> | 200 |
| GET /classrooms/{classroomId} | ClassroomStudentsResponse | 200 |
| PATCH /classrooms/{classroomId} | ClassroomResponse | 200 |
| DELETE /classrooms/{classroomId} | Empty | 204 |
| POST /classrooms/{classroomId}/students/{studentId} | Empty | 200 |
| POST /classrooms/{classroomId} | Empty | 200 |
| DELETE /classrooms/{classroomId}/students/{studentId} | Empty | 204 |
| POST /students | Empty | 201 |
| GET /students/user-students | List<StudentResponse> | 200 |
| GET /students/{studentId} | StudentResponse | 200 |
| PATCH /students/{studentId} | UpdateStudent | 200 |
| DELETE /students/{studentId} | Empty | 204 |
| POST /tasks | Empty | 201 |
| GET /tasks/user-tasks | List<TaskResponse> | 200 |
| GET /tasks/{taskId} | TaskResponse | 200 |
| DELETE /tasks/{taskId} | Empty | 204 |
| POST /submissions | Empty | 201 |
| POST /submissions/{taskId} | Empty | 201 |
| PATCH /submissions | Empty | 200 |
| GET /submissions/{taskId} | SubmissionResponse | 200 |
| POST /attendances | Empty | 201 |
| POST /attendances/{classSessionId}/{studentId}/{presence} | Empty | 201 |
| PATCH /attendances | Empty | 200 |
| POST /class-sessions | Empty | 201 |
| GET /class-sessions/user-classSessions | List<ClassSessionResponse> | 200 |
| GET /class-sessions/{classSessionId} | ClassSessionResponse | 200 |
| DELETE /class-sessions/{classSessionId} | Empty | 204 |

---

## Common DTOs

### Request DTOs
- AuthRequest: email, password
- UserRequest: firstName, lastName, email, password
- UserUpdate: firstName, lastName, email
- ChangePasswordRequest: currentPassword, newPassword
- ClassroomRequest: name
- CreateStudentRequest: name, dateOfBirth
- UpdateStudent: name, dateOfBirth, observations
- CreateTaskRequest: name
- SubmissionRequest: studentId, taskId, submitted
- SubmissionsRequest: students (List of IDs)
- AttendanceRequest: classSessionId, studentsPresence (Map of ID→boolean)
- UpdateAttendanceRequest: classSessionId, studentsPresence
- AddStudentsRequest: students (Set of IDs)
- RefreshTokenRequest: refreshToken

### Response DTOs
- AuthResponse: token, refreshToken, firstName, lastName, email, role
- RefreshTokenResponse: token, refreshToken
- UserResponse: firstName, lastName, email
- ClassroomResponse: id, name
- ClassroomStudentsResponse: id, name, students
- StudentResponse: id, name, dateOfBirth
- StudentClassroomResponse: id, name
- TaskResponse: id, name
- SubmissionResponse: taskId, taskTitle, createdAt, submissions
- StudentSubmission: studentId, studentName, submitted
- ClassSessionResponse: id, createdAt

---

## Pagination
Not implemented yet - all list endpoints return complete results.

## Last Updated
April 10, 2026
