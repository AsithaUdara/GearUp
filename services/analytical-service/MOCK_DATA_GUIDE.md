# How to Add Mock Data & Test in Frontend

## Overview
You now have **POST endpoints** to add mock data dynamically through your frontend!

---

## 🔥 Available POST Endpoints

### 1️⃣ Add a New Activity (Recent Activity)
**POST** `http://localhost:8087/api/analytics/activities`

**Body (JSON):**
```json
{
  "eventDescription": "New appointment booked: Tesla Model 3",
  "status": "OK",
  "relatedEntityType": "APPOINTMENT",
  "relatedEntityId": "APT-2024-100"
}
```

**Status Options:** `OK`, `ATTENTION`, `WARNING`

---

### 2️⃣ Add a Popular Service
**POST** `http://localhost:8087/api/analytics/services/popular?serviceName=Engine Repair&bookingCount=45&percentage=13.2&rank=6`

**Query Parameters:**
- `serviceName` (required) - Name of the service
- `bookingCount` (required) - Number of bookings
- `percentage` (optional) - Percentage of total
- `rank` (optional) - Ranking position

---

### 3️⃣ Add Service Analytics
**POST** `http://localhost:8087/api/analytics/services`

**Body (JSON):**
```json
{
  "serviceName": "Engine Repair",
  "serviceType": "REPAIR",
  "appointmentCount": 25,
  "totalRevenue": 3500.00,
  "averageRating": 4.8
}
```

---

### 4️⃣ Reset All Data
**POST** `http://localhost:8087/api/analytics/reset`

Clears all analytics data (useful for testing fresh state).

---

### 5️⃣ Delete Specific Items
**DELETE** `http://localhost:8087/api/analytics/activities/{id}`
**DELETE** `http://localhost:8087/api/analytics/services/popular/{id}`
**DELETE** `http://localhost:8087/api/analytics/services/{id}`

---

## 🎨 Frontend Integration Examples

### React/Next.js Example

```typescript
// Add a new activity
const addActivity = async () => {
  const response = await fetch('http://localhost:8087/api/analytics/activities', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      eventDescription: 'New appointment booked: Tesla Model 3',
      status: 'OK',
      relatedEntityType: 'APPOINTMENT',
      relatedEntityId: 'APT-2024-100'
    })
  });
  
  const newActivity = await response.json();
  console.log('Activity added:', newActivity);
  
  // Refresh the dashboard
  refreshDashboard();
};

// Add a popular service
const addPopularService = async () => {
  const response = await fetch(
    'http://localhost:8087/api/analytics/services/popular?' +
    'serviceName=Engine Repair&bookingCount=45&percentage=13.2&rank=6',
    { method: 'POST' }
  );
  
  const service = await response.json();
  console.log('Service added:', service);
};

// Refresh dashboard data
const refreshDashboard = async () => {
  const response = await fetch('http://localhost:8087/api/analytics/dashboard');
  const data = await response.json();
  setDashboardData(data);
};
```

---

## 🧪 Testing with cURL (Windows PowerShell)

### Add a Recent Activity
```powershell
$body = @{
    eventDescription = "Emergency repair: BMW X5"
    status = "WARNING"
    relatedEntityType = "SERVICE"
    relatedEntityId = "SVC-2024-999"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:8087/api/analytics/activities `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
```

### Add a Popular Service
```powershell
Invoke-RestMethod -Uri "http://localhost:8087/api/analytics/services/popular?serviceName=Transmission Service&bookingCount=38&percentage=11.1&rank=7" `
    -Method POST
```

### Reset All Data
```powershell
Invoke-RestMethod -Uri http://localhost:8087/api/analytics/reset -Method POST
```

---

## 🔍 Testing Workflow

### Step 1: Clear existing data (optional)
```powershell
Invoke-RestMethod -Uri http://localhost:8087/api/analytics/reset -Method POST
```

### Step 2: Add some test activities
```powershell
$activities = @(
    @{
        eventDescription = "Appointment confirmed: Audi A6"
        status = "OK"
        relatedEntityType = "APPOINTMENT"
        relatedEntityId = "APT-001"
    },
    @{
        eventDescription = "Low inventory: Spark Plugs"
        status = "ATTENTION"
        relatedEntityType = "INVENTORY"
        relatedEntityId = "INV-123"
    },
    @{
        eventDescription = "Emergency service required!"
        status = "WARNING"
        relatedEntityType = "SERVICE"
        relatedEntityId = "SVC-EMRG"
    }
)

foreach ($activity in $activities) {
    $body = $activity | ConvertTo-Json
    Invoke-RestMethod -Uri http://localhost:8087/api/analytics/activities `
        -Method POST `
        -Body $body `
        -ContentType "application/json"
    Start-Sleep -Seconds 1
}
```

### Step 3: Check in frontend
Open your browser and navigate to your analytics page. You should see the new activities appear in the "Recent Activity" section!

### Step 4: Verify with GET endpoint
```powershell
Invoke-RestMethod -Uri http://localhost:8087/api/analytics/activities/recent?limit=10 | ConvertTo-Json -Depth 5
```

---

## 🎯 Quick Test Script

Save this as `test-analytics.ps1`:

```powershell
# Test Analytics API

Write-Host "🧪 Testing Analytics API..." -ForegroundColor Cyan

# 1. Add a new activity
Write-Host "`n➕ Adding new activity..." -ForegroundColor Yellow
$activity = @{
    eventDescription = "Test: New customer registered - $(Get-Date -Format 'HH:mm:ss')"
    status = "OK"
    relatedEntityType = "CUSTOMER"
    relatedEntityId = "CUST-TEST-$(Get-Random -Maximum 999)"
} | ConvertTo-Json

$result = Invoke-RestMethod -Uri http://localhost:8087/api/analytics/activities `
    -Method POST `
    -Body $activity `
    -ContentType "application/json"

Write-Host "✅ Activity added: $($result.eventDescription)" -ForegroundColor Green

# 2. Add a popular service
Write-Host "`n➕ Adding popular service..." -ForegroundColor Yellow
$service = Invoke-RestMethod -Uri "http://localhost:8087/api/analytics/services/popular?serviceName=Wheel Alignment&bookingCount=55&percentage=16.1&rank=6" `
    -Method POST

Write-Host "✅ Service added: $($service.serviceName) - $($service.bookingCount) bookings" -ForegroundColor Green

# 3. Get recent activities
Write-Host "`n📊 Fetching recent activities..." -ForegroundColor Yellow
$activities = Invoke-RestMethod -Uri http://localhost:8087/api/analytics/activities/recent?limit=5

Write-Host "✅ Recent Activities:" -ForegroundColor Green
foreach ($act in $activities) {
    $color = switch ($act.status) {
        "OK" { "Green" }
        "ATTENTION" { "Yellow" }
        "WARNING" { "Red" }
        default { "White" }
    }
    Write-Host "  [$($act.timeAgo)] $($act.event)" -ForegroundColor $color
}

# 4. Get top services
Write-Host "`n🏆 Top Services:" -ForegroundColor Yellow
$topServices = Invoke-RestMethod -Uri http://localhost:8087/api/analytics/services/top?limit=10

foreach ($svc in $topServices) {
    Write-Host "  $($svc.serviceName): $($svc.count) bookings ($($svc.percentage)%)" -ForegroundColor Cyan
}

Write-Host "`n✨ Test complete! Check your frontend now!" -ForegroundColor Green
```

Run it:
```powershell
.\test-analytics.ps1
```

---

## 📱 Frontend Component Example (React)

```typescript
import { useState, useEffect } from 'react';

const API_BASE = 'http://localhost:8087/api/analytics';

export default function AnalyticsPage() {
  const [dashboard, setDashboard] = useState(null);
  const [newActivity, setNewActivity] = useState({
    eventDescription: '',
    status: 'OK',
    relatedEntityType: 'APPOINTMENT',
    relatedEntityId: ''
  });

  // Load dashboard data
  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    const res = await fetch(`${API_BASE}/dashboard`);
    const data = await res.json();
    setDashboard(data);
  };

  // Add new activity
  const handleAddActivity = async (e) => {
    e.preventDefault();
    
    await fetch(`${API_BASE}/activities`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newActivity)
    });
    
    // Reload dashboard
    await loadDashboard();
    
    // Reset form
    setNewActivity({
      eventDescription: '',
      status: 'OK',
      relatedEntityType: 'APPOINTMENT',
      relatedEntityId: ''
    });
  };

  return (
    <div>
      <h1>Analytics Dashboard</h1>
      
      {/* Add Activity Form */}
      <form onSubmit={handleAddActivity}>
        <input
          value={newActivity.eventDescription}
          onChange={e => setNewActivity({...newActivity, eventDescription: e.target.value})}
          placeholder="Event description"
        />
        <select
          value={newActivity.status}
          onChange={e => setNewActivity({...newActivity, status: e.target.value})}
        >
          <option value="OK">OK</option>
          <option value="ATTENTION">Attention</option>
          <option value="WARNING">Warning</option>
        </select>
        <button type="submit">Add Activity</button>
      </form>

      {/* Display Dashboard */}
      {dashboard && (
        <>
          <h2>Metrics</h2>
          <p>Appointments: {dashboard.metrics.appointments}</p>
          <p>New Customers: {dashboard.metrics.newCustomers}</p>
          
          <h2>Recent Activities</h2>
          {dashboard.recentActivities.map((activity, i) => (
            <div key={i} className={`activity-${activity.status.toLowerCase()}`}>
              <span>{activity.timeAgo}</span>
              <span>{activity.event}</span>
            </div>
          ))}
          
          <h2>Top Services</h2>
          {dashboard.topServices.map((service, i) => (
            <div key={i}>
              {service.serviceName}: {service.count} bookings
            </div>
          ))}
        </>
      )}
    </div>
  );
}
```

---

## 🎬 Complete Workflow

1. **Open your frontend analytics page**
2. **Use the API to add data:**
   - Add activities via POST
   - Add services via POST
   - Add analytics via POST
3. **Refresh your frontend** (or set up auto-refresh)
4. **See new data appear immediately!**

---

## 💡 Pro Tips

1. **Auto-refresh**: Set up polling every 5-10 seconds to see new data in real-time
   ```typescript
   useEffect(() => {
     const interval = setInterval(loadDashboard, 5000);
     return () => clearInterval(interval);
   }, []);
   ```

2. **Toast notifications**: Show a success message when data is added
3. **Form validation**: Ensure required fields are filled
4. **Loading states**: Show spinners while fetching/posting data

---

Your analytics service is now **fully interactive**! 🚀
