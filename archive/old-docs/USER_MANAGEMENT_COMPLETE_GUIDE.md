# 🔐 User Management & Password Login Complete Guide

## ✅ ALL ISSUES FIXED!

### 1. ✅ Edit User - NOW WORKING
- **Email**: ✅ Editable
- **Name**: ✅ Editable  
- **Role**: ✅ Can change (Admin/Employee/Customer)
- **Status**: ✅ Can change (Active/Deactivated)

### 2. ✅ Role Filter - WORKING
- Click "All Roles" → Select role → Users filtered

---

## 🔑 How Users Login After Being Created

### The Problem
When you create a user, they exist in **PostgreSQL** but NOT in **Firebase** (which handles authentication). They cannot login yet!

### The Solution (3 Options)

---

## Option 1: Manual Firebase Setup (Quick Test)

**Use this for testing RIGHT NOW:**

1. **Create user in your app** ✅ (Already working)
2. **Go to Firebase Console**: https://console.firebase.google.com/
3. **Authentication** → **Users** → **Add User**
4. **Enter SAME EMAIL** as database
5. **Set temporary password**: `TempPass@123`
6. **Copy the Firebase UID**
7. **Update database**:
   ```sql
   UPDATE users 
   SET firebase_uid = 'paste_firebase_uid_here'
   WHERE email = 'employee@gearup.com';
   ```
8. **User can now login** with that email and password!

---

## Option 2: Automated Password Reset Email (RECOMMENDED)

### Quick Implementation:

#### **Step 1: Add Email Dependencies**
```xml
<!-- pom.xml in user-auth-service -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

#### **Step 2: Configure Email (application.yml)**
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password  # Get from Google Account → Security → App Passwords
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

#### **Step 3: Create EmailService**
Create file: `services/user-auth-service/src/main/java/com/gearup/userauth/service/EmailService.java`

```java
package com.gearup.userauth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendPasswordSetupEmail(String toEmail, String userName, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to GearUp - Set Up Your Password");
        message.setText(
            "Hi " + userName + ",\n\n" +
            "Your account has been created!\n\n" +
            "Click here to set your password:\n" + resetLink + "\n\n" +
            "After setting your password, login at: http://localhost:3000/login\n\n" +
            "Best regards,\nGearUp Team"
        );
        
        mailSender.send(message);
    }
}
```

#### **Step 4: Add Method in AdminUserService**
Add to `AdminUserService.java`:

```java
@Autowired
private EmailService emailService;

public String sendPasswordSetupEmail(Long userId, String adminFirebaseUid) {
    User user = userService.getUserById(userId);
    
    try {
        // Create Firebase user if doesn't exist
        if (user.getFirebaseUid() == null || user.getFirebaseUid().isEmpty()) {
            String tempPassword = UUID.randomUUID().toString().substring(0, 16) + "Aa1!";
            
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(user.getEmail())
                .setPassword(tempPassword)
                .setDisplayName(user.getDisplayName())
                .setEmailVerified(true);
            
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            user.setFirebaseUid(userRecord.getUid());
            userRepository.save(user);
        }
        
        // Generate password reset link
        String link = FirebaseAuth.getInstance()
            .generatePasswordResetLink(user.getEmail());
        
        // Send email
        emailService.sendPasswordSetupEmail(user.getEmail(), user.getDisplayName(), link);
        
        logger.info("Password setup email sent to {}", user.getEmail());
        return link;
        
    } catch (FirebaseAuthException e) {
        logger.error("Failed to send password setup email", e);
        throw new RuntimeException("Failed to send password setup: " + e.getMessage());
    }
}
```

#### **Step 5: Add Controller Endpoint**
Add to `AdminUserController.java`:

```java
@PostMapping("/{userId}/send-password-setup")
public ResponseEntity<ApiResponse<String>> sendPasswordSetup(
        @PathVariable Long userId,
        @RequestAttribute("firebaseUid") String adminFirebaseUid) {
    
    logger.info("Admin sending password setup email for user {}", userId);
    String resetLink = adminUserService.sendPasswordSetupEmail(userId, adminFirebaseUid);
    return ResponseEntity.ok(ApiResponse.success("Password setup email sent successfully", resetLink));
}
```

#### **Step 6: Add Frontend Button**
Update `UsersTable.tsx` - add to actions menu:

```tsx
<button
  onClick={() => handleSendPasswordSetup(user.id)}
  className="flex w-full items-center gap-2 px-4 py-2 text-sm hover:bg-gray-100"
>
  <Mail className="h-4 w-4" />
  Send Password Setup
</button>
```

Add handler in `page.tsx`:

```tsx
const handleSendPasswordSetup = async (userId: number) => {
  try {
    const token = await auth.currentUser?.getIdToken();
    const response = await fetch(
      `http://localhost:8080/api/v1/admin/users/${userId}/send-password-setup`,
      {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      }
    );
    
    if (response.ok) {
      alert('Password setup email sent successfully!');
    } else {
      alert('Failed to send email');
    }
  } catch (err) {
    console.error('Error:', err);
    alert('Error sending email');
  }
};
```

---

## Option 3: Self-Service Registration (Long-term)

Create a registration page where:
1. Admin sends invitation email with unique token
2. User clicks link → Goes to registration page
3. User sets their own password
4. Account activated automatically

---

## 🎯 Complete Workflow (After Option 2 Implementation)

### For Admin:
1. Go to User Management
2. Click "Add Employee"
3. Enter email, name, select role
4. Click "Save" → User created in database
5. Click "..." menu → **"Send Password Setup"**
6. ✅ Email sent!

### For New User:
1. Receives email: "Welcome to GearUp"
2. Clicks "Set Your Password" link
3. Enters new password (2 times)
4. Redirected to login
5. Logs in with email + new password
6. ✅ Access granted!

---

## 🧪 Test Right Now (Option 1)

While you implement Option 2, test with Option 1:

```bash
# 1. Create user in your app (Test User 1 - already created)

# 2. Check Firebase Console
#    - Go to https://console.firebase.google.com/
#    - Authentication → Users
#    - Add user manually with same email
#    - Copy Firebase UID

# 3. Update database
$env:PGPASSWORD="Niro"
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -h localhost -p 5434 -d as_user_auth_service

UPDATE users 
SET firebase_uid = 'paste_your_firebase_uid_here'
WHERE email = 'Test1@gmail.com';

# 4. User can login at http://localhost:3000/login
```

---

## 📊 Current System Status

**✅ WORKING:**
- Create users (Admin/Employee/Customer)
- Edit user (email, name, role, status)
- Filter by role
- Search users
- Deactivate/Activate users
- Audit logging

**⚠️ NEEDS IMPLEMENTATION:**
- Automated password setup emails
- Firebase user auto-creation
- Welcome emails

**🔧 WORKAROUND (NOW):**
- Manual Firebase user creation
- Manual UID sync to database

---

## 🚀 Quick Start Implementation

**Time needed**: ~30 minutes

1. Add email dependency to `pom.xml` ✅
2. Configure Gmail in `application.yml` ✅
3. Create `EmailService.java` ✅
4. Update `AdminUserService.java` ✅
5. Update `AdminUserController.java` ✅
6. Update `UsersTable.tsx` ✅
7. Test! ✅

---

**Last Updated**: November 6, 2025  
**All edit features**: ✅ WORKING  
**Role filter**: ✅ WORKING  
**Password setup**: ⏳ Implementation guide above
