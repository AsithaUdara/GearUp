# 🔐 Complete Authentication Flow Testing Guide

**Date**: November 6, 2025  
**Branch**: `feature/user-service-admin`

---

## 📋 Pre-Test Checklist

### ✅ Backend Services Status
- [x] **User Auth Service**: Running on `http://localhost:8082`
- [x] **Database**: PostgreSQL on port `5434` with test data
- [ ] **Config Server**: Should run on `http://localhost:8888`
- [ ] **Eureka Discovery**: Should run on `http://localhost:8761`
- [ ] **API Gateway**: Should run on `http://localhost:8080`

### ✅ Test Users Available
| Email | Firebase UID | Role | Password (Firebase) | Status |
|-------|--------------|------|---------------------|--------|
| admin@gearup.com | test_admin_uid_12345 | ADMIN | TestAdmin@123 | ACTIVE |
| employee@gearup.com | test_employee_uid_67890 | (No Role) | TestEmployee@123 | ACTIVE |
| customer@gearup.com | test_customer_uid_11111 | CUSTOMER | TestCustomer@123 | ACTIVE |

> **Important**: These users exist in our PostgreSQL database. You need to create matching users in Firebase Authentication Console with the same emails and Firebase UIDs.

---

## 🎯 Testing Phases

### Phase 1: Firebase Authentication Setup (⏳ TO DO)

#### Step 1.1: Create Firebase Test Users
You need to manually create these users in Firebase Console:

1. **Go to Firebase Console**:
   - Navigate to: https://console.firebase.google.com/
   - Select your project
   - Go to: **Authentication** → **Users**

2. **Create Admin User**:
   ```
   Email: admin@gearup.com
   Password: TestAdmin@123
   User UID: test_admin_uid_12345
   ```

3. **Create Employee User**:
   ```
   Email: employee@gearup.com
   Password: TestEmployee@123
   User UID: test_employee_uid_67890
   ```

4. **Create Customer User**:
   ```
   Email: customer@gearup.com
   Password: TestCustomer@123
   User UID: test_customer_uid_11111
   ```

> **Note**: Firebase auto-generates UIDs. To use our specific UIDs, you may need to use Firebase Admin SDK or adjust the UIDs in our database to match Firebase-generated ones.

#### Step 1.2: Set Custom Claims (ADMIN Role)
Use Firebase Admin SDK to set custom claims for admin user:

```javascript
// Run this in Firebase Functions or Admin SDK
admin.auth().setCustomUserClaims('test_admin_uid_12345', {
  role: 'ADMIN',
  admin: true
});
```

Or create a backend endpoint to set claims after first login.

---

### Phase 2: Backend API Testing (Direct)

#### Test 2.1: Health Check ✅
```powershell
# Test service health
curl http://localhost:8082/actuator/health
```

**Expected Response**:
```json
{"status":"UP"}
```

#### Test 2.2: Public Endpoints (No Auth Required)
```powershell
# Test registration endpoint (if available)
curl -X POST http://localhost:8082/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "newuser@test.com",
    "password": "Test@123",
    "firstName": "New",
    "lastName": "User"
  }'
```

#### Test 2.3: Protected Endpoints (Auth Required)

**Step 1**: Get Firebase ID Token (Manual Login)
1. Go to your frontend login page
2. Login with `admin@gearup.com` / `TestAdmin@123`
3. Open Browser DevTools → Console
4. Run:
   ```javascript
   firebase.auth().currentUser.getIdToken().then(token => console.log(token))
   ```
5. Copy the token

**Step 2**: Test User Profile Endpoint
```powershell
$token = "YOUR_FIREBASE_TOKEN_HERE"

curl http://localhost:8082/api/v1/users/profile `
  -H "Authorization: Bearer $token"
```

**Expected Response**:
```json
{
  "id": 2,
  "email": "admin@gearup.com",
  "firstName": "Test",
  "lastName": "Admin",
  "role": "ADMIN",
  "accountStatus": "ACTIVE"
}
```

#### Test 2.4: Admin Endpoints (ADMIN Role Required)

**List All Users** (Admin Only):
```powershell
$token = "YOUR_FIREBASE_TOKEN_HERE"

curl "http://localhost:8082/api/v1/admin/users?page=0&size=10" `
  -H "Authorization: Bearer $token"
```

**Expected Response**:
```json
{
  "users": [
    {
      "id": 2,
      "email": "admin@gearup.com",
      "firstName": "Test",
      "lastName": "Admin",
      "role": "ADMIN",
      "accountStatus": "ACTIVE"
    },
    // ... more users
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalElements": 3
}
```

**Create Employee** (Admin Only):
```powershell
$token = "YOUR_FIREBASE_TOKEN_HERE"

curl -X POST http://localhost:8082/api/v1/admin/users/employee `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "email": "manager@gearup.com",
    "password": "Manager@123",
    "firstName": "Test",
    "lastName": "Manager",
    "phoneNumber": "+1234567890",
    "role": "EMPLOYEE"
  }'
```

**Update User** (Admin Only):
```powershell
$token = "YOUR_FIREBASE_TOKEN_HERE"
$userId = 3

curl -X PUT "http://localhost:8082/api/v1/admin/users/$userId" `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "firstName": "Updated",
    "lastName": "Employee",
    "phoneNumber": "+9876543210",
    "role": "EMPLOYEE"
  }'
```

**Deactivate User** (Admin Only):
```powershell
$token = "YOUR_FIREBASE_TOKEN_HERE"
$userId = 3

curl -X POST "http://localhost:8082/api/v1/admin/users/$userId/deactivate" `
  -H "Authorization: Bearer $token"
```

**Delete User** (Admin Only):
```powershell
$token = "YOUR_FIREBASE_TOKEN_HERE"
$userId = 3

curl -X DELETE "http://localhost:8082/api/v1/admin/users/$userId" `
  -H "Authorization: Bearer $token"
```

**Get User Statistics** (Admin Only):
```powershell
$token = "YOUR_FIREBASE_TOKEN_HERE"

curl http://localhost:8082/api/v1/admin/users/stats `
  -H "Authorization: Bearer $token"
```

**Expected Response**:
```json
{
  "totalUsers": 3,
  "activeUsers": 3,
  "inactiveUsers": 0,
  "adminCount": 1,
  "employeeCount": 1,
  "customerCount": 1
}
```

---

### Phase 3: Frontend Integration Testing

#### Step 3.1: Start Frontend
```powershell
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-frontent"
npm run dev
```

Frontend should be available at: `http://localhost:3000`

#### Step 3.2: Test Login Flow

1. **Navigate to Login Page**:
   - Go to: `http://localhost:3000/login` (or wherever your login is)

2. **Login as Admin**:
   - Email: `admin@gearup.com`
   - Password: `TestAdmin@123`
   - Click "Login"

3. **Verify Token Generation**:
   - Open Browser DevTools → Network Tab
   - Look for Firebase Authentication API calls
   - Verify ID token is generated

4. **Check Backend Sync**:
   - After login, frontend should call backend `/api/v1/users/profile`
   - Backend should recognize Firebase UID and return user data
   - User should be redirected to dashboard

#### Step 3.3: Test Admin Panel

1. **Navigate to Admin Users Page**:
   - Go to: `http://localhost:3000/admin/users`

2. **Verify User List Loads**:
   - Should display all 3 users from database
   - Should show roles, status, actions

3. **Test Create Employee**:
   - Click "Add Employee" button
   - Fill form:
     - Email: `testmanager@gearup.com`
     - Password: `Manager@123`
     - First Name: `Test`
     - Last Name: `Manager`
     - Phone: `+1234567890`
     - Role: `EMPLOYEE`
   - Click "Create"
   - Verify success message
   - Verify new user appears in list

4. **Test Edit User**:
   - Click "Edit" on employee user
   - Change first name to "Updated"
   - Click "Save"
   - Verify changes reflect in list

5. **Test Deactivate User**:
   - Click "Deactivate" on a user
   - Confirm action
   - Verify user status changes to "INACTIVE"

6. **Test Delete User**:
   - Click "Delete" on a user
   - Confirm action
   - Verify user is removed from list

7. **Test Search & Filters**:
   - Use search bar to find users by name/email
   - Filter by role (Admin, Employee, Customer)
   - Filter by status (Active, Inactive)

#### Step 3.4: Test Authorization

1. **Logout as Admin**

2. **Login as Customer**:
   - Email: `customer@gearup.com`
   - Password: `TestCustomer@123`

3. **Try to Access Admin Panel**:
   - Go to: `http://localhost:3000/admin/users`
   - **Expected**: Should be redirected to dashboard or show "Access Denied"

4. **Verify Role-Based Access**:
   - Customer should NOT see admin menu items
   - Customer should only access customer features

---

### Phase 4: Error Handling Testing

#### Test 4.1: Invalid Token
```powershell
curl http://localhost:8082/api/v1/users/profile `
  -H "Authorization: Bearer invalid_token_here"
```

**Expected**: 401 Unauthorized

#### Test 4.2: Expired Token
- Wait for token to expire (1 hour)
- Try accessing protected endpoint
- **Expected**: 401 Unauthorized + refresh token flow

#### Test 4.3: Insufficient Permissions
```powershell
# Login as customer, get token
# Try to access admin endpoint

curl http://localhost:8082/api/v1/admin/users `
  -H "Authorization: Bearer $customerToken"
```

**Expected**: 403 Forbidden

#### Test 4.4: Invalid Request Data
```powershell
curl -X POST http://localhost:8082/api/v1/admin/users/employee `
  -H "Authorization: Bearer $adminToken" `
  -H "Content-Type: application/json" `
  -d '{
    "email": "invalid-email",
    "password": "123"
  }'
```

**Expected**: 400 Bad Request with validation errors

---

### Phase 5: Audit Trail Verification

Check that admin actions are logged:

```powershell
$env:PGPASSWORD='Niro'
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" `
  -U postgres -p 5434 -d as_user_auth_service `
  -c "SELECT * FROM user_audit_log ORDER BY created_at DESC LIMIT 10;"
```

**Verify**:
- User creation logged
- User updates logged
- User deactivation logged
- User deletion logged
- Includes: user_id, action, entity_type, old_values, new_values, ip_address

---

## 🔍 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     AUTHENTICATION FLOW                      │
└─────────────────────────────────────────────────────────────┘

1. User Login (Frontend)
   ↓
2. Firebase Authentication
   ├─ Email/Password → Firebase Auth
   ├─ Generate ID Token (JWT)
   └─ Store User Session
   ↓
3. Backend Request
   ├─ Frontend sends: Authorization: Bearer {ID_TOKEN}
   ├─ API Gateway/Service receives request
   └─ FirebaseAuthFilter intercepts
   ↓
4. Token Verification
   ├─ Verify with Firebase Admin SDK
   ├─ Extract Firebase UID
   └─ Validate signature & expiration
   ↓
5. Database Lookup
   ├─ Find user by firebase_uid
   ├─ Load user roles & permissions
   └─ Check account_status = ACTIVE
   ↓
6. Security Context
   ├─ Create CustomUserDetails
   ├─ Set SecurityContextHolder
   └─ Apply @PreAuthorize rules
   ↓
7. Controller Execution
   ├─ Check method-level security
   ├─ Execute business logic
   └─ Return response
   ↓
8. Audit Logging
   └─ Log action to user_audit_log
```

---

## 📊 Test Results Checklist

### Backend Tests:
- [ ] Health check returns 200 OK
- [ ] Protected endpoints require token
- [ ] Valid token grants access
- [ ] Invalid token returns 401
- [ ] Admin endpoints require ADMIN role
- [ ] Non-admin users get 403 on admin endpoints
- [ ] User profile returns correct data
- [ ] Admin can list all users
- [ ] Admin can create employees
- [ ] Admin can update users
- [ ] Admin can deactivate users
- [ ] Admin can delete users
- [ ] Admin can view user statistics

### Frontend Tests:
- [ ] Login page renders correctly
- [ ] Login with valid credentials succeeds
- [ ] Login with invalid credentials fails
- [ ] Token is stored in memory/state
- [ ] Protected routes redirect if not authenticated
- [ ] Admin panel accessible to admin users
- [ ] Admin panel blocked for non-admin users
- [ ] User list loads and displays correctly
- [ ] Create employee form works
- [ ] Edit user modal works
- [ ] Deactivate user button works
- [ ] Delete user button works
- [ ] Search and filters work
- [ ] Loading states display correctly
- [ ] Error messages display correctly

### Integration Tests:
- [ ] Frontend → Backend communication works
- [ ] Firebase token properly sent in headers
- [ ] Backend validates Firebase tokens
- [ ] Role-based access control enforced
- [ ] Database queries execute correctly
- [ ] Audit logs created for admin actions
- [ ] Error handling works end-to-end

---

## 🐛 Common Issues & Solutions

### Issue 1: "Firebase token verification failed"
**Cause**: Backend Firebase Admin SDK not properly configured  
**Solution**: 
- Check `firebase-service-account.json` exists
- Verify `GOOGLE_APPLICATION_CREDENTIALS` environment variable
- Check Firebase project ID matches

### Issue 2: "User not found in database"
**Cause**: Firebase UID mismatch between Firebase Auth and PostgreSQL  
**Solution**:
- Check Firebase UID in Firebase Console
- Update database: `UPDATE users SET firebase_uid = 'actual_uid' WHERE email = 'admin@gearup.com';`

### Issue 3: "403 Forbidden on admin endpoints"
**Cause**: User doesn't have ADMIN role in database  
**Solution**:
```sql
-- Add ADMIN role to user
INSERT INTO user_roles (user_id, role_id) 
VALUES (
  (SELECT id FROM users WHERE email = 'admin@gearup.com'),
  (SELECT id FROM roles WHERE name = 'ADMIN')
);
```

### Issue 4: "CORS error on frontend"
**Cause**: API Gateway not configured for CORS  
**Solution**: Check `api-gateway` CORS configuration in `application.yml`

### Issue 5: "Token expired"
**Cause**: Firebase tokens expire after 1 hour  
**Solution**: Implement token refresh logic in frontend

---

## 🚀 Quick Test Script

Run this PowerShell script to test all endpoints:

```powershell
# Quick Test Script
Write-Host "🔐 GearUp Authentication Flow Test" -ForegroundColor Cyan
Write-Host ""

# 1. Check service health
Write-Host "1. Checking service health..." -ForegroundColor Yellow
$health = curl http://localhost:8082/actuator/health
Write-Host "   Status: $($health.StatusCode)" -ForegroundColor Green
Write-Host ""

# 2. Get Firebase token (manual step)
Write-Host "2. Get Firebase Token:" -ForegroundColor Yellow
Write-Host "   - Login at frontend: http://localhost:3000/login" -ForegroundColor White
Write-Host "   - Email: admin@gearup.com" -ForegroundColor White
Write-Host "   - Password: TestAdmin@123" -ForegroundColor White
Write-Host "   - Open DevTools Console and run:" -ForegroundColor White
Write-Host "     firebase.auth().currentUser.getIdToken().then(console.log)" -ForegroundColor Cyan
Write-Host ""
$token = Read-Host "   Paste Firebase Token here"
Write-Host ""

# 3. Test profile endpoint
Write-Host "3. Testing profile endpoint..." -ForegroundColor Yellow
try {
    $profile = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/users/profile" `
        -Headers @{"Authorization" = "Bearer $token"}
    Write-Host "   ✓ Profile loaded: $($profile.email)" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Failed: $_" -ForegroundColor Red
}
Write-Host ""

# 4. Test admin users list
Write-Host "4. Testing admin users list..." -ForegroundColor Yellow
try {
    $users = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users?page=0&size=10" `
        -Headers @{"Authorization" = "Bearer $token"}
    Write-Host "   ✓ Found $($users.totalElements) users" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Failed: $_" -ForegroundColor Red
}
Write-Host ""

# 5. Test user stats
Write-Host "5. Testing user statistics..." -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users/stats" `
        -Headers @{"Authorization" = "Bearer $token"}
    Write-Host "   ✓ Total Users: $($stats.totalUsers)" -ForegroundColor Green
    Write-Host "   ✓ Admin Count: $($stats.adminCount)" -ForegroundColor Green
    Write-Host "   ✓ Employee Count: $($stats.employeeCount)" -ForegroundColor Green
    Write-Host "   ✓ Customer Count: $($stats.customerCount)" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Failed: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "✅ Backend API tests complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Next: Test frontend at http://localhost:3000" -ForegroundColor Cyan
```

Save as: `test-auth-flow.ps1`

---

## 📝 Next Steps

1. **Create Firebase Test Users** (Phase 1)
2. **Run Backend API Tests** (Phase 2)
3. **Start Frontend** (Phase 3)
4. **Test Complete Flow** (All Phases)
5. **Document Issues** (Track in GitHub Issues)
6. **Fix & Iterate** (Until all tests pass)

---

**Ready to begin testing! 🎯**
