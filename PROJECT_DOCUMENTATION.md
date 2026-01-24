# Health Monitor Android App - BSc Final Year Project

## Project Overview

A complete Android health monitoring application that connects to an Arduino-based wearable device via Bluetooth Low Energy (BLE) to monitor vital signs in real-time.

**Target Users:** Caregivers and parents of children with medical needs  
**Platform:** Android (API 21 - API 34)  
**Language:** Pure Java (no Kotlin)

---

## Architecture

### Single Activity + Multiple Fragments Architecture

**Why this approach?**
- Modern Android best practice
- Better lifecycle management
- Easier state preservation across configuration changes
- Simplified navigation with Navigation Component
- Reduced memory overhead

### Design Patterns Used

1. **MVVM (Model-View-ViewModel)**
   - ViewModels handle business logic
   - LiveData for reactive UI updates
   - Clear separation of concerns

2. **Repository Pattern**
   - Centralized data management
   - Single source of truth
   - Abstracts data sources (BLE + Room database)

3. **Service Pattern**
   - Foreground Service maintains BLE connection
   - Survives app backgrounding
   - Automatic reconnection logic

---

## Project Structure

```
com.example.healthmonitor/
├── activities/
│   └── MainActivity.java              # Single host activity
├── fragments/
│   ├── MonitoringFragment.java        # Main dashboard
│   ├── ScanFragment.java              # Device scanning
│   └── SettingsFragment.java          # Threshold configuration
├── services/
│   └── BleService.java                # Foreground service for BLE
├── ble/
│   ├── BleManager.java                # Central BLE logic
│   └── BleConstants.java              # UUIDs and constants
├── models/
│   ├── VitalSigns.java                # Data model + Room entity
│   ├── Thresholds.java                # Alert thresholds
│   └── BleDevice.java                 # Scan result model
├── database/
│   ├── AppDatabase.java               # Room database
│   └── VitalSignsDao.java             # Data access object
├── viewmodels/
│   └── MonitoringViewModel.java       # ViewModel for monitoring
├── adapters/
│   └── DeviceAdapter.java             # RecyclerView adapter
├── utils/
│   ├── AlertManager.java              # Alert system
│   ├── ThresholdManager.java          # Threshold persistence
│   └── PermissionUtil.java            # Permission handling
└── HealthMonitorApplication.java      # Application class
```

---

## Core Features

### 1. BLE Communication

**Service UUID:** `0000180d-0000-1000-8000-00805f9b34fb`  
**Characteristic UUID:** `00002a37-0000-1000-8000-00805f9b34fb`

**Data Format (String):**
```
HR:85,SpO2:97,Temp:36.8
```

**Alternative Binary Format (if needed):**
```
Byte 0-1: Heart Rate (uint16)
Byte 2: SpO2 (uint8)
Byte 3-4: Temperature (float * 10, e.g., 368 = 36.8°C)
```

**Key BLE Features:**
- Automatic device scanning with RSSI filtering
- GATT connection management
- Notification-based data reception
- Sequential GATT operation queue
- Automatic reconnection with exponential backoff
- Proper permission handling for Android 12+

### 2. Real-Time Monitoring Dashboard

**Displays:**
- Heart Rate (BPM) - Large, color-coded display
- SpO2 (%) - Color-coded based on threshold
- Temperature (°C) - One decimal precision
- Connection status indicator
- Last update timestamp
- Historical line chart (last 50 readings)

**Color Coding:**
- Green: Normal range
- Red: Outside threshold (alert condition)

### 3. Alert System

**Features:**
- User-configurable thresholds
- Local push notifications
- Vibration feedback
- 30-second cooldown to prevent spam
- Hysteresis implementation

**Default Thresholds (for children):**
- Heart Rate: 60-140 BPM
- SpO2: ≥92%
- Temperature: 35.5-38.5°C

### 4. Background Operation

**Foreground Service:**
- Keeps BLE connection alive
- Persistent notification
- Automatic reconnection
- Data logging to database

**Reconnection Strategy:**
- Exponential backoff (2s, 4s, 8s, 16s, 30s max)
- Maximum 5 attempts
- User notification on failure

### 5. Data Persistence

**Room Database:**
- Stores all vital sign readings
- Timestamp-indexed
- Efficient queries for charts
- Automatic cleanup of old data

**SharedPreferences:**
- User threshold settings
- Last connected device
- Alert cooldown timestamps

---

## Permissions

### Android 12+ (API 31+)
- `BLUETOOTH_SCAN` - Device discovery
- `BLUETOOTH_CONNECT` - Connection management
- `POST_NOTIFICATIONS` - Alert notifications

### Android 11 and below
- `BLUETOOTH` - Classic Bluetooth
- `BLUETOOTH_ADMIN` - BLE operations
- `ACCESS_FINE_LOCATION` - Required for BLE scanning

### All versions
- `FOREGROUND_SERVICE` - Background operation
- `VIBRATE` - Alert feedback
- `INTERNET` - Future cloud features

---

## Key Classes Explained

### BleManager.java
Central BLE management class handling:
- Device scanning with callbacks
- GATT connection lifecycle
- Service discovery
- Characteristic notifications
- Data parsing
- Error handling
- Permission checks

**Important Methods:**
- `startScan()` - Begin device discovery
- `connect(BluetoothDevice)` - Connect to device
- `disconnect()` - Graceful disconnection
- `reconnect()` - Automatic reconnection
- `parseVitalSigns(byte[])` - Data parsing

### BleService.java
Foreground service that:
- Maintains BLE connection in background
- Saves data to Room database
- Triggers alerts via AlertManager
- Provides callbacks to UI
- Shows persistent notification

**Lifecycle:**
- Started by MainActivity
- Bound for communication
- Survives app backgrounding
- Cleaned up on app close

### MonitoringFragment.java
Main dashboard displaying:
- Real-time vital signs
- Connection status
- Historical chart (MPAndroidChart)
- Color-coded values
- Last update time

**Data Flow:**
1. BleService receives data
2. Saves to Room database
3. ViewModel observes LiveData
4. Fragment updates UI

### AlertManager.java
Handles alert logic:
- Threshold checking
- Notification creation
- Vibration control
- Cooldown management
- Alert history

**Cooldown Implementation:**
- Stores last alert time in SharedPreferences
- 30-second minimum between alerts per parameter
- Prevents notification spam

---

## Dependencies

```gradle
// AndroidX Core
implementation 'androidx.appcompat:appcompat:1.7.1'
implementation 'com.google.android.material:material:1.13.0'
implementation 'androidx.constraintlayout:constraintlayout:2.2.1'

// Navigation Component
implementation 'androidx.navigation:navigation-fragment:2.7.7'
implementation 'androidx.navigation:navigation-ui:2.7.7'

// Room Database
implementation 'androidx.room:room-runtime:2.6.1'
annotationProcessor 'androidx.room:room-compiler:2.6.1'

// Lifecycle Components
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'

// MPAndroidChart for graphs
implementation 'com.github.PhilJay:MPAndroidChart:3.1.0'

// Timber for logging
implementation 'com.jakewharton.timber:timber:5.0.1'
```

---

## Arduino Wearable Device - Expected Behavior

### Hardware Requirements
- Arduino board with BLE capability (e.g., Arduino Nano 33 BLE)
- MAX30102 sensor (Heart Rate + SpO2)
- DS18B20 or similar temperature sensor
- Battery power supply

### Arduino Code Structure (Pseudo-code)

```cpp
#include <ArduinoBLE.h>

// Define BLE Service and Characteristic
BLEService healthService("0000180d-0000-1000-8000-00805f9b34fb");
BLECharacteristic vitalSignsChar("00002a37-0000-1000-8000-00805f9b34fb", 
                                  BLERead | BLENotify, 50);

void setup() {
  BLE.begin();
  BLE.setLocalName("HealthMonitor");
  BLE.setAdvertisedService(healthService);
  healthService.addCharacteristic(vitalSignsChar);
  BLE.addService(healthService);
  BLE.advertise();
}

void loop() {
  BLEDevice central = BLE.central();
  
  if (central && central.connected()) {
    // Read sensors
    int heartRate = readHeartRate();
    int spO2 = readSpO2();
    float temperature = readTemperature();
    
    // Format data
    String data = "HR:" + String(heartRate) + 
                  ",SpO2:" + String(spO2) + 
                  ",Temp:" + String(temperature, 1);
    
    // Send via BLE
    vitalSignsChar.writeValue(data.c_str());
    
    delay(2000); // Send every 2 seconds
  }
}
```

---

## Testing Checklist

### BLE Functionality
- [ ] Device scanning works
- [ ] Can connect to device
- [ ] Data reception works
- [ ] Automatic reconnection works
- [ ] Handles Bluetooth off/on
- [ ] Handles device out of range

### UI/UX
- [ ] All fragments navigate correctly
- [ ] Real-time data updates
- [ ] Chart displays correctly
- [ ] Color coding works
- [ ] Dark mode supported
- [ ] Rotation handled properly

### Alerts
- [ ] Notifications appear
- [ ] Vibration works
- [ ] Cooldown prevents spam
- [ ] Threshold settings save
- [ ] Alert colors correct

### Permissions
- [ ] Runtime permissions requested
- [ ] Rationale shown
- [ ] Handles permission denial
- [ ] Works on Android 12+
- [ ] Works on Android 11 and below

### Background Operation
- [ ] Service starts correctly
- [ ] Connection maintained in background
- [ ] Data logged while backgrounded
- [ ] Foreground notification shows
- [ ] Service survives app kill

---

## Known Limitations & Future Enhancements

### Current Limitations
1. Single device connection only
2. No cloud sync
3. Limited historical data (device storage only)
4. No data export feature
5. Basic chart visualization

### Suggested Enhancements
1. **Cloud Integration**
   - Firebase Realtime Database
   - Multi-device sync
   - Web dashboard for caregivers

2. **Advanced Analytics**
   - Trend analysis
   - Predictive alerts
   - Health reports

3. **Multi-User Support**
   - Multiple patient profiles
   - Family sharing
   - Doctor access

4. **Enhanced Visualization**
   - Multiple chart types
   - Comparison views
   - Statistical summaries

5. **Data Export**
   - CSV export
   - PDF reports
   - Share with healthcare providers

---

## Troubleshooting

### BLE Connection Issues
**Problem:** Cannot find device  
**Solution:** 
- Ensure Bluetooth is enabled
- Check location permission (Android < 12)
- Verify device is advertising
- Check device name filter

**Problem:** Connection drops frequently  
**Solution:**
- Check signal strength (RSSI)
- Reduce distance to device
- Check for interference
- Verify battery level

### Permission Issues
**Problem:** Scan fails on Android 12+  
**Solution:**
- Request BLUETOOTH_SCAN permission
- Add neverForLocation flag in manifest
- Check runtime permission grant

### Data Issues
**Problem:** No data received  
**Solution:**
- Verify service UUID matches
- Check characteristic UUID
- Ensure notifications enabled
- Verify data format

---

## Deployment

### Release Build Steps
1. Update version code/name in build.gradle
2. Generate signed APK/AAB
3. Test on multiple devices
4. Submit to Play Store (optional)

### Play Store Requirements
- Privacy policy (if collecting data)
- App icon (512x512)
- Screenshots (phone + tablet)
- Feature graphic
- Short/full description
- Content rating

---

## Academic Considerations

### Report Sections to Include
1. **Introduction** - Problem statement, objectives
2. **Literature Review** - BLE technology, health monitoring
3. **System Design** - Architecture diagrams, UML
4. **Implementation** - Key code explanations
5. **Testing** - Test cases, results
6. **Evaluation** - Performance, usability
7. **Conclusion** - Achievements, limitations, future work

### Diagrams to Create
- System architecture diagram
- BLE communication flow
- Database schema
- Activity/Fragment lifecycle
- Alert system flowchart
- Use case diagram
- Sequence diagrams

---

## Credits & References

**Libraries Used:**
- MPAndroidChart by PhilJay
- Timber by Jake Wharton
- AndroidX Libraries by Google

**BLE Resources:**
- Android BLE Documentation
- Bluetooth SIG specifications

**Design:**
- Material Design 3 Guidelines
- Android UI/UX Best Practices

---

## License

This project is created for educational purposes as part of a BSc Final Year Project.

---

## Contact & Support

For questions or issues, refer to:
- Android BLE documentation
- Stack Overflow (android-bluetooth tag)
- Material Design guidelines

**Project Status:** Complete and ready for demonstration/submission
