# Quick Reference - WiFi + Firestore Migration
## Child Health Monitor - Developer Cheat Sheet

---

## 🚀 QUICK START

### 1. Connect to ESP32
```java
// In your Fragment/Activity
HealthMonitorViewModelWifi viewModel = new ViewModelProvider(this)
    .get(HealthMonitorViewModelWifi.class);

// Connect to device
viewModel.connect("192.168.1.100");

// Observe connection state
viewModel.getConnectionState().observe(this, state -> {
    if (state == WifiHealthMonitorManager.ConnectionState.CONNECTED) {
        Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
    }
});
```

### 2. Receive Real-Time Data
```java
// Observe health data
viewModel.getCurrentReading().observe(this, reading -> {
    if (reading != null) {
        int hr = reading.getHeartRate();
        int spo2 = reading.getSpO2();
        float temp = reading.getTemperature();
        // Update UI
    }
});

// Observe health status
viewModel.getHealthStatus().observe(this, status -> {
    if (status != null) {
        HealthStatus.Condition condition = status.getOverallCondition();
        // Update status indicator
    }
});
```

### 3. Query Historical Data
```java
// Get last 100 records
viewModel.getLastNRecords(100).observe(this, records -> {
    // Display in chart or list
});

// Get records by time range
long startTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24h ago
long endTime = System.currentTimeMillis();
viewModel.getRecordsByTimeRange(startTime, endTime).observe(this, records -> {
    // Display in chart
});
```

---

## 📡 WEBSOCKET PROTOCOL

### Connection
```
URL: ws://192.168.1.100:8080/health
Protocol: WebSocket (RFC 6455)
```

### Health Data (Device → Android)
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

### Commands (Android → Device)
```json
{
  "command": "start|stop|status",
  "timestamp": 1710000000
}
```

### Heartbeat
```json
// Ping (Android → Device)
{"type":"ping","timestamp":1710000000}

// Pong (Device → Android)
{"type":"pong","timestamp":1710000000}
```

---

## 🔥 FIRESTORE OPERATIONS

### Insert Record
```java
FirestoreHealthRepository repo = new FirestoreHealthRepository();
HealthReading reading = new HealthReading(75, 98, 36.7f, true, true);
HealthStatus status = HealthStatus.evaluate(reading);
repo.insertHealthRecord(reading, status);
```

### Query Records
```java
// All records
repo.getAllRecords().observe(this, records -> {
    // Process records
});

// Last N records
repo.getLastNRecords(100).observe(this, records -> {
    // Process records
});

// Time range
repo.getRecordsByTimeRange(startTime, endTime).observe(this, records -> {
    // Process records
});

// Critical records only
repo.getCriticalRecords().observe(this, records -> {
    // Process critical records
});
```

### Delete Records
```java
// Delete all
repo.deleteAllRecords();

// Delete old records (>30 days)
repo.deleteOldRecords(30);
```

### Get Statistics
```java
// Record count
repo.getRecordCount(count -> {
    Log.d(TAG, "Total records: " + count);
});

// Average vitals
repo.getAverageVitals(startTime, endTime, (avgHR, avgSpO2, avgTemp) -> {
    Log.d(TAG, "Avg HR: " + avgHR);
    Log.d(TAG, "Avg SpO2: " + avgSpO2);
    Log.d(TAG, "Avg Temp: " + avgTemp);
});
```

---

## 🔐 FIRESTORE SECURITY RULES

### Deploy Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/health_records/{recordId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Test Rules (Firebase Console)
```
Operation: get
Path: /users/user123/health_records/record1
Auth: { uid: "user123" }
Expected: ALLOW
```

---

## 🔌 ESP32 SETUP

### 1. Install Libraries
```
Arduino IDE → Sketch → Include Library → Manage Libraries

Install:
- WebSockets by Markus Sattler
- ArduinoJson by Benoit Blanchon
- MAX30105 by SparkFun
- Adafruit MLX90614 by Adafruit
```

### 2. Configure WiFi
```cpp
const char* WIFI_SSID = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
```

### 3. Upload Firmware
```
Tools → Board → ESP32 Dev Module
Tools → Port → (select ESP32)
Click Upload
```

### 4. Get IP Address
```
Open Serial Monitor (115200 baud)
Look for: "IP Address: 192.168.1.100"
```

---

## 🧪 TESTING COMMANDS

### Test WiFi Connection
```java
WifiHealthMonitorManager manager = new WifiHealthMonitorManager(context);
manager.setWifiCallback(new WifiHealthMonitorManager.WifiCallback() {
    @Override
    public void onConnectionStateChanged(ConnectionState state) {
        Log.d(TAG, "State: " + state);
    }
    
    @Override
    public void onHealthDataReceived(HealthReading reading) {
        Log.d(TAG, "Data: " + reading.toString());
    }
    
    @Override
    public void onError(String error) {
        Log.e(TAG, "Error: " + error);
    }
});

manager.connect("192.168.1.100");
```

### Test Firestore Write
```java
FirestoreHealthRepository repo = new FirestoreHealthRepository();
HealthReading reading = new HealthReading(75, 98, 36.7f, true, true);
HealthStatus status = new HealthStatus();
repo.insertHealthRecord(reading, status);

// Check Firebase Console
// Navigate to: Firestore Database → Data
// Verify: users/{your-uid}/health_records/{doc-id}
```

---

## 🐛 TROUBLESHOOTING

### WiFi Connection Fails
```bash
# Check ESP32 IP
ping 192.168.1.100

# Check ESP32 Serial Monitor
# Look for: "WiFi connected!"

# Check Android Logcat
adb logcat | grep "WiFi"
```

### No Data in Firestore
```java
// Check authentication
FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
if (user == null) {
    Log.e(TAG, "User not authenticated!");
}

// Check Logcat for errors
adb logcat | grep "Firestore"
```

### Charts Not Displaying
```java
// Check data exists
repo.getRecordCount(count -> {
    Log.d(TAG, "Record count: " + count);
    if (count == 0) {
        Log.w(TAG, "No data to display!");
    }
});
```

---

## 📊 PERFORMANCE TARGETS

| Metric | Target | Command |
|--------|--------|---------|
| Latency | < 2s | Measure sensor→UI time |
| Firestore Write | < 500ms | Check Logcat timestamps |
| Battery (8h) | < 20% | Monitor battery stats |
| Daily Writes | 8,640 | Firebase Console → Usage |
| Daily Reads | < 100 | Firebase Console → Usage |

---

## 💰 COST MONITORING

### Check Firestore Usage
```
Firebase Console → Firestore Database → Usage

Monitor:
- Document reads
- Document writes
- Document deletes
- Storage size
```

### Expected Costs (Single User)
```
Writes: 8,640/day (FREE - under 20K limit)
Reads: ~100/day (FREE - under 50K limit)
Storage: ~1MB/month (FREE - under 1GB limit)
Total: $0/month
```

### Cost Optimization
```java
// Batch writes (every 10 seconds)
private static final long STORAGE_INTERVAL_MS = 10000;

// Delete old records
repo.deleteOldRecords(30); // Keep last 30 days
```

---

## 🔄 MIGRATION CHECKLIST

### Pre-Migration
- [ ] Backup Room database
- [ ] Export data to CSV
- [ ] Firebase project configured
- [ ] Firestore security rules deployed
- [ ] ESP32 device ready

### Implementation
- [ ] Add dependencies (OkHttp, Gson)
- [ ] Create WifiHealthMonitorManager
- [ ] Create FirestoreHealthRepository
- [ ] Create HealthMonitorViewModelWifi
- [ ] Update Fragments to use new ViewModel
- [ ] Flash ESP32 firmware

### Testing
- [ ] WiFi connection works
- [ ] Real-time data updates
- [ ] Firestore storage working
- [ ] Charts display correctly
- [ ] CSV export functional
- [ ] Offline mode works

### Cleanup
- [ ] Remove BLE dependencies (optional)
- [ ] Mark old classes @Deprecated
- [ ] Update documentation
- [ ] Monitor Firestore costs

---

## 📞 QUICK LINKS

### Documentation
- Migration Plan: `WIFI_FIRESTORE_MIGRATION_PLAN.md`
- Architecture: `WIFI_FIRESTORE_ARCHITECTURE.md`
- Step-by-Step: `STEP_BY_STEP_MIGRATION_GUIDE.md`
- Testing: `MIGRATION_TESTING_GUIDE.md`
- Security Rules: `FIRESTORE_SECURITY_RULES.txt`

### Code Files
- WiFi Manager: `app/src/main/java/.../wifi/WifiHealthMonitorManager.java`
- Firestore Repo: `app/src/main/java/.../repository/FirestoreHealthRepository.java`
- ViewModel: `app/src/main/java/.../viewmodels/HealthMonitorViewModelWifi.java`
- ESP32 Code: `arduino/ESP32_WiFi_Health_Monitor.ino`

### External Resources
- Firebase Console: https://console.firebase.google.com
- Firestore Docs: https://firebase.google.com/docs/firestore
- ESP32 Arduino: https://github.com/espressif/arduino-esp32
- OkHttp: https://square.github.io/okhttp/

---

## 🎯 KEY COMMANDS

### Android Studio
```bash
# Build project
./gradlew clean build

# Run app
./gradlew installDebug

# View logs
adb logcat | grep "HealthMonitor"
```

### Firebase CLI
```bash
# Login
firebase login

# Deploy rules
firebase deploy --only firestore:rules

# View logs
firebase firestore:logs
```

### Arduino IDE
```bash
# Verify code
Ctrl+R (Windows) / Cmd+R (Mac)

# Upload code
Ctrl+U (Windows) / Cmd+U (Mac)

# Serial Monitor
Ctrl+Shift+M (Windows) / Cmd+Shift+M (Mac)
```

---

## 💡 PRO TIPS

### 1. Use Static IP for ESP32
```cpp
// In ESP32 code
IPAddress local_IP(192, 168, 1, 100);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);

WiFi.config(local_IP, gateway, subnet);
WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
```

### 2. Enable Firestore Offline Persistence
```java
// Enabled by default, but can configure
FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
    .build();
firestore.setFirestoreSettings(settings);
```

### 3. Optimize Battery Usage
```java
// Reduce heartbeat frequency
private static final long HEARTBEAT_INTERVAL_MS = 60000; // 60 seconds

// Use WiFi sleep mode on ESP32
WiFi.setSleep(true);
```

### 4. Monitor Connection Quality
```java
viewModel.getConnectionState().observe(this, state -> {
    switch (state) {
        case CONNECTED:
            statusIndicator.setBackgroundColor(Color.GREEN);
            break;
        case CONNECTING:
            statusIndicator.setBackgroundColor(Color.YELLOW);
            break;
        case ERROR:
            statusIndicator.setBackgroundColor(Color.RED);
            break;
    }
});
```

### 5. Handle Network Errors Gracefully
```java
viewModel.getErrorMessage().observe(this, error -> {
    if (error != null) {
        Snackbar.make(view, error, Snackbar.LENGTH_LONG)
            .setAction("Retry", v -> viewModel.connect(ipAddress))
            .show();
        viewModel.clearError();
    }
});
```

---

*Last Updated: Current Session*
*Quick Reference Version: 1.0*
*Print this for easy access during development!*
