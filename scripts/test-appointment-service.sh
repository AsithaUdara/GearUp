#!/usr/bin/env bash

# ========================================
# GearUp Backend - Appointment Service Test
# ========================================
# This script tests the appointment service endpoints

set -e

# Configuration
BASE_URL="http://localhost:8084"
SERVICE_ID=1
USER_ID="test_user_123"
DATE=$(date -d "tomorrow" +%Y-%m-%d)

echo "🔍 Testing Appointment Service at $BASE_URL"
echo "====================================="

# Test 1: Get all services
echo "1. Testing GET /api/services"
curl -s -X GET "$BASE_URL/api/services" | jq '.' || echo "❌ Services endpoint failed"

# Test 2: Get time slots
echo -e "\n2. Testing GET /api/timeslots?serviceId=$SERVICE_ID&date=$DATE"
curl -s -X GET "$BASE_URL/api/timeslots?serviceId=$SERVICE_ID&date=$DATE" | jq '.' || echo "❌ Timeslots endpoint failed"

# Test 3: Create a booking
echo -e "\n3. Testing POST /api/bookings"
BOOKING_RESPONSE=$(curl -s -X POST "$BASE_URL/api/bookings" \
  -H "Content-Type: application/json" \
  -d "{
    \"serviceId\": $SERVICE_ID,
    \"timeSlotId\": 1,
    \"userId\": \"$USER_ID\",
    \"customerName\": \"John Doe\",
    \"customerEmail\": \"john.doe@example.com\",
    \"customerPhone\": \"+1234567890\",
    \"notes\": \"Test booking\"
  }")

echo "$BOOKING_RESPONSE" | jq '.' || echo "❌ Booking creation failed"

# Extract booking ID for further tests
BOOKING_ID=$(echo "$BOOKING_RESPONSE" | jq -r '.id' 2>/dev/null || echo "")

if [ "$BOOKING_ID" != "" ] && [ "$BOOKING_ID" != "null" ]; then
  # Test 4: Get user bookings
  echo -e "\n4. Testing GET /api/bookings?userId=$USER_ID"
  curl -s -X GET "$BASE_URL/api/bookings?userId=$USER_ID" | jq '.' || echo "❌ Get user bookings failed"
  
  # Test 5: Update booking
  echo -e "\n5. Testing PUT /api/bookings/$BOOKING_ID"
  curl -s -X PUT "$BASE_URL/api/bookings/$BOOKING_ID" \
    -H "Content-Type: application/json" \
    -d "{
      \"customerName\": \"John Smith\",
      \"notes\": \"Updated test booking\"
    }" | jq '.' || echo "❌ Booking update failed"
  
  # Test 6: Cancel booking
  echo -e "\n6. Testing DELETE /api/bookings/$BOOKING_ID"
  curl -s -X DELETE "$BASE_URL/api/bookings/$BOOKING_ID" || echo "❌ Booking cancellation failed"
else
  echo "⚠️  Skipping booking tests - booking creation failed"
fi

echo -e "\n✅ Appointment service test completed!"