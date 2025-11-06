# PostgreSQL Database - Visual Access Guide (Like MongoDB Atlas)

## 🎯 Quick Access to Your Database (Web Interface)

### Step 1: Open PgAdmin Web Interface
**URL:** http://localhost:5050

**Login Credentials:**
- Email: `admin@gearup.com`
- Password: `admin123`

---

## 📋 Step-by-Step: Connect to Tracking Service Database

### Step 2: Add Server Connection

1. **Right-click on "Servers"** in the left panel
2. Click **"Register" → "Server"**

3. **General Tab:**
   - Name: `GearUp PostgreSQL` (or any name you like)

4. **Connection Tab:**
   - Host name/address: `gearup-postgres` (or `localhost` if connecting from host)
   - Port: `5432`
   - Maintenance database: `postgres`
   - Username: `postgres`
   - Password: Check your `.env` file for `POSTGRES_PASSWORD` (or try `postgres`)
   - ✅ Check "Save password"

5. Click **"Save"**

---

### Step 3: Access Tracking Service Database

1. **Expand** the server you just created
2. **Expand** "Databases"
3. **Find and expand** `as_tracking_service`
4. **Expand** "Schemas" → "public" → "Tables"

### Step 4: View Tables and Data

You'll see these tables:
- ✅ `work_task` - Employee work tasks
- ✅ `modification_request` - Customer modification requests  
- ✅ `parts_request` - Parts/material requests
- ✅ `service_progress` - Service progress tracking
- ✅ `flyway_schema_history` - Migration history (system table)

**To view data:**
1. **Right-click** on any table (e.g., `work_task`)
2. Click **"View/Edit Data" → "All Rows"**
3. You'll see all the data in a table format!

---

## 🔍 Quick Database Info

### Tracking Service Database:
- **Database Name:** `as_tracking_service`
- **User:** `svc_tracking_service`
- **Password:** `tracking_svc_pass_2024`

### Other Databases Available:
- `as_automobile_service`
- `as_notification_service`
- `as_user_auth_service`
- `as_template_service`

---

## 💡 PgAdmin Features (Similar to MongoDB Atlas)

### ✅ What You Can Do:
1. **Browse Tables** - See all tables visually
2. **View Data** - See all rows in table format
3. **Run Queries** - Execute SQL queries
4. **Edit Data** - Modify records directly
5. **Create Tables** - Design new tables visually
6. **View Relationships** - See foreign keys and relationships
7. **Export Data** - Export to CSV, JSON, etc.

### 📊 View Data:
- Right-click table → "View/Edit Data" → "All Rows"
- Or use Query Tool: Right-click database → "Query Tool"

### 🔍 Run SQL Queries:
1. Right-click on `as_tracking_service` database
2. Click **"Query Tool"**
3. Type SQL: `SELECT * FROM work_task;`
4. Click **Execute** (▶️ button)

---

## 🆚 Comparison: MongoDB Atlas vs PostgreSQL + PgAdmin

| Feature | MongoDB Atlas | PostgreSQL + PgAdmin |
|---------|---------------|---------------------|
| Web Interface | ✅ Yes | ✅ Yes (PgAdmin) |
| View Collections/Tables | ✅ Yes | ✅ Yes |
| View Documents/Rows | ✅ Yes | ✅ Yes |
| Run Queries | ✅ Yes | ✅ Yes (SQL) |
| Visual Database Browser | ✅ Yes | ✅ Yes |
| Create Databases | ✅ Yes | ✅ Yes |
| Create Tables/Collections | ✅ Yes | ✅ Yes |

**PgAdmin is the PostgreSQL equivalent of MongoDB Atlas web interface!**

---

## 🚀 Quick Start Commands

### Open PgAdmin:
```powershell
Start-Process "http://localhost:5050"
```

### View Database via Command Line (Alternative):
```powershell
docker exec -it gearup-postgres psql -U svc_tracking_service -d as_tracking_service
```

---

## 📝 Example: View All Tasks

In PgAdmin Query Tool, run:
```sql
SELECT * FROM work_task ORDER BY created_at DESC;
```

This shows all tasks, newest first!

---

## 🎓 Tips for Beginners

1. **Start with PgAdmin** - It's the easiest way to explore
2. **Use Query Tool** - Learn SQL by running queries
3. **View Data First** - Right-click tables to see what's inside
4. **Don't Delete** - Be careful with DELETE commands
5. **Use SELECT** - Safe to run SELECT queries anytime

---

## 🔗 Access URLs

- **PgAdmin Web UI:** http://localhost:5050
- **PostgreSQL Direct:** localhost:5432
- **Database Name:** as_tracking_service

---

**That's it! PgAdmin is your MongoDB Atlas equivalent for PostgreSQL! 🎉**

