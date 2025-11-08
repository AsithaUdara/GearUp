# 🗄️ pgAdmin Setup Guide for Payment Service

## Quick Access

**pgAdmin Web Interface**: http://localhost:5050

**Login Credentials**:
- Email: `admin@gearup.com`
- Password: `admin123`

---

## 🚀 Step-by-Step Setup

### 1. Start Docker Services
```powershell
cd C:\Users\ASUS\Desktop\EAD\gearup-backend\GearUp-backend\deployment\docker
docker-compose up -d
```

### 2. Access pgAdmin
Open your browser and go to: **http://localhost:5050**

### 3. Add Database Server Connection

#### Click "Add New Server"

**General Tab:**
- Name: `GearUp Payment Service`

**Connection Tab:**
- Host name/address: `db` (Docker internal hostname)
- Port: `5432`
- Maintenance database: `postgres`
- Username: `as_payment_user`
- Password: `payment_pass_123` (check your .env file for actual password)
- ✅ Save password

Click **Save**

---

## 📊 View Payment Service Tables

After connecting, expand the tree:

```
Servers
  └── GearUp Payment Service
      └── Databases
          └── as_payment_service
              └── Schemas
                  └── public
                      └── Tables
```

**Your Tables:**
1. ✅ `payment_requests` - All payment requests
2. ✅ `payment_request_services` - Service items
3. ✅ `customer_bills` - Generated bills
4. ✅ `customer_reviews` - Customer reviews
5. ✅ `flyway_schema_history` - Migration tracking

---

## 🔍 Quick Queries to Run

### See All Payment Requests
```sql
SELECT * FROM payment_requests ORDER BY submitted_date DESC;
```

### See All Bills
```sql
SELECT 
    cb.id,
    cb.customer_name,
    cb.final_amount,
    cb.payment_status,
    cb.review_submitted,
    cb.approved_date
FROM customer_bills cb
ORDER BY cb.approved_date DESC;
```

### See Published Reviews
```sql
SELECT 
    cr.customer_name,
    cr.service_name,
    cr.rating,
    cr.review_text,
    cr.status,
    cr.submitted_date
FROM customer_reviews cr
WHERE cr.status = 'PUBLISHED'
ORDER BY cr.rating DESC;
```

### Payment Statistics
```sql
SELECT 
    COUNT(*) FILTER (WHERE status = 'PENDING') as pending_count,
    COUNT(*) FILTER (WHERE status = 'APPROVED') as approved_count,
    COUNT(*) FILTER (WHERE status = 'REJECTED') as rejected_count,
    SUM(total_amount) FILTER (WHERE status = 'APPROVED') as total_revenue
FROM payment_requests;
```

---

## 🐛 Troubleshooting

### Can't Connect to Database?
1. Make sure Docker containers are running: `docker-compose ps`
2. Check if `gearup-postgres` container is healthy
3. Verify .env file has correct credentials

### Wrong Password?
Check your `.env` file for:
```
PAYMENT_DB_USER=as_payment_user
PAYMENT_DB_PASSWORD=payment_pass_123
```

### Can't Access pgAdmin?
1. Check if port 5050 is available
2. Restart pgAdmin container: `docker-compose restart pgadmin`
3. Check logs: `docker logs gearup-pgadmin`

---

## 📝 Your Mock Data

**8 Payment Requests:**
- 5 PENDING
- 2 APPROVED
- 1 REJECTED

**2 Customer Bills:**
- Emily Brown: $275.00 (PAID, review submitted)
- Jane Smith: $110.00 (UNPAID, no review)

**2 Customer Reviews:**
- Emily Brown: 5-star (PUBLISHED)
- Jane Smith: 4-star (PENDING)

---

## 🎯 Next Steps

1. ✅ Access pgAdmin at http://localhost:5050
2. ✅ Add server connection
3. ✅ Browse payment_requests, customer_bills, customer_reviews tables
4. ✅ Run queries to verify mock data
5. ✅ Test APIs in Postman while watching database changes in pgAdmin
6. 🔜 Connect frontend to APIs

**Pro Tip**: Keep pgAdmin open in one tab and your frontend in another to see real-time database updates!
