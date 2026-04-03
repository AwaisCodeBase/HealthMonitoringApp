# WiFi + Firestore Migration Plan
## Child Health Monitor - BLE to WiFi & Room to Firestore

---

## 📋 EXECUTIVE SUMMARY

**Objective:** Transform the Child Health Monitor from BLE + Room architecture to WiFi + Firestore while maintaining all existing features, UI, and MVVM architecture.

**Approach:** WebSocket-based real-time communication (preferred over REST API)

**Timeline:** Phased migration with backward compatibility

**Risk Level:** Medium (network dependency, cloud costs)

---

## 🎯 DECISION: WebSocket vs REST API

### ✅ CHOSEN: WebSocket (Preferred)

**Justification:**
- **Real-time bidirectional communication** - Perfect for 1 Hz health monitoring
- **Lower latency** - Persistent connection eliminates HTTP handshake overhead
- **Efficient** - Single connection vs multiple HTTP requests
- **Push notifications** - Server can push alerts to device
- **Better for healthcare** - Continuous monitoring requires persistent connection

**Trade-offs:**
- More complex than REST API
- Requires WebSocket server on Arduino/ESP32
- Connection management overhead

### ❌ NOT CHOSEN: REST API (HTTP)

**Why not:**
- Polling overhead (1 Hz = 1 request/second = 3,600 requests/hour)
- Higher latency (TCP handshake per request)
- Battery drain on mobile device
- Not ideal for real-time continuous monitoring
- Server load from constant polling

**When to use REST:**
- Intermittent data updates
- Request-response patterns
- Simpler implementation needs

---

## 🏗️ NEW ARCHITECTURE

### Current Architecture (BLE + Room)
```
┌─────────────────────────────────────────────────────────────┐
│                    HARDWARE LAYER                            │
│  MAX30102 + MLX90614 → Arduino Nano 33 BLE                  │
└──────────────────┬──────────────────────────────────────────┘
                   │ BLE (GATT)
┌──────────────────▼──────────────────────────────────────────┐
│                 ANDROID APP LAYER                            │
│  HealthMonitorBleManager                                     │
│         ↓                                                    │
│  HealthMonitorViewModel (MVVM)                              │
│         ↓                                                    │
│  HealthDataRepository                                        │
│         ↓                                                    │
│  Room Database (SQLite)                                      │
│         ↓                                                    │
│  UI (Dashboard, Charts, History)                            │
└─────────────────────────────────────────────────────────────┘
```

### New Architecture (WiFi + Firestore)
```
┌─────────────────────────────────────────────────────────────┐
│                    HARDWARE LAYER                            │
│  MAX30102 + MLX90614 → ESP32 (WiFi)                         │
│  WebSocket Server (Port 8080)                               │
└──────────────────┬──────────────────────────────────────────┘
                   │ WiFi (WebSocket)
                   │ JSON: {"heartRate":75,"spo2":98,"temp":36.7}
┌──────────────────▼──────────────────────────────────────────┐
│                 ANDROID APP LAYER                            │
│  WifiHealthMonitorManager (WebSocket Client)                │
│         ↓                                                    │
│  HealthMonitorViewModel (MVVM) - UNCHANGED                  │
│         ↓                                                    │
│  FirestoreHealthRepository (NEW)                            │
│         ↓                                                    │
│  Firebase Firestore (Cloud + Offline Cache)                 │
│         ↓                                                    │
│  UI (Dashboard, Charts, History) - UNCHANGED                │
└─────────────────────────────────────────────────────────────┘

Firebase Auth: Multi-user isolation (users/{userId}/health_records)
```

---

## 📊 DATA FLOW COMPARISON

### OLD: BLE + Room
```
Arduino → BLE GATT → BleManager → ViewModel → Repository → Room → UI
         (binary)    (callback)   (LiveData)  (executor)  (SQL)
```

### NEW: WiFi + Firestore
```
ESP32 → WebSocket → WifiManager → ViewModel → Repository → Firestore → UI
       (JSON)      (callback)    (LiveData)  (executor)  (NoSQL)
```

**Key Changes:**
1. Binary BLE packets → JSON over WebSocket
2. Room DAO → Firestore SDK
3. Local SQLite → Cloud Firestore (with offline cache)
4. BLE scanning → WiFi device discovery (mDNS/static IP)

---

## 🔄 MIGRATION PHASES

### Phase 1: Firestore Integration (Backend First)
**Goal:** Replace Room with Firestore without touching BLE

**Tasks:**
1. ✅ Add Firestore dependencies (already in build.gradle)
2. ✅ Create `FirestoreHealthRepository`
3. ✅ Implement Firestore CRUD operations
4. ✅ Add offline persistence
5. ✅ Update ViewModel to use new repository
6. ✅ Test with existing BLE data flow

**Deliverables:**
- `FirestoreHealthRepository.java`
- Firestore security rules
- Migration script (Room → Firestore)

**Testing:**
- Verify data writes to Firestore
- Test offline mode
- Validate multi-user isolation

---

### Phase 2: WiFi Communication (Frontend)
**Goal:** Replace BLE with WebSocket

**Tasks:**
1. ✅ Create `WifiHealthMonitorManager`
2. ✅ Implement WebSocket client (OkHttp)
3. ✅ Add connection management
4. ✅ Implement auto-reconnect
5. ✅ Update ViewModel to use WiFi manager
6. ✅ Remove BLE dependencies

**Deliverables:**
- `WifiHealthMonitorManager.java`
- `WifiConnectionConfig.java`
- Connection state management

**Testing:**
- WebSocket connection/disconnection
- JSON parsing
- Network failure handling

---

### Phase 3: Arduino/ESP32 Firmware
**Goal:** WiFi-enabled device firmware

**Tasks:**
1. ✅ Port Arduino code to ESP32
2. ✅ Implement WebSocket server
3. ✅ JSON serialization
4. ✅ WiFi connection management
5. ✅ Sensor data streaming

**Deliverables:**
- `ESP32_WiFi_Health_Monitor.ino`
- WiFi configuration guide
- Troubleshooting guide

---

### Phase 4: Testing & Optimization
**Goal:** Production-ready system

**Tasks:**
1. ✅ End-to-end testing
2. ✅ Performance optimization
3. ✅ Battery/power testing
4. ✅ Network latency analysis
5. ✅ Firestore cost optimization

---

## 📁 FILE CHANGES

### Files to CREATE
```
✅ app/src/main/java/com/example/sensorycontrol/wifi/
   ├── WifiHealthMonitorManager.java
   ├── WifiConnectionConfig.java
   └── WebSocketCallback.java

✅ app/src/main/java/com/example/sensorycontrol/repository/
   └── FirestoreHealthRepository.java

✅ arduino/
   └── ESP32_WiFi_Health_Monitor.ino

✅ Documentation/
   ├── WIFI_FIRESTORE_MIGRATION_PLAN.md (this file)
   ├── WIFI_SETUP_GUIDE.md
   ├── FIRESTORE_SCHEMA.md
   └── MIGRATION_TESTING_GUIDE.md
```

### Files to MODIFY
```
✅ app/src/main/java/com/example/sensorycontrol/viewmodels/
   └── HealthMonitorViewModel.java (replace BLE with WiFi)

✅ app/src/main/java/com/example/sensorycontrol/models/
   └── HealthReading.java (add fromJson() method)

✅ app/build.gradle.kts (add OkHttp for WebSocket)

✅ AndroidManifest.xml (add INTERNET permission)
```

### Files to DEPRECATE (Keep for backward compatibility)
```
⚠️ app/src/main/java/com/example/sensorycontrol/ble/
   └── HealthMonitorBleManager.java (mark @Deprecated)

⚠️ app/src/main/java/com/example/sensorycontrol/database/
   ├── AppDatabase.java
   ├── HealthRecordDao.java
   └── HealthRecordEntity.java

⚠️ app/src/main/java/com/example/sensorycontrol/repository/
   └── HealthDataRepository.java (rename to RoomHealthRepository)
```

---

## 🔐 FIRESTORE SCHEMA

### Collection Structure
```
firestore/
└── users/
    └── {userId}/                    # Firebase Auth UID
        └── health_records/          # Subcollection
            └── {recordId}/          # Auto-generated document ID
                ├── heartRate: number
                ├── spo2: number
                ├── temperature: number
                ├── status: string   # "GOOD", "WARNING", "CRITICAL"
                ├── timestamp: timestamp
                └── deviceId: string (optional)
```

### Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own health records
    match /users/{userId}/health_records/{recordId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Indexes (for queries)
```
Collection: users/{userId}/health_records
Fields: timestamp (Descending), status (Ascending)
Query: WHERE status == "CRITICAL" ORDER BY timestamp DESC
```

---

## 📡 WEBSOCKET PROTOCOL

### Connection
```
URL: ws://<ESP32_IP>:8080/health
Protocol: WebSocket (RFC 6455)
```

### Message Format (Device → Android)
```json
{
  "heartRate": 75,
  "spo2": 98,
  "temperature": 36.7,
  "timestamp": 1710000000,
  "valid": {
    "hr": true,
    "temp": true
  }
}
```

### Control Messages (Android → Device)
```json
{
  "command": "start|stop|status",
  "timestamp": 1710000000
}
```

### Error Messages
```json
{
  "error": "sensor_failure",
  "message": "MAX30102 not responding",
  "timestamp": 1710000000
}
```

---

## ⚠️ RISKS & MITIGATION

### Risk 1: Network Dependency
**Impact:** HIGH - App won't work without WiFi
**Mitigation:**
- Firestore offline persistence (automatic)
- Local caching of last 100 readings
- Clear UI indicators for connection status
- Graceful degradation

### Risk 2: Firestore Costs
**Impact:** MEDIUM - Cloud storage costs
**Mitigation:**
- Batch writes (10-second intervals)
- Delete old records (>30 days)
- Use Firestore free tier (50K reads/day, 20K writes/day)
- Monitor usage with Firebase console

**Cost Estimate:**
- 1 write every 10 seconds = 8,640 writes/day
- Free tier: 20,000 writes/day
- **Result:** FREE for single user, $0.18/month for 10 users

### Risk 3: WebSocket Connection Stability
**Impact:** MEDIUM - Connection drops
**Mitigation:**
- Auto-reconnect with exponential backoff
- Heartbeat/ping-pong (every 30 seconds)
- Connection timeout (60 seconds)
- Fallback to last known good state

### Risk 4: Data Migration
**Impact:** LOW - Existing Room data loss
**Mitigation:**
- Keep Room database for 30 days
- Provide migration tool (Room → Firestore)
- Export to CSV before migration
- Dual-write during transition period

### Risk 5: ESP32 vs Arduino Nano 33 BLE
**Impact:** MEDIUM - Hardware change required
**Mitigation:**
- ESP32 is cheaper ($5 vs $25)
- Same sensor compatibility (I2C)
- Better WiFi performance
- Provide migration guide

---

## 🔧 DEPENDENCIES

### Android (build.gradle.kts)
```kotlin
dependencies {
    // Existing
    implementation("com.google.firebase:firebase-firestore")
    
    // NEW: WebSocket client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // NEW: JSON parsing (if not using Gson)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // REMOVE: Room (after migration)
    // implementation(libs.room.runtime)
    // annotationProcessor(libs.room.compiler)
}
```

### ESP32 (Arduino IDE)
```cpp
// Libraries
#include <WiFi.h>
#include <WebSocketsServer.h>
#include <Wire.h>
#include <MAX30105.h>
#include <Adafruit_MLX90614.h>
#include <ArduinoJson.h>
```

---

## 📈 PERFORMANCE COMPARISON

### BLE vs WiFi

| Metric | BLE | WiFi (WebSocket) |
|--------|-----|------------------|
| **Range** | 10-30m | 50-100m |
| **Latency** | 10-50ms | 5-20ms |
| **Throughput** | 1 Mbps | 54-600 Mbps |
| **Power** | Very Low | Medium |
| **Connection** | 1:1 | 1:Many |
| **Setup** | Pairing | WiFi credentials |
| **Cost** | $25 (Nano 33 BLE) | $5 (ESP32) |

### Room vs Firestore

| Metric | Room (SQLite) | Firestore |
|--------|---------------|-----------|
| **Storage** | Local only | Cloud + Local cache |
| **Sync** | None | Automatic |
| **Multi-device** | No | Yes |
| **Offline** | Always | Cached |
| **Queries** | SQL | NoSQL |
| **Cost** | Free | Free tier + usage |
| **Backup** | Manual | Automatic |

---

## 🧪 TESTING STRATEGY

### Unit Tests
```
✅ WifiHealthMonitorManager
   - Connection/disconnection
   - JSON parsing
   - Error handling
   - Auto-reconnect

✅ FirestoreHealthRepository
   - CRUD operations
   - Offline mode
   - Multi-user isolation
   - Query performance
```

### Integration Tests
```
✅ End-to-end data flow
   - ESP32 → Android → Firestore
   - Real-time updates
   - Network failures
   - Authentication
```

### Performance Tests
```
✅ Latency measurement
✅ Battery consumption
✅ Network bandwidth
✅ Firestore write rate
```

### User Acceptance Tests
```
✅ UI unchanged
✅ Charts working
✅ CSV export
✅ Multi-user support
```

---

## 📝 MIGRATION CHECKLIST

### Pre-Migration
- [ ] Backup Room database
- [ ] Export existing data to CSV
- [ ] Test Firestore security rules
- [ ] Verify Firebase project setup
- [ ] Order ESP32 devices

### Migration
- [ ] Deploy Firestore repository
- [ ] Test with BLE (hybrid mode)
- [ ] Deploy WiFi manager
- [ ] Update ViewModel
- [ ] Test end-to-end
- [ ] Flash ESP32 firmware

### Post-Migration
- [ ] Monitor Firestore usage
- [ ] Verify data integrity
- [ ] User training
- [ ] Update documentation
- [ ] Remove deprecated code (after 30 days)

---

## 🎓 LESSONS LEARNED

### Why WebSocket over REST?
Real-time healthcare monitoring requires persistent connections. WebSocket eliminates polling overhead and provides bidirectional communication for alerts and control commands.

### Why Firestore over Room?
Cloud storage enables multi-device sync, automatic backups, and remote monitoring. Offline persistence maintains local-first experience.

### Why ESP32 over Arduino Nano 33 BLE?
ESP32 provides better WiFi performance, lower cost, and same sensor compatibility. WiFi offers longer range and easier setup than BLE.

---

## 📞 SUPPORT & RESOURCES

### Documentation
- [WebSocket Protocol (RFC 6455)](https://tools.ietf.org/html/rfc6455)
- [Firebase Firestore Docs](https://firebase.google.com/docs/firestore)
- [OkHttp WebSocket](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-web-socket/)
- [ESP32 Arduino Core](https://github.com/espressif/arduino-esp32)

### Libraries
- [ArduinoWebSockets](https://github.com/Links2004/arduinoWebSockets)
- [ArduinoJson](https://arduinojson.org/)
- [MAX30105 Library](https://github.com/sparkfun/SparkFun_MAX3010x_Sensor_Library)

---

## ✅ SUCCESS CRITERIA

1. ✅ Real-time data updates (1 Hz)
2. ✅ All UI features working
3. ✅ Charts displaying correctly
4. ✅ CSV export functional
5. ✅ Multi-user isolation
6. ✅ Offline mode working
7. ✅ Auto-reconnect on network loss
8. ✅ Firestore costs within budget
9. ✅ No breaking changes to UI
10. ✅ MVVM architecture maintained

---

*Last Updated: Current Session*
*Status: Migration Plan Complete*
*Next: Implementation Phase 1 - Firestore Integration*
