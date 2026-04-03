# WiFi + Firestore Architecture
## Child Health Monitor - Technical Architecture

---

## 📐 SYSTEM ARCHITECTURE OVERVIEW

```
┌─────────────────────────────────────────────────────────────────────┐
│                         HARDWARE LAYER                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  ESP32 (WiFi-enabled microcontroller)                        │  │
│  │  ┌────────────┐  ┌────────────┐                             │  │
│  │  │ MAX30102   │  │ MLX90614   │                             │  │
│  │  │ HR + SpO2  │  │ Temp       │                             │  │
│  │  └─────┬──────┘  └─────┬──────┘                             │  │
│  │        │ I2C           │ I2C                                 │  │
│  │        └───────┬───────┘                                     │  │
│  │                │                                              │  │
│  │        ┌───────▼────────┐                                    │  │
│  │        │  Sensor Reader │                                    │  │
│  │        │  (1 Hz)        │                                    │  │
│  │        └───────┬────────┘                                    │  │
│  │                │                                              │  │
│  │        ┌───────▼────────┐                                    │  │
│  │        │ JSON Serializer│                                    │  │
│  │        └───────┬────────┘                                    │  │
│  │                │                                              │  │
│  │        ┌───────▼────────┐                                    │  │
│  │        │ WebSocket      │                                    │  │
│  │        │ Server :8080   │                                    │  │
│  │        └───────┬────────┘                                    │  │
│  └────────────────┼─────────────────────────────────────────────┘  │
└───────────────────┼─────────────────────────────────────────────────┘
                    │ WiFi (WebSocket)
                    │ ws://192.168.1.100:8080/health
                    │ JSON: {"heartRate":75,"spo2":98,"temp":36.7}
                    │
┌───────────────────▼─────────────────────────────────────────────────┐
│                      ANDROID APP LAYER                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    PRESENTATION LAYER                         │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐            │  │
│  │  │ Dashboard  │  │ Monitoring │  │  History   │            │  │
│  │  │ Fragment   │  │ Fragment   │  │  Fragment  │            │  │
│  │  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘            │  │
│  │        │                │                │                    │  │
│  │        └────────────────┼────────────────┘                    │  │
│  │                         │ LiveData Observers                  │  │
│  └─────────────────────────┼─────────────────────────────────────┘  │
│                            │                                         │
│  ┌─────────────────────────▼─────────────────────────────────────┐  │
│  │                    VIEWMODEL LAYER (MVVM)                     │  │
│  │  ┌──────────────────────────────────────────────────────────┐│  │
│  │  │  HealthMonitorViewModelWifi                              ││  │
│  │  │  ┌────────────────────────────────────────────────────┐ ││  │
│  │  │  │ LiveData:                                          │ ││  │
│  │  │  │  - currentReading                                  │ ││  │
│  │  │  │  - healthStatus                                    │ ││  │
│  │  │  │  - connectionState                                 │ ││  │
│  │  │  │  - heartRate, spO2, temperature                    │ ││  │
│  │  │  └────────────────────────────────────────────────────┘ ││  │
│  │  │  ┌────────────────────────────────────────────────────┐ ││  │
│  │  │  │ Storage Strategy:                                  │ ││  │
│  │  │  │  - Every 10 seconds                                │ ││  │
│  │  │  │  - On status change                                │ ││  │
│  │  │  └────────────────────────────────────────────────────┘ ││  │
│  │  └──────────────┬───────────────────┬───────────────────────┘│  │
│  └─────────────────┼───────────────────┼─────────────────────────┘  │
│                    │                   │                             │
│         ┌──────────▼──────────┐  ┌────▼──────────────────┐          │
│         │ WifiHealthMonitor   │  │ FirestoreHealth       │          │
│         │ Manager             │  │ Repository            │          │
│         └──────────┬──────────┘  └────┬──────────────────┘          │
│                    │                   │                             │
│  ┌─────────────────▼───────────────────▼─────────────────────────┐  │
│  │                    DATA LAYER                                  │  │
│  │  ┌──────────────────────┐  ┌──────────────────────────────┐  │  │
│  │  │ WebSocket Client     │  │ Firestore SDK                │  │  │
│  │  │ (OkHttp)             │  │ (Firebase)                   │  │  │
│  │  │                      │  │                              │  │  │
│  │  │ - Connection mgmt    │  │ - CRUD operations            │  │  │
│  │  │ - JSON parsing       │  │ - Real-time listeners        │  │  │
│  │  │ - Auto-reconnect     │  │ - Offline persistence        │  │  │
│  │  │ - Heartbeat          │  │ - Multi-user isolation       │  │  │
│  │  └──────────┬───────────┘  └──────────┬───────────────────┘  │  │
│  └─────────────┼──────────────────────────┼──────────────────────┘  │
└────────────────┼──────────────────────────┼──────────────────────────┘
                 │                          │
                 │ WiFi                     │ Internet
                 │                          │
┌────────────────▼──────────────────────────▼──────────────────────────┐
│                      CLOUD LAYER                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Firebase Firestore                         │   │
│  │  ┌──────────────────────────────────────────────────────────┐│   │
│  │  │  Collection: users/{userId}/health_records               ││   │
│  │  │  ┌────────────────────────────────────────────────────┐  ││   │
│  │  │  │ Document: {recordId}                               │  ││   │
│  │  │  │  - heartRate: number                               │  ││   │
│  │  │  │  - spo2: number                                    │  ││   │
│  │  │  │  - temperature: number                             │  ││   │
│  │  │  │  - status: string                                  │  ││   │
│  │  │  │  - timestamp: timestamp                            │  ││   │
│  │  │  │  - hrValid: boolean                                │  ││   │
│  │  │  │  - tempValid: boolean                              │  ││   │
│  │  │  └────────────────────────────────────────────────────┘  ││   │
│  │  └──────────────────────────────────────────────────────────┘│   │
│  │  ┌──────────────────────────────────────────────────────────┐│   │
│  │  │  Features:                                                ││   │
│  │  │  - Offline cache (local persistence)                     ││   │
│  │  │  - Real-time sync                                        ││   │
│  │  │  - Security rules (user isolation)                       ││   │
│  │  │  - Automatic backups                                     ││   │
│  │  │  - Scalable (multi-device)                               ││   │
│  │  └──────────────────────────────────────────────────────────┘│   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    Firebase Authentication                    │   │
│  │  - Email/Password                                             │   │
│  │  - Google Sign-In                                             │   │
│  │  - User ID generation                                         │   │
│  │  - Session management                                         │   │
│  └──────────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 DATA FLOW DIAGRAM

### Real-Time Monitoring Flow

```
┌─────────────┐
│   Sensors   │
│ MAX30102    │
│ MLX90614    │
└──────┬──────┘
       │ I2C (1 Hz)
       │
┌──────▼──────┐
│   ESP32     │
│ Read Sensors│
└──────┬──────┘
       │
┌──────▼──────┐
│   JSON      │
│ Serialize   │
└──────┬──────┘
       │ {"heartRate":75,"spo2":98,"temp":36.7}
       │
┌──────▼──────┐
│ WebSocket   │
│   Send      │
└──────┬──────┘
       │ WiFi
       │
┌──────▼──────────────┐
│ Android App         │
│ WifiHealthMonitor   │
│ Manager             │
└──────┬──────────────┘
       │ Callback
       │
┌──────▼──────────────┐
│ ViewModel           │
│ - Parse JSON        │
│ - Update LiveData   │
│ - Evaluate Status   │
└──────┬──────────────┘
       │
       ├─────────────────────┐
       │                     │
┌──────▼──────────┐   ┌──────▼──────────┐
│ UI Update       │   │ Storage Logic   │
│ - Dashboard     │   │ - Every 10s     │
│ - Charts        │   │ - Status change │
│ - Animations    │   └──────┬──────────┘
└─────────────────┘          │
                             │
                      ┌──────▼──────────┐
                      │ Firestore       │
                      │ Repository      │
                      └──────┬──────────┘
                             │ Internet
                             │
                      ┌──────▼──────────┐
                      │ Firebase        │
                      │ Firestore       │
                      │ (Cloud)         │
                      └─────────────────┘
```

---

## 🏗️ COMPONENT ARCHITECTURE

### 1. Hardware Layer (ESP32)

**Components:**
- **Sensor Reader:** Reads MAX30102 and MLX90614 at 1 Hz
- **JSON Serializer:** Converts sensor data to JSON format
- **WebSocket Server:** Listens on port 8080, handles connections
- **WiFi Manager:** Manages WiFi connection, auto-reconnect

**Responsibilities:**
- Read sensor data accurately
- Serialize to JSON
- Transmit over WebSocket
- Handle connection failures
- Report errors

**Technologies:**
- ESP32 Arduino Core
- ArduinoWebSockets library
- ArduinoJson library
- MAX30105 library
- Adafruit MLX90614 library

---

### 2. Presentation Layer (Android UI)

**Components:**
- **Fragments:** Dashboard, Monitoring, History, Settings, Scan
- **Layouts:** XML layouts with Material You design
- **Adapters:** RecyclerView adapters for lists
- **Animations:** Pulse, blink, scale animations

**Responsibilities:**
- Display real-time data
- Render charts
- Handle user input
- Show connection status
- Display errors

**Technologies:**
- Android Fragments
- Material Components
- MPAndroidChart
- Navigation Component
- ViewBinding

---

### 3. ViewModel Layer (MVVM)

**Component:** `HealthMonitorViewModelWifi`

**Responsibilities:**
- Manage WiFi connection lifecycle
- Expose LiveData for UI observation
- Implement storage strategy (10s batching)
- Evaluate health status
- Handle errors

**LiveData Exposed:**
```java
- currentReading: HealthReading
- healthStatus: HealthStatus
- connectionState: ConnectionState
- errorMessage: String
- heartRate: Integer
- spO2: Integer
- temperature: Float
- hrValid: Boolean
- tempValid: Boolean
```

**Storage Strategy:**
```java
Store if:
1. Time since last store >= 10 seconds
2. Health status changed (GOOD → WARNING → CRITICAL)

Prevents:
- Excessive Firestore writes
- High costs
- Network congestion

Ensures:
- Critical data captured
- Status changes recorded
- Efficient storage
```

---

### 4. WiFi Manager

**Component:** `WifiHealthMonitorManager`

**Responsibilities:**
- Establish WebSocket connection
- Parse JSON messages
- Handle auto-reconnect
- Send heartbeat (keep-alive)
- Manage connection state

**Connection States:**
```java
DISCONNECTED → CONNECTING → CONNECTED
                    ↓
                  ERROR
                    ↓
              (auto-reconnect)
```

**Auto-Reconnect Strategy:**
```
Attempt 1: 3 seconds delay
Attempt 2: 6 seconds delay
Attempt 3: 9 seconds delay
Attempt 4: 12 seconds delay
Attempt 5: 15 seconds delay
Max attempts: 5
```

**Heartbeat Mechanism:**
```
Interval: 30 seconds
Purpose: Keep connection alive
Format: {"type":"ping","timestamp":12345}
Response: {"type":"pong","timestamp":12345}
```

---

### 5. Firestore Repository

**Component:** `FirestoreHealthRepository`

**Responsibilities:**
- CRUD operations on Firestore
- Real-time listeners
- Offline persistence
- Multi-user isolation
- Query optimization

**Collection Structure:**
```
users/
  {userId}/
    health_records/
      {recordId}/
        - heartRate: number
        - spo2: number
        - temperature: number
        - status: string
        - timestamp: timestamp
        - hrValid: boolean
        - tempValid: boolean
```

**Query Methods:**
```java
- getAllRecords(): LiveData<List<HealthRecordFirestore>>
- getLastNRecords(limit): LiveData<List<HealthRecordFirestore>>
- getRecordsByTimeRange(start, end): LiveData<List<HealthRecordFirestore>>
- getCriticalRecords(): LiveData<List<HealthRecordFirestore>>
- getRecordCount(callback): void
- deleteAllRecords(): void
- deleteOldRecords(days): void
- getAverageVitals(start, end, callback): void
```

**Offline Persistence:**
```
Enabled by default in Firestore SDK
- Writes queued locally
- Reads from cache
- Automatic sync on reconnection
- No code changes required
```

---

## 🔐 SECURITY ARCHITECTURE

### Firebase Authentication

**Flow:**
```
User → Login → Firebase Auth → Token → Firestore
                                  ↓
                            User ID (UID)
                                  ↓
                    users/{UID}/health_records/
```

**Security Rules:**
```javascript
// User can only access their own data
match /users/{userId}/health_records/{recordId} {
  allow read, write: if request.auth.uid == userId;
}
```

**Data Isolation:**
- Each user has separate collection
- User ID from Firebase Auth
- Security rules enforce isolation
- No cross-user access possible

---

## 📊 DATA MODEL

### HealthReading (Model)

```java
class HealthReading {
    int heartRate;        // 0-250 BPM
    int spO2;             // 0-100%
    float temperature;    // 30-45°C
    boolean hrValid;      // Heart rate validity
    boolean tempValid;    // Temperature validity
    long timestamp;       // Milliseconds since epoch
}
```

### HealthStatus (Model)

```java
class HealthStatus {
    Condition overallCondition;  // GOOD, WARNING, CRITICAL
    String heartRateStatus;      // "Normal", "High", "Low"
    String spO2Status;           // "Normal", "Low"
    String temperatureStatus;    // "Normal", "High", "Low"
}

enum Condition {
    GOOD,      // All vitals normal
    WARNING,   // One vital abnormal
    CRITICAL   // Multiple vitals abnormal or severe
}
```

### HealthRecordFirestore (Firestore Document)

```java
class HealthRecordFirestore {
    String id;              // Document ID (auto-generated)
    long timestamp;         // Milliseconds since epoch
    int heartRate;          // BPM
    int spO2;               // Percentage
    double temperature;     // Celsius
    String healthStatus;    // "GOOD", "WARNING", "CRITICAL"
    String userId;          // Firebase Auth UID
}
```

---

## 🔄 STATE MANAGEMENT

### Connection State Machine

```
┌──────────────┐
│ DISCONNECTED │ ◄─────────────────┐
└──────┬───────┘                   │
       │ connect()                 │
       │                           │
┌──────▼───────┐                   │
│  CONNECTING  │                   │
└──────┬───────┘                   │
       │                           │
       ├─────────────┐             │
       │ success     │ failure     │
       │             │             │
┌──────▼───────┐ ┌──▼──────┐      │
│  CONNECTED   │ │  ERROR  │──────┘
└──────┬───────┘ └─────────┘
       │                ↑
       │ disconnect()   │
       │                │
┌──────▼────────┐       │
│ DISCONNECTING │───────┘
└───────────────┘
```

### Health Status Evaluation

```
Input: HealthReading
  ↓
Evaluate Heart Rate:
  - 60-120 BPM: Normal
  - <60 or >120: Abnormal
  ↓
Evaluate SpO2:
  - ≥95%: Normal
  - <95%: Abnormal
  ↓
Evaluate Temperature:
  - 36.0-37.5°C: Normal
  - <36.0 or >37.5: Abnormal
  ↓
Overall Status:
  - All normal: GOOD
  - 1 abnormal: WARNING
  - 2+ abnormal: CRITICAL
  ↓
Output: HealthStatus
```

---

## 📈 PERFORMANCE ARCHITECTURE

### Latency Optimization

**Target Latency:** < 2 seconds (sensor → UI)

**Breakdown:**
```
Sensor read:        50ms
JSON serialize:     10ms
WebSocket send:     20ms
Network transit:    50ms
JSON parse:         10ms
ViewModel update:   10ms
UI render:          50ms
─────────────────────────
Total:             200ms ✓
```

**Optimization Techniques:**
- Efficient JSON parsing
- Background threading
- LiveData observers
- Minimal UI updates
- Hardware acceleration

---

### Firestore Write Optimization

**Strategy:** Batch writes every 10 seconds

**Comparison:**
```
Without batching:
- 1 write/second
- 86,400 writes/day
- Cost: $0.18/day

With batching (10s):
- 1 write/10 seconds
- 8,640 writes/day
- Cost: $0 (free tier)

Savings: 90% reduction
```

**Implementation:**
```java
private void storeHealthDataIfNeeded(HealthReading reading, HealthStatus status) {
    long currentTime = System.currentTimeMillis();
    
    // Store if 10 seconds elapsed
    if (currentTime - lastStoredTimestamp >= 10000) {
        repository.insertHealthRecord(reading, status);
        lastStoredTimestamp = currentTime;
    }
    
    // OR store if status changed (critical data)
    if (lastStoredStatus != status.getOverallCondition()) {
        repository.insertHealthRecord(reading, status);
        lastStoredStatus = status.getOverallCondition();
    }
}
```

---

### Offline Persistence

**Firestore Cache:**
```
Enabled by default
- Local SQLite cache
- Automatic sync
- No code changes
- Transparent to app
```

**Benefits:**
- Works offline
- Faster reads
- Reduced network usage
- Better user experience

---

## 🔧 TECHNOLOGY STACK

### Android App

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Architecture | MVVM | - |
| UI | Material You | 3.0 |
| Networking | OkHttp | 4.12.0 |
| Database | Firestore | Latest |
| Auth | Firebase Auth | Latest |
| Charts | MPAndroidChart | 3.1.0 |
| Logging | Timber | 5.0.1 |

### ESP32 Firmware

| Component | Library | Version |
|-----------|---------|---------|
| Core | ESP32 Arduino | 2.0+ |
| WebSocket | ArduinoWebSockets | 2.3+ |
| JSON | ArduinoJson | 6.21+ |
| HR/SpO2 | MAX30105 | 1.2+ |
| Temperature | Adafruit MLX90614 | 2.1+ |

### Cloud Services

| Service | Purpose | Tier |
|---------|---------|------|
| Firebase Auth | User authentication | Free |
| Firestore | Cloud database | Free/Pay-as-you-go |
| Firebase Analytics | Usage tracking | Free |

---

## 🎯 DESIGN PATTERNS

### 1. MVVM (Model-View-ViewModel)

**Benefits:**
- Separation of concerns
- Testability
- Maintainability
- Reactive UI updates

**Implementation:**
```
View (Fragment) → ViewModel → Repository → Data Source
       ↑              ↓
       └─── LiveData ─┘
```

### 2. Repository Pattern

**Benefits:**
- Abstract data source
- Centralized data access
- Easy to swap implementations
- Testable

**Implementation:**
```
ViewModel → Repository → Firestore
                      → Room (deprecated)
```

### 3. Observer Pattern

**Benefits:**
- Reactive updates
- Decoupled components
- Lifecycle-aware
- Automatic cleanup

**Implementation:**
```
LiveData → Observer → UI Update
```

### 4. Singleton Pattern

**Benefits:**
- Single instance
- Global access
- Resource efficiency

**Implementation:**
```
FirebaseFirestore.getInstance()
FirebaseAuth.getInstance()
```

---

## ✅ ARCHITECTURE PRINCIPLES

### 1. Separation of Concerns
- UI logic in Fragments
- Business logic in ViewModel
- Data logic in Repository
- Network logic in Manager

### 2. Single Responsibility
- Each class has one purpose
- Easy to understand
- Easy to test
- Easy to maintain

### 3. Dependency Injection
- Dependencies passed in constructor
- Testable
- Flexible
- Decoupled

### 4. Reactive Programming
- LiveData for UI updates
- Observers for data changes
- Lifecycle-aware
- Automatic cleanup

### 5. Offline-First
- Local cache priority
- Sync when online
- Works without internet
- Better user experience

---

*Last Updated: Current Session*
*Status: Architecture Documentation Complete*
*Version: 1.0*
