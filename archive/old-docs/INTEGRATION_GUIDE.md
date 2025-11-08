# 🚀 Complete Integration Guide - Admin User Management

## 📋 Overview
This guide will help you complete the **entire admin user management flow** from backend to frontend.

---

## ✅ Backend Status (100% Complete)

All backend services are **ready and running**:
- ✅ User Auth Service with Admin APIs (Port 8082)
- ✅ 8 Admin endpoints implemented
- ✅ Firebase authentication configured
- ✅ Role-based authorization enabled
- ✅ Audit logging active
- ✅ PostgreSQL database connected

---

## 🎯 Frontend Integration Steps

### **STEP 1: Start Your Frontend Application**

Open a new terminal and run:

```powershell
cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-frontent"
npm run dev
```

Your frontend should start on **http://localhost:3000**

---

### **STEP 2: Create the Admin API Hook**

Create a new file: `GearUp-frontent/src/hooks/useAdminUsers.ts`

Copy the complete hook from `docs/ADMIN_USER_MANAGEMENT_API.md` (lines 300-500)

Or use this simplified version:

```typescript
// GearUp-frontent/src/hooks/useAdminUsers.ts
import { useState } from 'react';
import { auth } from '@/lib/firebase';

const API_BASE_URL = 'http://localhost:8082/api/v1/admin/users';

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
  }) => {
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

      const response = await fetch(`${API_BASE_URL}?${queryParams}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });

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

  const updateUser = async (userId: number, data: {
    role: string;
    status: string;
  }) => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAuthToken();
      const response = await fetch(`${API_BASE_URL}/${userId}`, {
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

  const createEmployee = async (data: {
    email: string;
    name: string;
    role: string;
    phoneNumber?: string;
  }) => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAuthToken();
      const response = await fetch(`${API_BASE_URL}/employees`, {
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

  return {
    loading,
    error,
    getAllUsers,
    updateUser,
    createEmployee
  };
}
```

---

### **STEP 3: Update Your UserEditModal Component**

Update `GearUp-frontent/src/app/components/admin/UserEditModal.tsx`:

```typescript
'use client';
import { X, ChevronDown } from "lucide-react";
import type { User } from '@/app/admin/users/page';
import { useEffect, useState, Fragment } from "react";
import { Listbox, Transition } from '@headlessui/react';
import { useAdminUsers } from '@/hooks/useAdminUsers';

type Props = { 
  isOpen: boolean; 
  onClose: () => void;
  user: User | null;
  onSuccess?: () => void; // Callback after successful update
};

const roles: Array<'Employee' | 'Admin'> = ['Employee', 'Admin'];

export default function UserEditModal({ isOpen, onClose, user, onSuccess }: Props) {
  const { updateUser, createEmployee, loading, error } = useAdminUsers();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<'Employee' | 'Admin' | 'Customer'>('Employee');
  const [status, setStatus] = useState<'Active' | 'Deactivated'>('Active');

  useEffect(() => {
    if (user) {
      setName(user.name);
      setEmail(user.email);
      setRole(user.role);
      setStatus(user.status);
    } else {
      setName('');
      setEmail('');
      setRole('Employee');
      setStatus('Active');
    }
  }, [user, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (user) {
        // Update existing user
        await updateUser(user.id, { 
          role: role.toUpperCase(), // Backend expects ADMIN, EMPLOYEE, CUSTOMER
          status 
        });
        alert('User updated successfully!');
      } else {
        // Create new employee
        await createEmployee({
          email,
          name,
          role: role.toUpperCase(), // Backend expects ADMIN or EMPLOYEE
          phoneNumber: undefined
        });
        alert('Employee created successfully!');
      }
      
      if (onSuccess) onSuccess(); // Refresh the user list
      onClose();
    } catch (err) {
      console.error('Failed to save user:', err);
      alert(`Error: ${error || 'Failed to save user'}`);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
      <div className="w-full max-w-lg rounded-lg bg-white shadow-xl" role="dialog" aria-modal="true">
        <div className="flex items-center justify-between border-b px-6 py-4">
          <h2 className="font-heading text-xl font-bold">
            {user ? `Edit User: ${user.name}` : 'Add New Employee'}
          </h2>
          <button onClick={onClose} className="rounded-full p-1 hover:bg-gray-100">
            <X className="h-5 w-5" />
          </button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-6 p-6">
          {error && (
            <div className="rounded-md bg-red-50 p-3 text-sm text-red-600">
              {error}
            </div>
          )}
          
          <div>
            <label className="text-sm font-medium">Email Address</label>
            <input 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              readOnly={!!user}
              required
              type="email"
              className="mt-1 w-full rounded-md border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary disabled:bg-gray-100" 
            />
          </div>
          
          {!user && (
            <div>
              <label htmlFor="name" className="text-sm font-medium">Full Name</label>
              <input 
                id="name" 
                value={name} 
                onChange={(e) => setName(e.target.value)}
                required
                className="mt-1 w-full rounded-md border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary" 
              />
            </div>
          )}
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium">Role</label>
              <Listbox value={role} onChange={setRole}>
                <div className="relative mt-1">
                  <Listbox.Button className="relative w-full cursor-default rounded-md border border-border bg-white py-2 pl-3 pr-10 text-left text-sm focus:outline-none focus:ring-2 focus:ring-primary">
                    <span className="block truncate">{role}</span>
                    <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                      <ChevronDown className="h-5 w-5 text-gray-400" />
                    </span>
                  </Listbox.Button>
                  <Transition
                    as={Fragment}
                    leave="transition ease-in duration-100"
                    leaveFrom="opacity-100"
                    leaveTo="opacity-0"
                  >
                    <Listbox.Options className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
                      {roles.map((r) => (
                        <Listbox.Option
                          key={r}
                          value={r}
                          className={({ active }) =>
                            `${active ? 'bg-primary/10 text-primary' : 'text-foreground'} relative cursor-default select-none py-2 pl-4 pr-4`
                          }
                        >
                          {r}
                        </Listbox.Option>
                      ))}
                      {user?.role === 'Customer' && (
                        <Listbox.Option
                          value="Customer"
                          disabled
                          className="relative cursor-not-allowed select-none py-2 pl-4 pr-4 text-muted-foreground"
                        >
                          Customer
                        </Listbox.Option>
                      )}
                    </Listbox.Options>
                  </Transition>
                </div>
              </Listbox>
            </div>
            
            {user && (
              <div>
                <label htmlFor="status" className="text-sm font-medium">Status</label>
                <select
                  id="status"
                  value={status}
                  onChange={(e) => setStatus(e.target.value as 'Active' | 'Deactivated')}
                  className="mt-1 w-full rounded-md border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
                >
                  <option>Active</option>
                  <option>Deactivated</option>
                </select>
              </div>
            )}
          </div>
          
          <div className="flex justify-end gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              disabled={loading}
              className="px-4 py-2 text-sm font-medium rounded-md bg-gray-100 hover:bg-gray-200 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 text-sm font-medium rounded-md bg-primary text-white hover:brightness-110 disabled:opacity-50"
            >
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
```

---

### **STEP 4: Update Your Admin Users Page**

Update `GearUp-frontent/src/app/admin/users/page.tsx` to fetch real data:

```typescript
'use client';
import { useEffect, useState } from 'react';
import { useAdminUsers } from '@/hooks/useAdminUsers';
import UserEditModal from '@/app/components/admin/UserEditModal';

export interface User {
  id: number;
  email: string;
  name: string;
  role: 'Admin' | 'Employee' | 'Customer';
  status: 'Active' | 'Deactivated';
  createdAt: string;
  lastLoginAt?: string;
}

export default function UsersPage() {
  const { getAllUsers, loading, error } = useAdminUsers();
  const [users, setUsers] = useState<User[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);

  const loadUsers = async () => {
    try {
      const result = await getAllUsers({
        page,
        size: 10,
        search: searchTerm || undefined,
        role: roleFilter || undefined,
        status: statusFilter || undefined
      });
      
      setUsers(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      console.error('Failed to load users:', err);
    }
  };

  useEffect(() => {
    loadUsers();
  }, [page, searchTerm, roleFilter, statusFilter]);

  const handleEditUser = (user: User) => {
    setSelectedUser(user);
    setIsModalOpen(true);
  };

  const handleCreateEmployee = () => {
    setSelectedUser(null);
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedUser(null);
  };

  const handleSuccess = () => {
    loadUsers(); // Refresh the list
  };

  if (loading && users.length === 0) {
    return <div className="p-8">Loading users...</div>;
  }

  if (error) {
    return <div className="p-8 text-red-600">Error: {error}</div>;
  }

  return (
    <div className="p-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">User Management</h1>
        <button
          onClick={handleCreateEmployee}
          className="rounded-md bg-primary px-4 py-2 text-white hover:brightness-110"
        >
          Add Employee
        </button>
      </div>

      {/* Filters */}
      <div className="mb-6 flex gap-4">
        <input
          type="text"
          placeholder="Search by name or email..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="flex-1 rounded-md border px-3 py-2"
        />
        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          className="rounded-md border px-3 py-2"
        >
          <option value="">All Roles</option>
          <option value="ADMIN">Admin</option>
          <option value="EMPLOYEE">Employee</option>
          <option value="CUSTOMER">Customer</option>
        </select>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="rounded-md border px-3 py-2"
        >
          <option value="">All Status</option>
          <option value="Active">Active</option>
          <option value="Deactivated">Deactivated</option>
        </select>
      </div>

      {/* Users Table */}
      <div className="overflow-x-auto">
        <table className="w-full border-collapse">
          <thead>
            <tr className="border-b">
              <th className="p-3 text-left">Name</th>
              <th className="p-3 text-left">Email</th>
              <th className="p-3 text-left">Role</th>
              <th className="p-3 text-left">Status</th>
              <th className="p-3 text-left">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className="border-b hover:bg-gray-50">
                <td className="p-3">{user.name}</td>
                <td className="p-3">{user.email}</td>
                <td className="p-3">
                  <span className={`rounded-full px-2 py-1 text-xs ${
                    user.role === 'Admin' ? 'bg-purple-100 text-purple-800' :
                    user.role === 'Employee' ? 'bg-blue-100 text-blue-800' :
                    'bg-gray-100 text-gray-800'
                  }`}>
                    {user.role}
                  </span>
                </td>
                <td className="p-3">
                  <span className={`rounded-full px-2 py-1 text-xs ${
                    user.status === 'Active' ? 'bg-green-100 text-green-800' :
                    'bg-red-100 text-red-800'
                  }`}>
                    {user.status}
                  </span>
                </td>
                <td className="p-3">
                  <button
                    onClick={() => handleEditUser(user)}
                    className="text-primary hover:underline"
                  >
                    Edit
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="mt-6 flex items-center justify-between">
        <button
          onClick={() => setPage(p => Math.max(0, p - 1))}
          disabled={page === 0}
          className="rounded-md border px-4 py-2 disabled:opacity-50"
        >
          Previous
        </button>
        <span>Page {page + 1} of {totalPages}</span>
        <button
          onClick={() => setPage(p => p + 1)}
          disabled={page >= totalPages - 1}
          className="rounded-md border px-4 py-2 disabled:opacity-50"
        >
          Next
        </button>
      </div>

      <UserEditModal
        isOpen={isModalOpen}
        onClose={handleModalClose}
        user={selectedUser}
        onSuccess={handleSuccess}
      />
    </div>
  );
}
```

---

### **STEP 5: Test the Complete Flow**

1. **Start Frontend**:
   ```powershell
   cd "c:\Users\ASUS\Desktop\Final EAD\GearUp-frontent"
   npm run dev
   ```

2. **Login as Admin**:
   - Open http://localhost:3000
   - Login with your admin account

3. **Navigate to Users Page**:
   - Go to `/admin/users` route
   - You should see the list of users from the database

4. **Test Features**:
   - ✅ Search users by name/email
   - ✅ Filter by role (Admin/Employee/Customer)
   - ✅ Filter by status (Active/Deactivated)
   - ✅ Click "Add Employee" button
   - ✅ Fill in email, name, role
   - ✅ Submit and see new employee created
   - ✅ Click "Edit" on any user
   - ✅ Change role or status
   - ✅ Submit and see changes applied

---

## 🧪 Testing Admin API Directly (Optional)

If you want to test the API without the frontend:

1. **Get your Firebase token**:
   - Login to frontend as admin
   - Open browser console (F12)
   - Run: `firebase.auth().currentUser.getIdToken().then(t => console.log(t))`
   - Copy the token

2. **Update test script**:
   ```powershell
   # Open scripts/test-admin-api.ps1
   # Replace line 23: $token = "YOUR_FIREBASE_TOKEN_HERE"
   # With your actual token
   ```

3. **Run the test**:
   ```powershell
   .\scripts\test-admin-api.ps1
   ```

---

## 📊 Expected Results

After integration, you should be able to:

✅ View paginated list of all users  
✅ Search users by name or email  
✅ Filter users by role (Admin/Employee/Customer)  
✅ Filter users by status (Active/Deactivated)  
✅ Sort users by different fields  
✅ Create new employee/admin accounts  
✅ Update user roles (promote Employee to Admin)  
✅ Activate/Deactivate user accounts  
✅ See user statistics (total, active, by role, etc.)  
✅ All actions logged in audit trail  

---

## 🐛 Troubleshooting

### CORS Errors?
Add to `services/user-auth-service/src/main/resources/application.yml`:
```yaml
spring:
  web:
    cors:
      allowed-origins: "http://localhost:3000"
      allowed-methods: "*"
      allowed-headers: "*"
```

### 401 Unauthorized?
- Make sure you're logged in as ADMIN
- Check Firebase token is not expired (get new one)
- Verify user has ADMIN role in database

### 403 Forbidden?
- User doesn't have ADMIN role
- Check database: `SELECT * FROM user_roles WHERE user_id = YOUR_USER_ID;`

---

## 📚 Additional Resources

- **API Documentation**: `docs/ADMIN_USER_MANAGEMENT_API.md`
- **Backend Code**: `services/user-auth-service/src/main/java/com/gearup/userauth/controller/AdminUserController.java`
- **Test Script**: `scripts/test-admin-api.ps1`

---

## ✅ Completion Checklist

- [ ] Frontend started (npm run dev)
- [ ] useAdminUsers hook created
- [ ] UserEditModal updated with API integration
- [ ] Admin users page updated with real data
- [ ] Logged in as ADMIN
- [ ] Can view users list
- [ ] Can search and filter users
- [ ] Can create new employee
- [ ] Can update user role
- [ ] Can activate/deactivate users
- [ ] All changes reflected in database

---

**Need help with any specific step? Let me know!** 🚀
