# 🔥 Quick Guide: Get Firebase Token for Testing

## 📋 What You Need

From your Firebase Console screenshot, you have 5 users:
- `gde@gmail.com`
- `samankumara@gmail.com`
- `kalanam890@gmail.com`
- `lithraliyanagunawarden...`
- `udara@gmail.com`

**You need to know the password for one of these users.**

---

## 🚀 Method 1: Using Frontend (Easiest)

### Step 1: Check if you have `.env.local` file in frontend

```powershell
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-frontent"
ls .env.local
```

If it doesn't exist, create it from `.env.example`:
```powershell
Copy-Item .env.example .env.local
notepad .env.local
```

Fill in your Firebase credentials from Firebase Console.

### Step 2: Start Frontend

```powershell
npm run dev
```

### Step 3: Login and Get Token

1. Open browser: `http://localhost:3000/login`
2. Login with any Firebase user (e.g., `gde@gmail.com` + password)
3. Press `F12` (Developer Tools)
4. Go to **Console** tab
5. Run this:
   ```javascript
   firebase.auth().currentUser.getIdToken().then(token => {
     console.log(token);
     navigator.clipboard.writeText(token);
     alert('Token copied!');
   });
   ```
6. Token is copied to clipboard!

---

## 🚀 Method 2: Using PowerShell Script (No Frontend Needed)

### Step 1: Get your Firebase API Key

1. Go to Firebase Console
2. Project Settings → General
3. Copy **Web API Key** (looks like: `AIzaSyC...`)

### Step 2: Run the script

```powershell
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-backend"

# Example:
.\scripts\get-firebase-token.ps1 -Email "gde@gmail.com" -Password "YourPassword"
```

The script will:
- ✅ Get Firebase ID token
- ✅ Copy it to clipboard
- ✅ Display user info

---

## 🚀 Method 3: Using curl/Postman

### Get Token via Firebase REST API:

```powershell
$API_KEY = "YOUR_FIREBASE_API_KEY"
$email = "gde@gmail.com"
$password = "YourPassword"

$body = @{
    email = $email
    password = $password
    returnSecureToken = $true
} | ConvertTo-Json

$response = Invoke-RestMethod `
    -Uri "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"

# Copy token
$response.idToken | Set-Clipboard
Write-Host "Token copied to clipboard!"
Write-Host $response.idToken
```

---

## ⚠️ Important Notes

### 1. **Update Database UIDs**

Your database has test UIDs like `test_admin_uid_12345`, but Firebase generates real UIDs like `ItbZNNTjvsZHAYOycMPsSD3r...`

After getting a token, you need to **update the database** to match Firebase UIDs:

```powershell
# Get the Firebase UID from token (after logging in)
# Then update database:

$env:PGPASSWORD='Niro'
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "
UPDATE users 
SET firebase_uid = 'REAL_FIREBASE_UID_HERE' 
WHERE email = 'gde@gmail.com';
"
```

### 2. **Assign ADMIN Role**

To test admin endpoints, make sure one user has ADMIN role:

```powershell
$env:PGPASSWORD='Niro'

# Check if ADMIN role exists
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "
SELECT * FROM roles WHERE name = 'ADMIN';
"

# If not, create it
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Administrator role');
"

# Assign ADMIN role to user
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -p 5434 -d as_user_auth_service -c "
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.email = 'gde@gmail.com' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
"
```

---

## 🎯 Quick Test Flow

1. **Get Firebase API Key** from Firebase Console
2. **Run token script**:
   ```powershell
   .\scripts\get-firebase-token.ps1 -Email "gde@gmail.com" -Password "YourPassword"
   ```
3. **Token is copied to clipboard**
4. **Run test script**:
   ```powershell
   .\scripts\test-auth-flow.ps1
   ```
5. **Paste token when prompted**

---

## 🔍 Decode Token (Optional)

To see what's inside the token, decode it at: https://jwt.io/

You'll see:
- `sub`: Firebase UID
- `email`: User email
- `exp`: Expiration time
- Custom claims (if any)

---

## 📌 Summary

**Easiest way**: 
1. Run: `.\scripts\get-firebase-token.ps1 -Email "gde@gmail.com" -Password "YourPassword"`
2. Token auto-copied to clipboard
3. Paste when running `.\scripts\test-auth-flow.ps1`

**Make sure**:
- Firebase user exists
- Password is correct
- Database `firebase_uid` matches real Firebase UID
- User has ADMIN role (for admin endpoints)
