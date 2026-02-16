# 🚗 RidePool - Airport Ride Pooling System

An intelligent backend system that automatically matches airport passengers heading to the same destination and assigns them to available cabs.

## 📋 Quick Start

**Prerequisites**: Java 17+, Maven 3.6+, MongoDB Atlas

**Setup**:
```bash
git clone <repository-url>
cd ridepool
mvnw clean install
mvnw spring-boot:run
```

Application runs on `http://localhost:8080`

---

## 🛠️ Technology

- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Database**: MongoDB Atlas
- **Build**: Maven

---

## 📁 Project Structure

```
ridepool/
├── src/main/java/com/hintro/ridepool/
│   ├── controller/          # HTTP endpoints
│   ├── service/             # Business logic
│   ├── matcher/             # Matching algorithm
│   ├── entity/              # Data models
│   ├── repository/          # MongoDB persistence
│   ├── dto/                 # Data transfer objects
│   ├── config/              # Configuration
│   └── util/                # Utilities (distance calc)
├── src/main/resources/
│   └── application.properties
├── pom.xml
├── MATCHING_ENGINE_GUIDE.md
└── TEST_RESULTS.md
```

---

## ⚙️ Configuration

**File**: `src/main/resources/application.properties`

```properties
# MongoDB
spring.data.mongodb.uri=mongodb+srv://user:password@host/database

# Matching Engine
ridepool.matching.matching-radius-km=5.0
ridepool.matching.cab-capacity-seats=4

# Cab Assignment
ridepool.assignment.min-passengers-for-assignment=2
ridepool.assignment.cab-assignment-radius-km=10.0

# Server
server.port=8080
logging.level.com.hintro.ridepool=DEBUG
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/rides/request` | Create ride request |
| GET | `/rides/{id}` | Get ride by ID |
| GET | `/rides` | List all rides |
| POST | `/cabs` | Register cab |
| GET | `/cabs` | List cabs |

**Create Ride**:
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

---

## 🎯 How It Works

1. **Passenger Request** → Request stored in MongoDB with status WAITING
2. **Matching Engine** → Finds compatible requests (same airport, within 5 KM, capacity OK)
3. **Group Formation** → Assigns groupId and updates passenger status
4. **Cab Assignment** → If group full (4 seats), assigns nearest available cab
5. **Status Update** → All passengers notified of assignment

---

## 🏗️ Architecture

```
Controller → Service → Matcher → Repository → MongoDB
                ↓
           RideMatcher (Algorithm)
                ↓
         DistanceCalculator (Haversine)
```


**Key Components**:
- `RideRequestController`: HTTP endpoints
- `RideRequestService`: Business orchestration
- `RideMatcher`: Matching algorithm
- `DistanceCalculator`: Geographic calculations
- `RideRequest` / `Cab`: Data models

---

## 🧪 Testing

```bash
mvnw test
```

**Test Scenario**: Create cab → User1 requests → User2 requests → Auto-match → Assign cab ✅

See [API Documentation.md](API Documentation.md) for details.

---


## 🔐 Security

⚠️ **Never commit credentials** - Use `.gitignore` for:
- `application.properties` (contains passwords)
- `.env` files
- API keys and tokens

Use environment-specific profiles:
```bash
mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

---

## 🚀 Development

1. Create feature branch: `git checkout -b feature/name`
2. Test: `mvnw clean install && mvnw spring-boot:run`
3. Commit: `git commit -m "feat: description"`
4. Push & PR: `git push origin feature/name`

---

**Version**: 0.0.1-SNAPSHOT | **Last Updated**: February 16, 2026
#   R i d e P o o l _ B a c k e n d 
 
  
