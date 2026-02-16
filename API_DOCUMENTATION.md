# RidePool API Documentation

**Base URL**: `http://localhost:8080`

---

## 1. Create Ride Request

### Endpoint
```
POST /rides/request
```

### Request Body
```json
{
  "userId": "user1",
  "pickupLat": 28.5244,
  "pickupLng": 77.0855,
  "airportCode": "DEL",
  "seatsRequired": 2,
  "luggageCount": 2
}
```

### Success Response (201)
```json
{
  "id": "6993031d25dc7e016b50a5f3",
  "userId": "user1",
  "pickupLat": 28.5244,
  "pickupLng": 77.0855,
  "airportCode": "DEL",
  "seatsRequired": 2,
  "luggageCount": 2,
  "status": "WAITING",
  "groupId": null,
  "assignedCabId": null,
  "requestTime": "2026-02-16T10:30:00Z"
}
```

### What Happens Internally

**Stage 1: Request Saved**
- Request stored in MongoDB with status `WAITING`
- Auto-assigned timestamp

**Stage 2: Matching Engine Triggered**
- Searches for other `WAITING` requests with same airport
- Filters by distance (5 KM radius)
- Checks seat capacity (4 seats max)

---

## 2. Matching Engine Behavior

### Grouping Logic

#### Scenario A: Single Request (No Match)
```
User1 requests → Only 1 request exists → No matches
Status: WAITING (waiting for more passengers)
GroupId: null
```

#### Scenario B: Two Compatible Requests (Group Full)
```
User1 requests → User1.seats = 2, Status = WAITING
User2 requests → User2.seats = 2, Status = WAITING

Distance = 0.67 KM (within 5 KM) ✓
Total seats = 2 + 2 = 4 (equals capacity) ✓

Result:
- GroupId = "a270e46d-c85e-4ea6-b915-9e45347afe7c"
- Both users: Status = MATCHED
- Group Status = FULL → Triggers Cab Assignment
```

### Status States

| Status | Meaning |
|--------|---------|
| `WAITING` | Initial state, looking for matches |
| `MATCHED` | Grouped but not full (< 4 seats) |
| `ASSIGNED` | Group full, cab assigned |
| `COMPLETED` | Ride finished |
| `CANCELLED` | User cancelled |

### Matching Rules
✅ Same airport code  
✅ Both status = `WAITING`  
✅ Distance ≤ 5 KM  
✅ Combined seats ≤ 4  

---

## 3. Cab Assignment Engine

### Automatic Assignment (When Group = FULL)

**Trigger**: When grouped passengers total 4 seats

**Process**:
1. Find nearest available cab within 10 KM
2. Update cab status → `ASSIGNED`
3. Update all passengers → Status = `ASSIGNED`
4. Link all passengers to cabId

### Response After Assignment

```json
{
  "id": "6993031d25dc7e016b50a5f3",
  "userId": "user1",
  "pickupLat": 28.5244,
  "pickupLng": 77.0855,
  "airportCode": "DEL",
  "seatsRequired": 2,
  "luggageCount": 2,
  "status": "ASSIGNED",
  "groupId": "a270e46d-c85e-4ea6-b915-9e45347afe7c",
  "assignedCabId": "cab-6993031c25dc7e016b50a5f2",
  "requestTime": "2026-02-16T10:30:00Z"
}
```

### Cab Assignment Rules
✅ Cab must be `AVAILABLE`  
✅ Latest available cab within 10 KM  
✅ Minimum 2 passengers before assignment (configurable)  

---

## 4. Get Ride Request

### Endpoint
```
GET /rides/{id}
```

### Response
```json
{
  "id": "6993031d25dc7e016b50a5f3",
  "userId": "user1",
  "pickupLat": 28.5244,
  "pickupLng": 77.0855,
  "airportCode": "DEL",
  "seatsRequired": 2,
  "luggageCount": 2,
  "status": "ASSIGNED",
  "groupId": "a270e46d-c85e-4ea6-b915-9e45347afe7c",
  "assignedCabId": "cab-6993031c25dc7e016b50a5f2",
  "requestTime": "2026-02-16T10:30:00Z"
}
```

---

## 5. Register Cab

### Endpoint
```
POST /cabs
```

### Request Body
```json
{
  "driverName": "Raj Kumar",
  "currentLat": 28.5244,
  "currentLng": 77.0855,
  "totalSeats": 4,
  "luggageCapacity": 10
}
```

### Response (201)
```json
{
  "id": "cab-6993031c25dc7e016b50a5f2",
  "driverName": "Raj Kumar",
  "currentLat": 28.5244,
  "currentLng": 77.0855,
  "totalSeats": 4,
  "luggageCapacity": 10,
  "status": "AVAILABLE"
}
```

---

## 6. List All Cabs

### Endpoint
```
GET /cabs
```

### Response
```json
[
  {
    "id": "cab-6993031c25dc7e016b50a5f2",
    "driverName": "Raj Kumar",
    "currentLat": 28.5244,
    "currentLng": 77.0855,
    "totalSeats": 4,
    "luggageCapacity": 10,
    "status": "AVAILABLE"
  }
]
```

---

## Testing with cURL / Postman

### 1. Create Cab
```bash
curl -X POST http://localhost:8080/cabs \
  -H "Content-Type: application/json" \
  -d '{
    "driverName": "Raj Kumar",
    "currentLat": 28.5244,
    "currentLng": 77.0855,
    "totalSeats": 4,
    "luggageCapacity": 10
  }'
```

### 2. Create Ride Request (User 1)
```bash
curl -X POST http://localhost:8080/rides/request \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user1",
    "pickupLat": 28.5244,
    "pickupLng": 77.0855,
    "airportCode": "DEL",
    "seatsRequired": 2,
    "luggageCount": 2
  }'
```

### 3. Create Ride Request (User 2 - Compatible)
```bash
curl -X POST http://localhost:8080/rides/request \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user2",
    "pickupLat": 28.5280,
    "pickupLng": 77.0910,
    "airportCode": "DEL",
    "seatsRequired": 2,
    "luggageCount": 1
  }'
```

### 4. Get Ride by ID
```bash
curl -X GET http://localhost:8080/rides/RIDE_ID
```

### 5. Get All Cabs
```bash
curl -X GET http://localhost:8080/cabs
```

---

## Complete Test Flow

```
Step 1: POST /cabs → Get cab-id
Step 2: POST /rides/request (User1) → Status: WAITING
Step 3: POST /rides/request (User2) → Automatic Matching
        Both users: Status: MATCHED
        GroupId: Created
Step 4: Auto Cab Assignment Triggered
        Both users: Status: ASSIGNED
        assignedCabId: cab-id
Step 5: GET /rides/{id} → Verify Status: ASSIGNED
```

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid input",
  "message": "seatsRequired must be between 1 and 4"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Ride request not found"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "Database connection failed"
}
```

---

## Configuration

**File**: `src/main/resources/application.properties`

```properties
# Matching Radius
ridepool.matching.matching-radius-km=5.0

# Cab Capacity
ridepool.matching.cab-capacity-seats=4

# Assignment Radius
ridepool.assignment.cab-assignment-radius-km=10.0

# Min Passengers for Assignment
ridepool.assignment.min-passengers-for-assignment=2

# Server Port
server.port=8080
```

---

**Version**: 0.0.1 | **Last Updated**: February 16, 2026
