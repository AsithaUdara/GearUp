#!/usr/bin/env bash
# API Gateway event tests (bash)
# Usage: bash ./scripts/api-gateway-event-tests.sh
# Requirements: curl, jq

BASE_URL=${BASE_URL:-http://localhost:9090}
AUTH_HEADER=${AUTH_HEADER:-}

set -euo pipefail

function call() {
  METHOD=$1; PATH=$2; DATA=${3:-}
  URL="$BASE_URL$PATH"
  echo "==> $METHOD $URL"
  if [ -n "$DATA" ]; then
    if [ -n "$AUTH_HEADER" ]; then
      curl -s -X $METHOD -H "Authorization: $AUTH_HEADER" -H "Content-Type: application/json" -d "$DATA" "$URL" | jq || true
    else
      curl -s -X $METHOD -H "Content-Type: application/json" -d "$DATA" "$URL" | jq || true
    fi
  else
    if [ -n "$AUTH_HEADER" ]; then
      curl -s -X $METHOD -H "Authorization: $AUTH_HEADER" "$URL" | jq || true
    else
      curl -s -X $METHOD "$URL" | jq || true
    fi
  fi
}

# 1) Create booking (capture id)
CREATE_BOOKING_PAYLOAD='{"serviceId":1,"timeSlotId":1,"userId":"cust-123","customerName":"Jane Customer","customerEmail":"jane@example.com","customerPhone":"555-0100","notes":"Test booking"}'
resp=$(curl -s -X POST -H "Content-Type: application/json" ${AUTH_HEADER:+-H "Authorization: $AUTH_HEADER"} -d "$CREATE_BOOKING_PAYLOAD" "$BASE_URL/api/v1/bookings")
echo ">>> Booking create response:"; echo "$resp" | jq -r '.' || true
BOOKING_ID=$(echo "$resp" | jq -r '.id // .bookingId // empty')
if [ -z "$BOOKING_ID" ]; then
  echo "Failed to extract booking id from response. Response: $resp"
else
  echo "Extracted booking id: $BOOKING_ID"
  # Approve booking
  call PUT "/api/v1/bookings/$BOOKING_ID/approve"
  # Assign employee (triggers EmployeeAssignedToAppointmentEvent)
  call PUT "/api/v1/bookings/$BOOKING_ID/assign?employeeId=101&timeSlot=09:00-10:00"
fi

# 2) Payment request (capture id)
PAYMENT_PAYLOAD='{"customerName":"Jane Customer","customerEmail":"jane@example.com","vehicleInfo":"Toyota Corolla","services":[{"code":"SVC-1","description":"Oil change","price":49.99}],"submittedBy":"tests","submittedDate":"2025-11-16"}'
resp=$(curl -s -X POST -H "Content-Type: application/json" ${AUTH_HEADER:+-H "Authorization: $AUTH_HEADER"} -d "$PAYMENT_PAYLOAD" "$BASE_URL/api/v1/payments/admin/requests")
echo ">>> Payment create response:"; echo "$resp" | jq -r '.' || true
PAYMENT_ID=$(echo "$resp" | jq -r '.id // empty')
if [ -n "$PAYMENT_ID" ]; then
  echo "Extracted payment id: $PAYMENT_ID"
  # Optionally mark-paid or approve using the returned id
  # call PUT "/api/v1/payments/admin/requests/$PAYMENT_ID/approve"
fi

# 3) Customer create & KYC
CUSTOMER_PAYLOAD='{"firebaseUid":"cust-123","email":"jane@example.com","displayName":"Jane Customer","phone":"555-0100"}'
call POST "/api/v1/customers" "$CUSTOMER_PAYLOAD"
call PATCH "/api/v1/customers/cust-123/kyc?status=VERIFIED" ""

# 4) User register (example)
USER_PAYLOAD='{"email":"newuser@example.com","password":"P@ssw0rd","name":"New User"}'
call POST "/api/v1/auth/register" "$USER_PAYLOAD"
# assign role (example)
call PUT "/api/v1/admin/users/assign-role?userId=newuser@example.com&role=TECHNICIAN" ""

# 5) Vehicle register
VEHICLE_PAYLOAD='{"customerId":"cust-123","make":"Toyota","model":"Corolla","year":2021}'
call POST "/api/v1/vehicles" "$VEHICLE_PAYLOAD"
call PUT "/api/v1/vehicles/1" '{"updatedField":"color","value":"Blue"}'

# 6) Modification request
MOD_PAYLOAD='{"customerId":"cust-123","serviceId":1,"description":"Add sunroof","estimatedCost":250.0}'
call POST "/api/v1/service-modifications/1/requests" "$MOD_PAYLOAD"

# 7) Parts request
PARTS_PAYLOAD='{"partId":999,"partName":"Brake Pad","quantity":2,"reason":"Needed for job"}'
call POST "/api/v1/parts-requests" "$PARTS_PAYLOAD"

# 8) Chatbot message (capture sessionId)
CHAT_PAYLOAD='{"message":"Hello, I want to start a session"}'
resp=$(curl -s -X POST -H "Content-Type: application/json" ${AUTH_HEADER:+-H "Authorization: $AUTH_HEADER"} -d "$CHAT_PAYLOAD" "$BASE_URL/api/v1/chat/send")
echo ">>> Chat send response:"; echo "$resp" | jq -r '.' || true
SESSION_ID=$(echo "$resp" | jq -r '.sessionId // .session_id // empty')
if [ -n "$SESSION_ID" ]; then
  echo "Extracted chat sessionId: $SESSION_ID"
  # Close chat session
  call POST "/api/v1/chat/sessions/$SESSION_ID/close"
else
  echo "No chat sessionId found in response"
fi

# 9) Tracking tasks
TASK_PAYLOAD='{"title":"Inspect vehicle","description":"Inspect brakes","assigneeId":"emp-101","vehicleId":"veh-1"}'
call POST "/api/v1/tracking/tasks" "$TASK_PAYLOAD"

echo "\nDone. Extracted IDs: BOOKING_ID=${BOOKING_ID:-} PAYMENT_ID=${PAYMENT_ID:-} SESSION_ID=${SESSION_ID:-}"
echo "Check notification service logs or DB for created notifications."