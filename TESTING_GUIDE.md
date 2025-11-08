# 🧪 Testing Guide - Admin User Management

## 📋 Quick Test Setup

Since you need to test the admin user management features, you have **two options**:

---

## ✅ **Option 1: Create Test Users in Firebase (RECOMMENDED)**

This is the proper way that works with your real authentication flow.

### **Step 1: Create Admin User in Firebase Console**

1. **Open Firebase Console**: https://console.firebase.google.com/
2. **Go to your project**: `gear-up-46adc`
3. **Navigate to**: Authentication → Users
4. **Click**: "Add user"
5. **Enter**:
   - Email: `admin@gearup.com`
   - Password: `Admin@123456` (or any password you want)
6. **Click**: "Add user"
7. **Copy the User UID** (looks like: `xYz123AbC...`)

### **Step 2: Register Admin User in Your Backend**

**Option A: Use the Registration API**

```powershell
# Set your password first
$env:POSTGRES_PASSWORD='Niro'

# Register via API (requires backend running on port 8082)
$body = @{
    firebaseUid = "YOUR_FIREBASE_UID_HERE"  # Paste UID from Firebase Console
    email = "admin@gearup.com"
    displayName = "Admin User"
    phoneNumber = "+1234567890"
    role = "ADMIN"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/api/v1/users/register" `
    -Method POST `
    -Headers @{ "Content-Type" = "application/json" } `
    -Body $body
```

**Option B: Direct Database Insert**

```powershell
$env:POSTGRES_PASSWORD='Niro'

# Replace YOUR_FIREBASE_UID with the actual UID from Firebase Console
$firebaseUid = "YOUR_FIREBASE_UID_HERE"

& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "
INSERT INTO users (firebase_uid, email, display_name, first_name, last_name, email_verified, account_status, created_at, updated_at)
VALUES ('$firebaseUid', 'admin@gearup.com', 'Admin User', 'Admin', 'User', true, 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email = 'admin@gearup.com' AND r.name = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

SELECT u.id, u.email, u.display_name, r.name as role FROM users u 
LEFT JOIN user_roles ur ON u.id = ur.user_id 
LEFT JOIN roles r ON ur.role_id = r.id 
WHERE u.email = 'admin@gearup.com';
"
```

### **Step 3: Login and Test**

1. **Start Frontend**:
   ```powershell
   cd "..\GearUp-frontent"
   npm run dev
   ```

2. **Open Browser**: http://localhost:3000

3. **Login**:
   - Email: `admin@gearup.com`
   - Password: `Admin@123456` (or whatever you set)

4. **Navigate to**: `/admin/users`

5. **Test Features**:
   - ✅ View all users
   - ✅ Search users
   - ✅ Filter by role/status
   - ✅ Create new employee
   - ✅ Update user role
   - ✅ Activate/Deactivate users
   - ✅ Delete users

---

## 🔧 **Option 2: Use Test Data Script (Quick Testing)**

If you don't want to set up Firebase users yet, use test data.

### **Step 1: Insert Test Users**

```powershell
# Set password
$env:POSTGRES_PASSWORD='Niro'

# Run the test data script
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -f ".\scripts\create-test-admin.sql"
```

This creates:
- **Admin**: `admin@gearup.com` (Firebase UID: `test_admin_uid_12345`)
- **Employee**: `employee@gearup.com` (Firebase UID: `test_employee_uid_67890`)
- **Customer**: `customer@gearup.com` (Firebase UID: `test_customer_uid_11111`)

### **Step 2: Test with Postman/Curl (Backend API Only)**

Since these are test users without real Firebase accounts, you can't login through the frontend. Instead, test the API directly:

**Get Admin Token** (you'll need to mock this):

For testing purposes, you can bypass Firebase auth temporarily:

1. **Temporarily disable Firebase filter** (ONLY FOR LOCAL TESTING):
   
   Comment out the Firebase filter in your security config temporarily.

2. **Or use a real Firebase token** from another user and manually assign ADMIN role to that user.

### **Step 3: Test API Endpoints**

```powershell
# Get all users
curl http://localhost:8082/api/v1/admin/users

# Get user by ID
curl http://localhost:8082/api/v1/admin/users/1

# Get stats
curl http://localhost:8082/api/v1/admin/users/stats
```

⚠️ **Note**: This option is limited because you can't test the complete frontend flow without real Firebase authentication.

---

## 🎯 **Recommended Approach**

**Use Option 1** - Create real Firebase users. Here's why:

✅ Tests the **complete authentication flow**  
✅ Tests **Firebase token verification**  
✅ Tests **frontend-backend integration**  
✅ Tests **role-based access control**  
✅ **Production-ready** testing  

---

## 📝 **Test Checklist**

Once you have your admin user set up:

### **Backend Tests**
- [ ] Health check: `curl http://localhost:8082/actuator/health`
- [ ] Get all users: `curl http://localhost:8082/api/v1/admin/users`
- [ ] Create employee via API
- [ ] Update user role via API
- [ ] Get user statistics via API

### **Frontend Tests**
- [ ] Login as admin
- [ ] Navigate to `/admin/users`
- [ ] View users list (should load from backend)
- [ ] Search for user by name/email
- [ ] Filter by role (Admin/Employee/Customer)
- [ ] Filter by status (Active/Deactivated)
- [ ] Click "Add Employee" → Create new employee
- [ ] Click edit icon → Update user role
- [ ] Click edit icon → Change status to Deactivated
- [ ] Click edit icon → Change status back to Active
- [ ] Click delete icon → Confirm → User deleted
- [ ] Verify changes in database

### **Database Verification**
```powershell
$env:POSTGRES_PASSWORD='Niro'

# Check users
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "
SELECT u.id, u.email, u.display_name, r.name as role, u.account_status 
FROM users u 
LEFT JOIN user_roles ur ON u.id = ur.user_id 
LEFT JOIN roles r ON ur.role_id = r.id 
ORDER BY u.created_at DESC;
"

# Check audit log
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "
SELECT * FROM user_audit_log ORDER BY timestamp DESC LIMIT 10;
"
```

---

## 🐛 **Troubleshooting**

### **Problem: "Authentication required"**
**Solution**: Make sure you're logged in with a user that has ADMIN role.

```powershell
# Check user's role in database
$env:POSTGRES_PASSWORD='Niro'
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "
SELECT u.email, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE u.email = 'admin@gearup.com';
"
```

### **Problem: "403 Forbidden"**
**Solution**: User doesn't have ADMIN role. Assign it:

```powershell
$env:POSTGRES_PASSWORD='Niro'
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email = 'admin@gearup.com' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
"
```

### **Problem: "User not found"**
**Solution**: Register the user first (see Step 2 above).

### **Problem: "Invalid Firebase token"**
**Solution**: 
1. Make sure you're using a real Firebase user
2. Token expires after 1 hour - login again
3. Check Firebase config in frontend `.env.local`

---

## 📞 **Quick Reference**

### **Test Credentials (If using Option 1)**
- **Email**: `admin@gearup.com`
- **Password**: `Admin@123456` (or whatever you set in Firebase)
- **Role**: ADMIN

### **API Endpoints**
- Base URL: `http://localhost:8082`
- Admin API: `/api/v1/admin/users`
- Health: `/actuator/health`

### **Database Connection**
```powershell
$env:POSTGRES_PASSWORD='Niro'
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service
```

### **Frontend**
- Dev server: `npm run dev` (port 3000)
- Admin panel: http://localhost:3000/admin/users

---

## 🎉 **You're Ready!**

Follow **Option 1** above, create your admin user in Firebase, and you'll be able to test the complete admin user management flow! 🚀
