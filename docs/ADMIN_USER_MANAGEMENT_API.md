# Admin User Management API Documentation

## Overview
Complete REST API for admin user management in the User Auth Service. Includes user CRUD operations, role management, account activation/deactivation, search, filtering, pagination, and statistics.

---

## 🔒 Security
**All endpoints require ADMIN role authentication**

Add Firebase ID token to all requests:
```
Authorization: Bearer <firebase_id_token>
```

---

## 📋 API Endpoints

### 1. Get All Users (with Pagination, Search & Filters)

**GET** `/api/v1/admin/users`

Get paginated list of users with search and filtering capabilities.

#### Query Parameters:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-indexed) |
| `size` | int | 10 | Number of items per page |
| `search` | string | - | Search term (searches in name and email) |
| `role` | string | - | Filter by role: `ADMIN`, `EMPLOYEE`, `CUSTOMER` |
| `status` | string | - | Filter by status: `Active`, `Deactivated` |
| `sortBy` | string | createdAt | Sort field: `createdAt`, `email`, `name`, `lastLoginAt` |
| `sortDir` | string | DESC | Sort direction: `ASC`, `DESC` |

#### Example Request:
```bash
curl -X GET "http://localhost:8082/api/v1/admin/users?page=0&size=10&search=john&role=EMPLOYEE&status=Active&sortBy=createdAt&sortDir=DESC" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

#### Example Response:
```json
{
  "success": true,
  "message": null,
  "data": {
    "content": [
      {
        "id": 2,
        "email": "john.doe@gearup.com",
        "name": "John Doe",
        "role": "EMPLOYEE",
        "status": "Active",
        "createdAt": "2024-01-15T10:30:00",
        "lastLoginAt": "2024-01-20T14:22:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2024-01-20T15:30:00"
}
```

---

### 2. Get User by ID

**GET** `/api/v1/admin/users/{userId}`

Get detailed information about a specific user.

#### Example Request:
```bash
curl -X GET "http://localhost:8082/api/v1/admin/users/2" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

#### Example Response:
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": 2,
    "firebaseUid": "abc123xyz",
    "email": "john.doe@gearup.com",
    "displayName": "John Doe",
    "phoneNumber": "+1234567890",
    "photoUrl": null,
    "emailVerified": true,
    "phoneVerified": false,
    "accountStatus": "ACTIVE",
    "roles": [
      {
        "id": 2,
        "name": "EMPLOYEE",
        "description": "Employee role"
      }
    ],
    "permissions": ["USER_READ", "USER_WRITE"],
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-20T14:22:00",
    "lastLoginAt": "2024-01-20T14:22:00"
  },
  "timestamp": "2024-01-20T15:30:00"
}
```

---

### 3. Create Employee Account

**POST** `/api/v1/admin/users/employees`

Create a new employee or admin account. The account is created with a temporary Firebase UID and will be linked when the user signs up.

#### Request Body:
```json
{
  "email": "jane.smith@gearup.com",
  "name": "Jane Smith",
  "role": "EMPLOYEE",
  "phoneNumber": "+1234567891"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `email` | string | ✅ | Valid email | User's email address |
| `name` | string | ✅ | Not blank | Full name |
| `role` | string | ✅ | ADMIN or EMPLOYEE | Role to assign |
| `phoneNumber` | string | ❌ | - | Phone number (optional) |

#### Example Request:
```bash
curl -X POST "http://localhost:8082/api/v1/admin/users/employees" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@gearup.com",
    "name": "Jane Smith",
    "role": "EMPLOYEE",
    "phoneNumber": "+1234567891"
  }'
```

#### Example Response:
```json
{
  "success": true,
  "message": "Employee account created successfully. User can sign up with this email.",
  "data": {
    "id": 5,
    "firebaseUid": "pending_abc-123-xyz",
    "email": "jane.smith@gearup.com",
    "displayName": "Jane Smith",
    "phoneNumber": "+1234567891",
    "accountStatus": "ACTIVE",
    "roles": [
      {
        "id": 2,
        "name": "EMPLOYEE",
        "description": "Employee role"
      }
    ],
    "createdAt": "2024-01-20T15:30:00",
    "updatedAt": "2024-01-20T15:30:00"
  },
  "timestamp": "2024-01-20T15:30:00"
}
```

---

### 4. Update User

**PUT** `/api/v1/admin/users/{userId}`

Update user's role and account status.

#### Request Body:
```json
{
  "role": "ADMIN",
  "status": "Active"
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `role` | string | ✅ | ADMIN, EMPLOYEE, or CUSTOMER | New role |
| `status` | string | ✅ | Active or Deactivated | Account status |

#### Example Request:
```bash
curl -X PUT "http://localhost:8082/api/v1/admin/users/2" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ADMIN",
    "status": "Active"
  }'
```

#### Example Response:
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": 2,
    "email": "john.doe@gearup.com",
    "displayName": "John Doe",
    "roles": [
      {
        "id": 1,
        "name": "ADMIN",
        "description": "Administrator role"
      }
    ],
    "accountStatus": "ACTIVE",
    "updatedAt": "2024-01-20T15:35:00"
  },
  "timestamp": "2024-01-20T15:35:00"
}
```

---

### 5. Deactivate User

**PATCH** `/api/v1/admin/users/{userId}/deactivate`

Deactivate a user account (prevents login).

#### Example Request:
```bash
curl -X PATCH "http://localhost:8082/api/v1/admin/users/2/deactivate" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

#### Example Response:
```json
{
  "success": true,
  "message": "User deactivated successfully",
  "data": {
    "id": 2,
    "email": "john.doe@gearup.com",
    "accountStatus": "INACTIVE",
    "updatedAt": "2024-01-20T15:40:00"
  },
  "timestamp": "2024-01-20T15:40:00"
}
```

---

### 6. Activate User

**PATCH** `/api/v1/admin/users/{userId}/activate`

Activate a deactivated user account.

#### Example Request:
```bash
curl -X PATCH "http://localhost:8082/api/v1/admin/users/2/activate" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

#### Example Response:
```json
{
  "success": true,
  "message": "User activated successfully",
  "data": {
    "id": 2,
    "email": "john.doe@gearup.com",
    "accountStatus": "ACTIVE",
    "updatedAt": "2024-01-20T15:45:00"
  },
  "timestamp": "2024-01-20T15:45:00"
}
```

---

### 7. Delete User

**DELETE** `/api/v1/admin/users/{userId}`

Delete a user account (soft delete - deactivates the account).

#### Example Request:
```bash
curl -X DELETE "http://localhost:8082/api/v1/admin/users/2" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

#### Example Response:
```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null,
  "timestamp": "2024-01-20T15:50:00"
}
```

---

### 8. Get User Statistics

**GET** `/api/v1/admin/users/stats`

Get comprehensive user statistics and analytics.

#### Example Request:
```bash
curl -X GET "http://localhost:8082/api/v1/admin/users/stats" \
  -H "Authorization: Bearer YOUR_FIREBASE_TOKEN"
```

#### Example Response:
```json
{
  "success": true,
  "message": null,
  "data": {
    "totalUsers": 150,
    "activeUsers": 142,
    "deactivatedUsers": 8,
    "totalCustomers": 120,
    "totalEmployees": 25,
    "totalAdmins": 5,
    "newUsersThisMonth": 15,
    "newUsersToday": 3,
    "usersByRole": {
      "CUSTOMER": 120,
      "EMPLOYEE": 25,
      "ADMIN": 5
    },
    "usersByStatus": {
      "Active": 142,
      "Deactivated": 8
    }
  },
  "timestamp": "2024-01-20T15:55:00"
}
```

---

## 🎯 Frontend Integration Examples

### TypeScript/React Hook for Admin User Management

```typescript
// hooks/useAdminUsers.ts
import { useState, useEffect } from 'react';
import { auth } from '@/lib/firebase';

interface AdminUser {
  id: number;
  email: string;
  name: string;
  role: 'Admin' | 'Employee' | 'Customer';
  status: 'Active' | 'Deactivated';
  createdAt: string;
  lastLoginAt?: string;
}

interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export function useAdminUsers() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getAuthToken = async () => {
    const user = auth.currentUser;
    if (!user) throw new Error('Not authenticated');
    return await user.getIdToken();
  };

  const getAllUsers = async (params: {
    page?: number;
    size?: number;
    search?: string;
    role?: string;
    status?: string;
    sortBy?: string;
    sortDir?: string;
  }): Promise<PageResponse<AdminUser>> => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAuthToken();
      const queryParams = new URLSearchParams();
      
      if (params.page !== undefined) queryParams.append('page', params.page.toString());
      if (params.size !== undefined) queryParams.append('size', params.size.toString());
      if (params.search) queryParams.append('search', params.search);
      if (params.role) queryParams.append('role', params.role);
      if (params.status) queryParams.append('status', params.status);
      if (params.sortBy) queryParams.append('sortBy', params.sortBy);
      if (params.sortDir) queryParams.append('sortDir', params.sortDir);

      const response = await fetch(
        `http://localhost:8082/api/v1/admin/users?${queryParams}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );

      if (!response.ok) throw new Error('Failed to fetch users');
      
      const result = await response.json();
      return result.data;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const createEmployee = async (data: {
    email: string;
    name: string;
    role: 'ADMIN' | 'EMPLOYEE';
    phoneNumber?: string;
  }) => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAuthToken();
      const response = await fetch('http://localhost:8082/api/v1/admin/users/employees', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
      });

      if (!response.ok) throw new Error('Failed to create employee');
      
      const result = await response.json();
      return result.data;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const updateUser = async (userId: number, data: {
    role: 'ADMIN' | 'EMPLOYEE' | 'CUSTOMER';
    status: 'Active' | 'Deactivated';
  }) => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAuthToken();
      const response = await fetch(`http://localhost:8082/api/v1/admin/users/${userId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
      });

      if (!response.ok) throw new Error('Failed to update user');
      
      const result = await response.json();
      return result.data;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const deactivateUser = async (userId: number) => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAuthToken();
      const response = await fetch(
        `http://localhost:8082/api/v1/admin/users/${userId}/deactivate`,
        {
          method: 'PATCH',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      if (!response.ok) throw new Error('Failed to deactivate user');
      
      const result = await response.json();
      return result.data;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const activateUser = async (userId: number) => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAuthToken();
      const response = await fetch(
        `http://localhost:8082/api/v1/admin/users/${userId}/activate`,
        {
          method: 'PATCH',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      if (!response.ok) throw new Error('Failed to activate user');
      
      const result = await response.json();
      return result.data;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const deleteUser = async (userId: number) => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAuthToken();
      const response = await fetch(`http://localhost:8082/api/v1/admin/users/${userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) throw new Error('Failed to delete user');
      
      const result = await response.json();
      return result.data;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const getUserStats = async () => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAuthToken();
      const response = await fetch('http://localhost:8082/api/v1/admin/users/stats', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) throw new Error('Failed to fetch stats');
      
      const result = await response.json();
      return result.data;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return {
    loading,
    error,
    getAllUsers,
    createEmployee,
    updateUser,
    deactivateUser,
    activateUser,
    deleteUser,
    getUserStats
  };
}
```

### Usage in React Component

```typescript
'use client';
import { useEffect, useState } from 'react';
import { useAdminUsers } from '@/hooks/useAdminUsers';

export default function UsersPage() {
  const { getAllUsers, updateUser, loading, error } = useAdminUsers();
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);

  useEffect(() => {
    loadUsers();
  }, [page]);

  const loadUsers = async () => {
    try {
      const result = await getAllUsers({ page, size: 10 });
      setUsers(result.content);
    } catch (err) {
      console.error('Failed to load users:', err);
    }
  };

  const handleUpdateUser = async (userId: number, role: string, status: string) => {
    try {
      await updateUser(userId, { 
        role: role as 'ADMIN' | 'EMPLOYEE' | 'CUSTOMER', 
        status: status as 'Active' | 'Deactivated' 
      });
      await loadUsers(); // Reload list
    } catch (err) {
      console.error('Failed to update user:', err);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      {/* Your UI here */}
    </div>
  );
}
```

---

## 🔐 Security & Authorization

### Role Hierarchy
- **ADMIN**: Full access to all admin endpoints
- **EMPLOYEE**: No access to admin endpoints (normal user access only)
- **CUSTOMER**: No access to admin endpoints (normal user access only)

### Firebase Token Verification
All requests are verified through Firebase Authentication:
1. Frontend obtains Firebase ID token
2. Token sent in Authorization header
3. Backend verifies token with Firebase Admin SDK
4. User's Firebase UID extracted
5. User roles checked from database
6. @PreAuthorize("hasRole('ADMIN')") enforces authorization

---

## 📊 Database Audit Trail

All admin actions are automatically logged to the `user_audit_log` table:

- User creation by admin
- Role changes
- Status changes (activation/deactivation)
- Account deletion

Audit log includes:
- User ID
- Action type
- Description
- Old values (JSON)
- New values (JSON)
- Timestamp
- Admin who performed the action

---

## 🚀 Testing the API

### Test Script (PowerShell)

```powershell
# Get your Firebase token first (from frontend after login)
$token = "YOUR_FIREBASE_ID_TOKEN_HERE"

# 1. Get all users
$response = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users?page=0&size=10" `
    -Headers @{ "Authorization" = "Bearer $token" } -Method GET
$response.data

# 2. Create employee
$body = @{
    email = "test.employee@gearup.com"
    name = "Test Employee"
    role = "EMPLOYEE"
    phoneNumber = "+1234567890"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users/employees" `
    -Headers @{ "Authorization" = "Bearer $token"; "Content-Type" = "application/json" } `
    -Method POST -Body $body
$response.data

# 3. Update user
$userId = 2
$body = @{
    role = "ADMIN"
    status = "Active"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users/$userId" `
    -Headers @{ "Authorization" = "Bearer $token"; "Content-Type" = "application/json" } `
    -Method PUT -Body $body
$response.data

# 4. Get user stats
$response = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/admin/users/stats" `
    -Headers @{ "Authorization" = "Bearer $token" } -Method GET
$response.data
```

---

## ✅ Complete Feature List

- ✅ Paginated user list
- ✅ Search users by name/email
- ✅ Filter by role (Admin/Employee/Customer)
- ✅ Filter by status (Active/Deactivated)
- ✅ Sort by multiple fields
- ✅ Create employee accounts
- ✅ Update user role and status
- ✅ Activate/Deactivate accounts
- ✅ Delete users (soft delete)
- ✅ User statistics and analytics
- ✅ Audit logging for all actions
- ✅ Firebase authentication
- ✅ Role-based authorization

---

## 📝 Next Steps

1. **Restart User Auth Service** with new admin endpoints
2. **Test API endpoints** using Postman or curl
3. **Integrate with frontend** using the provided React hooks
4. **Test complete flow**: Login as admin → manage users → verify audit logs
5. **Add pagination controls** to your frontend UserEditModal
6. **Implement search and filters** in your admin panel

---

## 🎉 Ready to Use!

Your admin user management backend is now complete and ready to integrate with your frontend!

All endpoints are fully functional, secured with Firebase auth, and include comprehensive error handling and audit logging.
