# 🎯 Quick Start: View Your Payment Tables in pgAdmin

## ⚡ Super Quick Setup (2 minutes)

### Step 1: Login to pgAdmin
**URL**: http://localhost:5050  
✅ Already opened in your browser!

**Login**:
- Email: `admin@gearup.com`
- Password: `admin123`

---

### Step 2: Add Server Connection

1. Click **"Add New Server"** button (or right-click "Servers" → Create → Server)

2. **General Tab**:
   - Name: `GearUp Payment Service`

3. **Connection Tab**:
   - Host: `db`
   - Port: `5432`
   - Maintenance database: `postgres`
   - Username: `as_payment_user`
   - Password: `payment_pass_123`
   - ✅ Check "Save password"

4. Click **Save**

---

### Step 3: Browse Your Tables

Navigate in the left sidebar:

```
Servers
  └── GearUp Payment Service
      └── Databases
          └── as_payment_service  👈 Your database
              └── Schemas
                  └── public
                      └── Tables  👈 Click here!
```

**Your Tables:**
- 📋 `customer_bills` - All generated bills
- 💳 `payment_requests` - Payment requests  
- 🛠️ `payment_request_services` - Service items
- ⭐ `customer_reviews` - Customer reviews
- 📊 `flyway_schema_history` - Migration tracking

---

## 🔍 Quick Data Checks

### Right-click any table → "View/Edit Data" → "All Rows"

Or use **Query Tool** (Tools → Query Tool):

```sql
-- See all payment requests
SELECT * FROM payment_requests;

-- See all bills
SELECT * FROM customer_bills;

-- See published reviews
SELECT * FROM customer_reviews WHERE status = 'PUBLISHED';
```

---

## 📊 Your Mock Data

**Payment Requests**: 8 total
- 5 PENDING
- 2 APPROVED  
- 1 REJECTED

**Customer Bills**: 2 total
- Emily Brown: $275.00 (PAID ✅)
- Jane Smith: $110.00 (UNPAID ⏳)

**Reviews**: 2 total
- Emily Brown: 5 stars (PUBLISHED ✅)
- Jane Smith: 4 stars (PENDING ⏳)

---

## 🎉 You're All Set!

Now you can:
- ✅ View all database tables
- ✅ Check mock data
- ✅ Watch real-time updates when you test APIs
- ✅ Run custom SQL queries
- ✅ Export data if needed

**Keep pgAdmin open** while developing - you'll see changes happen live! 🔥
