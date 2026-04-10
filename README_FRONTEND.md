# School System API - Frontend Development Guide

## 📚 Documentation Files Available

Your API has been fully documented with **3 comprehensive markdown files** optimized for AI agents to build your frontend application.

### 1. **API_DOCUMENTATION.md** (Complete Reference)
- **Size:** 1,324 lines / 23 KB
- **Purpose:** Comprehensive guide with all details
- **Content:**
  - Complete endpoint specifications
  - Request/response examples for each endpoint
  - Data models and DTOs
  - Error handling and status codes
  - Example usage flows
  - Frontend implementation checklist

**Use this for:** Detailed understanding of any endpoint

### 2. **API_QUICK_REFERENCE.md** (Fast Lookup)
- **Size:** 282 lines / 7 KB
- **Purpose:** Quick reference for common operations
- **Content:**
  - All 34 endpoints in tables
  - Quick curl examples
  - Validation rules summary
  - Token management guide
  - Common error responses
  - Frontend integration checklist

**Use this for:** Quick lookups and common patterns

### 3. **API_ENDPOINTS_SUMMARY.md** (Technical Matrix)
- **Size:** 217 lines / 9 KB
- **Purpose:** Technical reference for developers/AI agents
- **Content:**
  - All endpoints categorized
  - Auth requirements matrix
  - Request/response type mapping
  - Status codes for each endpoint
  - DTO reference list

**Use this for:** AI agent parsing and understanding relationships

---

## 🎯 How to Use These Files

### For Claude or ChatGPT
```
Please read these files to understand the API:
- API_DOCUMENTATION.md (comprehensive)
- API_QUICK_REFERENCE.md (quick lookup)
- API_ENDPOINTS_SUMMARY.md (technical details)

Then build me a [React/Vue/etc] frontend application.
```

### For Cursor or Copilot
```
I have 3 API documentation files:
1. API_DOCUMENTATION.md
2. API_QUICK_REFERENCE.md  
3. API_ENDPOINTS_SUMMARY.md

Use these to generate frontend code for my school system.
```

### For Custom AI Agents
The files are structured for easy parsing:
- Markdown format (easy to parse)
- Tables for quick lookups
- JSON examples for request/response bodies
- Clear section headers
- Hierarchical organization

---

## 🔑 Key Information Summary

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
- JWT tokens (Bearer)
- Access token: 15 minutes
- Refresh token: 7 days
- Store tokens in localStorage/sessionStorage

### Total Endpoints
```
✅ 34 endpoints
├── 3 Authentication
├── 3 User Management
├── 8 Classroom Management
├── 5 Student Management
├── 4 Task Management
├── 4 Submission Tracking
├── 3 Attendance
└── 4 Class Sessions
```

### Public Endpoints (No Auth)
- POST /auth/login
- POST /auth/refresh
- POST /users/register

### Protected Endpoints (Auth Required)
- All other 31 endpoints

### Rate Limiting
- Login: 10 req/min per IP
- Register: 5 req/min per IP
- Refresh: 20 req/min per IP

---

## 📋 Common Request/Response Patterns

### Login
```json
POST /auth/login
{
  "email": "teacher@school.com",
  "password": "SecurePass123"
}

Response 200:
{
  "token": "...",
  "refreshToken": "...",
  "firstName": "João",
  "lastName": "Silva",
  "email": "teacher@school.com",
  "role": "USER"
}
```

### Create Resource (with Auth)
```json
POST /students
Authorization: Bearer {token}

{
  "name": "Maria Silva",
  "dateOfBirth": "2008-05-15"
}

Response 201: (empty body)
```

### List Resources
```json
GET /classrooms/user-classrooms
Authorization: Bearer {token}

Response 200:
[
  { "id": 1, "name": "Classe A" },
  { "id": 2, "name": "Classe B" }
]
```

### Update Resource
```json
PATCH /students/10
Authorization: Bearer {token}

{
  "name": "Maria Silva",
  "dateOfBirth": "2008-05-15",
  "observations": "Aluna dedicada"
}

Response 200:
{ "id": 10, "name": "Maria Silva", ... }
```

### Delete Resource
```json
DELETE /students/10
Authorization: Bearer {token}

Response 204: (empty body, no content)
```

---

## 🛠️ Frontend Implementation Steps

### 1. Authentication Setup
- [ ] Create login page
- [ ] Implement JWT token storage
- [ ] Add Authorization header to all requests
- [ ] Implement token refresh before expiration
- [ ] Handle 401 Unauthorized errors
- [ ] Add logout functionality

### 2. User Management
- [ ] Registration page
- [ ] Profile page (view/edit)
- [ ] Change password form

### 3. Classroom Management
- [ ] Create classroom form
- [ ] List classrooms
- [ ] View classroom students
- [ ] Add/remove students from classroom
- [ ] Edit classroom name
- [ ] Delete classroom

### 4. Student Management
- [ ] Create student form (with date picker)
- [ ] List students
- [ ] View student details
- [ ] Edit student information
- [ ] Delete student
- [ ] Add/remove from classrooms

### 5. Task Management
- [ ] Create task form
- [ ] List tasks
- [ ] View task details
- [ ] Delete task

### 6. Submission Tracking
- [ ] Mark student submissions (checkbox/toggle)
- [ ] Bulk mark submissions
- [ ] View submission status per task
- [ ] Edit submission status

### 7. Attendance
- [ ] Select class session
- [ ] Mark attendance (presence/absence)
- [ ] Bulk mark attendance
- [ ] View attendance history

### 8. Class Sessions
- [ ] Create class session (auto datetime)
- [ ] List class sessions
- [ ] View session details
- [ ] Delete session

---

## 🧠 AI Agent Instructions

When using an AI agent to build your frontend, provide this context:

### Minimal Context
```
Build a React/Vue frontend for a school system API.
API endpoints: API_DOCUMENTATION.md
Tech stack: [Your choice]
```

### Full Context
```
I have a REST API for a single-teacher classroom management system.

Documentation:
- API_DOCUMENTATION.md (complete specs)
- API_QUICK_REFERENCE.md (quick lookup)
- API_ENDPOINTS_SUMMARY.md (technical matrix)

Base URL: http://localhost:8080/api/v1
Auth: JWT (Bearer tokens)
Language: Portuguese UI preferred (based on API error messages)

Build a [React/Vue/Angular/Svelte] frontend with:
1. Authentication (login, register, logout)
2. Dashboard (overview of classrooms/students)
3. Classroom management
4. Student management
5. Task and submission tracking
6. Attendance marking

Key requirements:
- Use the provided API documentation
- Handle all error responses
- Implement proper validation
- Show loading states
- Format dates as YYYY-MM-DD
- Use Portuguese error messages from API
```

---

## 📝 Validation Rules (Important!)

### User Registration
- firstName: Required, max 80 chars
- lastName: Required, max 80 chars
- email: Required, valid email, max 120 chars
- password: Required, 8-100 chars

### Student Creation
- name: Required, max 120 chars
- dateOfBirth: Required, MUST BE IN PAST

### Classroom
- name: Required, max 120 chars

### Task
- name: Required, max 255 chars

### Submission
- studentId: Required, positive
- taskId: Required, positive
- submitted: Required, boolean

### Attendance
- classSessionId: Required, positive
- studentsPresence: Required, Map<studentId, boolean>

---

## 🔒 Security Checklist

- [ ] Store tokens securely (not in localStorage if possible)
- [ ] Include Authorization header on all protected requests
- [ ] Handle token expiration and refresh
- [ ] Redirect to login on 401 Unauthorized
- [ ] Show permission error on 403 Forbidden
- [ ] Validate input before submission
- [ ] Handle CORS if frontend is on different domain
- [ ] Don't expose API credentials in frontend code
- [ ] Use HTTPS in production

---

## 🐛 Common Issues & Solutions

### Issue: 401 Unauthorized
**Cause:** Missing or invalid token
**Solution:** 
- Ensure token is stored after login
- Include in Authorization header: `Authorization: Bearer {token}`
- Refresh token if expired

### Issue: 403 Forbidden
**Cause:** Trying to access another user's data
**Solution:**
- API scopes operations to authenticated user
- You can only see/modify your own classrooms, students, etc.

### Issue: 400 Bad Request
**Cause:** Validation error
**Solution:**
- Check field validation rules
- Date must be in YYYY-MM-DD format
- Email must be valid
- Required fields must be present

### Issue: 429 Too Many Requests
**Cause:** Rate limit exceeded
**Solution:**
- Wait for duration in Retry-After header
- Implement exponential backoff in frontend

### Issue: 404 Not Found
**Cause:** Resource doesn't exist
**Solution:**
- Verify ID is correct
- Ensure resource belongs to authenticated user
- Check if it was already deleted

---

## 📞 API Testing

### Test with cURL
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teacher@school.com","password":"SecurePass123"}'

# List classrooms (with token)
curl -X GET http://localhost:8080/api/v1/classrooms/user-classrooms \
  -H "Authorization: Bearer {token}"
```

### Use Swagger UI
```
http://localhost:8080/swagger-ui.html
```
The API has Swagger documentation built-in for testing endpoints interactively.

---

## 🚀 Development Workflow

1. **Read Documentation**
   - Start with API_DOCUMENTATION.md for complete understanding
   - Reference API_QUICK_REFERENCE.md for common patterns
   - Use API_ENDPOINTS_SUMMARY.md for endpoint lookup

2. **Set Up Frontend Project**
   - Initialize your project (React, Vue, Angular, etc.)
   - Configure API base URL
   - Set up routing

3. **Implement Authentication**
   - Login/Register pages
   - Token storage
   - Protected routes

4. **Build Features**
   - Classrooms
   - Students
   - Tasks
   - Submissions
   - Attendance

5. **Test with API**
   - Use Swagger UI for quick testing
   - Use cURL/Postman for manual testing
   - Test error scenarios

---

## 📚 File Locations

All documentation is in the repository root:
```
school-system/
├── API_DOCUMENTATION.md (Main documentation)
├── API_QUICK_REFERENCE.md (Quick lookup)
├── API_ENDPOINTS_SUMMARY.md (Technical matrix)
└── README.md (This file)
```

---

## ✅ Ready to Build!

You now have everything needed to build a complete frontend application for the school system API.

**Next Steps:**
1. Choose your frontend framework (React, Vue, Angular, Svelte, etc.)
2. Read API_DOCUMENTATION.md completely
3. Share these files with your AI agent or team
4. Start building!

---

## 🎓 Example: Building with React + AI Agent

```
I want to build a React app for a school system API.

Here are 3 markdown files with complete API documentation:
1. API_DOCUMENTATION.md - Complete specs
2. API_QUICK_REFERENCE.md - Quick reference
3. API_ENDPOINTS_SUMMARY.md - Technical matrix

Build a React application with:
- Vite or Create React App
- React Router for navigation
- Axios or Fetch for API calls
- JWT authentication
- Pages for: login, dashboard, classrooms, students, tasks, attendance
- Handle all errors properly
- Show loading states
- Format dates correctly

Use Portuguese for UI labels where appropriate.
```

---

**Generated:** April 10, 2026  
**API Version:** 1.0  
**Total Endpoints:** 34  
**Documentation Status:** Complete ✅
