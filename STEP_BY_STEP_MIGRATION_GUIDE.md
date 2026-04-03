# Step-by-Step Migration Guide
## BLE + Room → WiFi + Firestore

---

## 📋 OVERVIEW

This guide provides detailed step-by-step instructions for migrating the Child Health Monitor from BLE + Room to WiFi + Firestore.

**Estimated Time:** 4-6 hours
**Difficulty:** Intermediate
**Prerequisites:** Android Studio, Firebase account, ESP32 device

---

## 🎯 MIGRATION STRATEGY

**Approach:** Phased migration with backward compatibility

**Phases:**
1. **Phase 1:** Add Firestore (keep BLE + Room)
2. **Phase 2:** Add WiFi (dual mode: BLE + WiFi)
3. **Phase 3:** Test and validate
4. **Phase 4:** Remove BLE + Room (optional)

---

## 📦 PHASE 1: FIRESTORE INTEGRATION

### Step 1.1: Update Dependencies

**File:** `app/build.gradle.kts`

```kotlin
dependencies {
    // Existing dependencies...
    
    // ADD: WebSocket client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // ADD: JSON parsing (if not already present)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // KEEP: Firestore (already present)
    implementation("com.google.firebase:firebase-firestore")
    
    // KEEP: Room (for backward compatibility)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
}
```

**Action:**
```bash
# Sync Gradle
./gradlew clean build
```

---

### Step 1.2: Add Internet Permission

**File:** `app/src/main/AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- ADD: Internet permission for WiFi -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- KEEP: Existing permissions -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <!-- ... -->
    
</manifest>
```

---

### Step 1.3: Deploy Firestore Security Rules

**Action:**
1. Open Firebase Console: https://console.firebase.google.com
2. Select your project
3. Navigate to: Firestore Database → Rules
4. Copy rules from `FIRESTORE_SECURITY_RULES.txt`
5. Click "Publish"

**Verify:**
```
Rules deployed successfully
```

---

### Step 1.4: Create Firestore Repository

**Action:**
```bash
# File already created: app/src/main/java/com/example/sensorycontrol/repository/FirestoreHealthRepository.java
# No action needed - file provided
```

**Verify:**
```bash
# Check file exists
ls app/src/main/java/com/example/sensorycontrol/repository/FirestoreHealthRepository.java
```

---

### Step 1.5: Test Firestore Integration

**Test Code:** Create `FirestoreTest.java`

```java
// Test Firestore write
FirestoreHealthRepository repo = new FirestoreHealthRepository();
HealthReading reading = new HealthReading(75, 98, 36.7f, true, true);
HealthStatus status = HealthStatus.evaluate(reading);
repo.insertHealthRecord(reading, status);

// Check Firebase Console
// Navigate to: Firestore Database → Data
// Verify: users/{your-uid}/health_records/{doc-id}
```

**Expected Result:**
- Document created in Firestore
- All fields present
- No errors in Logcat

---

## 📡 PHASE 2: WIFI INTEGRATION

### Step 2.1: Create WiFi Manager

**Action:**
```bash
# File already created: app/src/main/java/com/example/sensorycontrol/wifi/WifiHealthMonitorManager.java
# No action needed - file provided
```

---

### Step 2.2: Create WiFi ViewModel

**Action:**
```bash
# File already created: app/src/main/java/com/example/sensorycontrol/viewmodels/HealthMonitorViewModelWifi.java
# No action needed - file provided
```

---

### Step 2.3: Update Fragment to Use WiFi ViewModel

**File:** `app/src/main/java/com/example/sensorycontrol/fragments/ModernDashboardFragment.java`

**Changes:**

```java
// OLD:
// private HealthMonitorViewModel viewModel;

// NEW:
private HealthMonitorViewModelWifi viewModel;

@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    // OLD:
    // viewModel = new ViewModelProvider(this).get(HealthMonitorViewModel.class);
    
    // NEW:
    viewModel = new ViewModelProvider(this).get(HealthMonitorViewModelWifi.class);
    
    // Rest of the code remains the same
    observeViewModel();
}

// Add WiFi connection method
private void connectToDevice() {
    // Get IP address from user input or SharedPreferences
    String ipAddress = "192.168.1.100"; // TODO: Get from settings
    viewModel.connect(ipAddress);
}
```

---

### Step 2.4: Add WiFi Settings UI

**File:** `app/src/main/res/layout/fragment_settings.xml`

**Add WiFi Configuration Section:**

```xml
<!-- WiFi Device Configuration -->
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="16dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="2dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="WiFi Device"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginBottom="16dp" />
        
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="ESP32 IP Address"
            style="@style/Widget.Material3.TextInputLayout.OutlinedBox">
            
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/et_device_ip"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:inputType="text"
                android:text="192.168.1.100" />
            
        </com.google.android.material.textfield.TextInputLayout>
        
        <com.google.android.material.button.MaterialButton
            android:id="@+id/btn_test_connection"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:layout_marginTop="12dp"
            android:text="Test Connection"
            app:cornerRadius="12dp" />
        
    </LinearLayout>
    
</com.google.android.material.card.MaterialCardView>
```

**File:** `app/src/main/java/com/example/sensorycontrol/fragments/SettingsFragment.java`

**Add Connection Test:**

```java
private void setupWifiSettings() {
    TextInputEditText etDeviceIp = view.findViewById(R.id.et_device_ip);
    MaterialButton btnTestConnection = view.findViewById(R.id.btn_test_connection);
    
    // Load saved IP
    SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    String savedIp = prefs.getString("device_ip", "192.168.1.100");
    etDeviceIp.setText(savedIp);
    
    btnTestConnection.setOnClickListener(v -> {
        String ip = etDeviceIp.getText().toString();
        
        // Save IP
        prefs.edit().putString("device_ip", ip).apply();
        
        // Test connection
        testWifiConnection(ip);
    });
}

private void testWifiConnection(String ip) {
    // Show progress
    Toast.makeText(requireContext(), "Testing connection...", Toast.LENGTH_SHORT).show();
    
    // Test connection (implement in ViewModel)
    viewModel.connect(ip);
    
    // Observe connection state
    viewModel.getConnectionState().observe(getViewLifecycleOwner(), state -> {
        if (state == WifiHealthMonitorManager.ConnectionState.CONNECTED) {
            Toast.makeText(requireContext(), "Connection successful!", Toast.LENGTH_SHORT).show();
        } else if (state == WifiHealthMonitorManager.ConnectionState.ERROR) {
            Toast.makeText(requireContext(), "Connection failed!", Toast.LENGTH_SHORT).show();
        }
    });
}
```

---

## 🔌 PHASE 3: ESP32 FIRMWARE

### Step 3.1: Install Arduino IDE

**Action:**
1. Download Arduino IDE: https://www.arduino.cc/en/software
2. Install ESP32 board support:
   - File → Preferences
   - Additional Board Manager URLs: `https://dl.espressif.com/dl/package_esp32_index.json`
   - Tools → Board → Boards Manager
   - Search "ESP32" and install

---

### Step 3.2: Install Required Libraries

**Action:**
In Arduino IDE:
1. Sketch → Include Library → Manage Libraries
2. Install:
   - `WebSockets` by Markus Sattler
   - `ArduinoJson` by Benoit Blanchon
   - `MAX30105` by SparkFun
   - `Adafruit MLX90614` by Adafruit

---

### Step 3.3: Configure WiFi Credentials

**File:** `arduino/ESP32_WiFi_Health_Monitor.ino`

**Edit:**
```cpp
// WiFi credentials
const char* WIFI_SSID = "YOUR_WIFI_SSID";        // CHANGE THIS
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD"; // CHANGE THIS
```

---

### Step 3.4: Upload Firmware

**Action:**
1. Connect ESP32 via USB
2. Tools → Board → ESP32 Dev Module
3. Tools → Port → (select your ESP32 port)
4. Click Upload button
5. Wait for "Done uploading"

**Verify:**
```
Serial Monitor (115200 baud):
=== ESP32 WiFi Health Monitor ===
Connecting to WiFi: YOUR_SSID
WiFi connected!
IP Address: 192.168.1.100
WebSocket server: ws://192.168.1.100:8080/health
Setup complete!
```

---

### Step 3.5: Test ESP32

**Action:**
1. Open Serial Monitor (115200 baud)
2. Place finger on MAX30102
3. Point MLX90614 at forehead
4. Observe JSON output

**Expected:**
```json
Sent: {"heartRate":75,"spo2":98,"temperature":36.7,"timestamp":12345,"valid":{"hr":true,"temp":true}}
```

---

## 🧪 PHASE 4: TESTING

### Step 4.1: Test WiFi Connection

**Action:**
1. Open Android app
2. Navigate to Settings
3. Enter ESP32 IP address
4. Click "Test Connection"

**Expected:**
- Toast: "Connection successful!"
- Connection state: CONNECTED

---

### Step 4.2: Test Real-Time Data

**Action:**
1. Navigate to Dashboard
2. Click "Connect Device"
3. Observe real-time updates

**Expected:**
- Heart rate updates every second
- SpO2 updates every second
- Temperature updates every second
- Status dot changes color
- Animations play

---

### Step 4.3: Test Firestore Storage

**Action:**
1. Let app run for 30 seconds
2. Open Firebase Console
3. Navigate to Firestore Database
4. Check: `users/{your-uid}/health_records`

**Expected:**
- 3 documents created (every 10 seconds)
- All fields present
- Timestamps correct

---

### Step 4.4: Test Offline Mode

**Action:**
1. Enable airplane mode
2. Navigate to History
3. Verify charts display

**Expected:**
- Data loads from cache
- Charts render correctly
- No errors

---

### Step 4.5: Test Charts

**Action:**
1. Generate 24 hours of data
2. Navigate to History
3. Select time ranges

**Expected:**
- Charts display correctly
- Data points accurate
- Zoom/pan works

---

## 🔄 PHASE 5: DATA MIGRATION (Optional)

### Step 5.1: Export Room Data

**Code:** Add to SettingsFragment

```java
private void exportRoomToFirestore() {
    // Get all Room records
    HealthDataRepository roomRepo = new HealthDataRepository(requireActivity().getApplication());
    roomRepo.getAllRecords().observe(getViewLifecycleOwner(), records -> {
        if (records == null || records.isEmpty()) {
            Toast.makeText(requireContext(), "No data to migrate", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Migrate to Firestore
        FirestoreHealthRepository firestoreRepo = new FirestoreHealthRepository();
        int count = 0;
        
        for (HealthRecordEntity record : records) {
            // Convert to HealthReading
            HealthReading reading = new HealthReading(
                record.getHeartRate(),
                record.getSpO2(),
                (float) record.getTemperature(),
                true,
                true
            );
            reading.setTimestamp(record.getTimestamp());
            
            // Convert to HealthStatus
            HealthStatus status = new HealthStatus();
            // Set status based on record.getHealthStatus()
            
            // Insert to Firestore
            firestoreRepo.insertHealthRecord(reading, status);
            count++;
        }
        
        Toast.makeText(requireContext(), 
            "Migrated " + count + " records to Firestore", 
            Toast.LENGTH_LONG).show();
    });
}
```

---

### Step 5.2: Verify Migration

**Action:**
1. Click "Migrate Data" button
2. Wait for completion
3. Check Firestore Console

**Expected:**
- All Room records in Firestore
- Data integrity maintained
- No duplicates

---

## 🗑️ PHASE 6: CLEANUP (Optional)

### Step 6.1: Remove BLE Dependencies

**File:** `app/build.gradle.kts`

```kotlin
dependencies {
    // REMOVE: (after confirming WiFi works)
    // implementation(libs.room.runtime)
    // annotationProcessor(libs.room.compiler)
}
```

---

### Step 6.2: Remove BLE Code

**Action:**
```bash
# Mark as deprecated (don't delete yet)
# app/src/main/java/com/example/sensorycontrol/ble/HealthMonitorBleManager.java
# app/src/main/java/com/example/sensorycontrol/database/
# app/src/main/java/com/example/sensorycontrol/repository/HealthDataRepository.java
```

**Add @Deprecated annotation:**
```java
@Deprecated
public class HealthMonitorBleManager {
    // Keep for 30 days, then remove
}
```

---

### Step 6.3: Remove BLE Permissions

**File:** `app/src/main/AndroidManifest.xml`

```xml
<!-- REMOVE: (after confirming WiFi works) -->
<!-- <uses-permission android:name="android.permission.BLUETOOTH" /> -->
<!-- <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" /> -->
<!-- <uses-permission android:name="android.permission.BLUETOOTH_SCAN" /> -->
<!-- <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" /> -->
```

---

## ✅ VERIFICATION CHECKLIST

### Pre-Migration
- [ ] Backup Room database
- [ ] Export data to CSV
- [ ] Firebase project configured
- [ ] Firestore security rules deployed
- [ ] ESP32 device ready

### Post-Migration
- [ ] WiFi connection works
- [ ] Real-time data updates
- [ ] Firestore storage working
- [ ] Charts display correctly
- [ ] CSV export functional
- [ ] Offline mode works
- [ ] Multi-user isolation verified
- [ ] Performance acceptable
- [ ] Battery drain acceptable
- [ ] Firestore costs within budget

---

## 🚨 TROUBLESHOOTING

### Issue 1: WiFi Connection Fails

**Symptoms:**
- Connection state: ERROR
- Toast: "Connection failed"

**Solutions:**
1. Verify ESP32 IP address
2. Check WiFi network (same network?)
3. Ping ESP32: `ping 192.168.1.100`
4. Check ESP32 Serial Monitor
5. Restart ESP32
6. Check firewall settings

---

### Issue 2: No Data in Firestore

**Symptoms:**
- Firestore collection empty
- No documents created

**Solutions:**
1. Check Firebase Auth (user logged in?)
2. Verify security rules
3. Check Logcat for errors
4. Test with Firebase Console
5. Verify internet connection

---

### Issue 3: Charts Not Displaying

**Symptoms:**
- Empty charts
- No data points

**Solutions:**
1. Check Firestore data exists
2. Verify time range selection
3. Check query filters
4. Test with smaller dataset
5. Check Logcat for errors

---

### Issue 4: High Firestore Costs

**Symptoms:**
- Unexpected charges
- High write count

**Solutions:**
1. Check write frequency (should be 0.1 Hz)
2. Verify batching strategy
3. Check for duplicate writes
4. Monitor Firebase Console
5. Adjust STORAGE_INTERVAL_MS

---

### Issue 5: ESP32 Disconnects

**Symptoms:**
- Frequent disconnections
- Reconnection attempts

**Solutions:**
1. Check WiFi signal strength
2. Increase heartbeat interval
3. Check power supply
4. Update ESP32 firmware
5. Check router settings

---

## 📞 SUPPORT

### Resources
- Migration Plan: `WIFI_FIRESTORE_MIGRATION_PLAN.md`
- Testing Guide: `MIGRATION_TESTING_GUIDE.md`
- Firestore Rules: `FIRESTORE_SECURITY_RULES.txt`
- ESP32 Code: `arduino/ESP32_WiFi_Health_Monitor.ino`

### Firebase Documentation
- Firestore: https://firebase.google.com/docs/firestore
- Security Rules: https://firebase.google.com/docs/firestore/security/get-started
- Pricing: https://firebase.google.com/pricing

### ESP32 Documentation
- Arduino Core: https://github.com/espressif/arduino-esp32
- WebSockets: https://github.com/Links2004/arduinoWebSockets
- ArduinoJson: https://arduinojson.org/

---

## 🎉 SUCCESS CRITERIA

**Migration Complete When:**
1. ✅ WiFi connection stable
2. ✅ Real-time data flowing
3. ✅ Firestore storage working
4. ✅ All UI features functional
5. ✅ Charts displaying correctly
6. ✅ CSV export working
7. ✅ Offline mode functional
8. ✅ Multi-user isolation verified
9. ✅ Performance acceptable
10. ✅ Costs within budget

**Congratulations! Migration complete! 🎊**

---

*Last Updated: Current Session*
*Status: Migration Guide Complete*
*Estimated Time: 4-6 hours*
