# Health Monitor App - Setup Guide

## Quick Start Guide

### Prerequisites
- Android Studio (latest version recommended)
- Android device or emulator with API 21+
- Arduino Nano 33 BLE (or similar) for hardware testing
- Basic knowledge of Android development

---

## Step 1: Project Setup

### 1.1 Open Project in Android Studio
```bash
1. Open Android Studio
2. File → Open → Select project folder
3. Wait for Gradle sync to complete
```

### 1.2 Verify Dependencies
The project uses Gradle version catalog. All dependencies are defined in:
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`

If sync fails, try:
```bash
File → Invalidate Caches / Restart
```

### 1.3 Update Package Name (Optional)
If you want to change the package name from `com.example.healthmonitor`:

1. Right-click package → Refactor → Rename
2. Update in `AndroidManifest.xml`
3. Update in `build.gradle.kts`

---

## Step 2: Testing Without Hardware

### 2.1 Using BLE Simulator Apps

**Option 1: nRF Connect (Recommended)**
1. Install "nRF Connect" from Play Store
2. Open app → Advertiser tab
3. Create new advertising packet:
   - Name: "HealthMonitor"
   - Service UUID: `0000180d-0000-1000-8000-00805f9b34fb`
4. Add characteristic:
   - UUID: `00002a37-0000-1000-8000-00805f9b34fb`
   - Properties: Read, Notify
   - Value: `HR:75,SpO2:98,Temp:36.5`
5. Start advertising

**Option 2: BLE Peripheral Simulator**
1. Install "BLE Peripheral Simulator" from Play Store
2. Configure service and characteristic with UUIDs above
3. Set data format as string

### 2.2 Testing on Emulator
**Note:** BLE doesn't work on Android emulators. You must use a physical device.

---

## Step 3: Hardware Setup (Arduino)

### 3.1 Arduino Requirements
- Arduino Nano 33 BLE (or compatible)
- USB cable
- Arduino IDE installed

### 3.2 Install Arduino Libraries
```
1. Open Arduino IDE
2. Tools → Manage Libraries
3. Install:
   - ArduinoBLE (by Arduino)
   - MAX30105 (optional, for real sensors)
   - DallasTemperature (optional, for temp sensor)
```

### 3.3 Upload Arduino Code
```
1. Open ARDUINO_SAMPLE_CODE.ino
2. Select board: Tools → Board → Arduino Nano 33 BLE
3. Select port: Tools → Port → (your Arduino port)
4. Click Upload
5. Open Serial Monitor (9600 baud) to see status
```

### 3.4 Verify Arduino is Working
Serial Monitor should show:
```
Health Monitor - BLE Device
============================
BLE device is now advertising...
Waiting for connections...
```

---

## Step 4: Running the Android App

### 4.1 Connect Android Device
```
1. Enable Developer Options on Android device
2. Enable USB Debugging
3. Connect via USB
4. Allow USB debugging prompt
```

### 4.2 Build and Run
```
1. In Android Studio: Run → Run 'app'
2. Select your device
3. Wait for installation
```

### 4.3 Grant Permissions
On first launch, the app will request:
- Bluetooth permissions
- Location permission (Android < 12)
- Notification permission (Android 13+)

**Grant all permissions** for the app to function.

### 4.4 Enable Bluetooth
If Bluetooth is off, the app will prompt you to enable it.

---

## Step 5: Connecting to Device

### 5.1 Scan for Devices
```
1. Open app
2. Tap "Scan" tab at bottom
3. Tap "Start Scan" button
4. Wait for devices to appear
```

### 5.2 Connect
```
1. Find "HealthMonitor" in the list
2. Tap on it
3. App will navigate to Monitor screen
4. Connection status should show "Connected"
```

### 5.3 View Data
You should now see:
- Real-time heart rate
- SpO2 percentage
- Temperature
- Historical chart updating

---

## Step 6: Testing Alert System

### 6.1 Configure Thresholds
```
1. Tap "Settings" tab
2. Modify thresholds (e.g., set HR max to 80)
3. Tap "Save Thresholds"
```

### 6.2 Trigger Alerts
**Method 1: Modify Arduino Code**
```cpp
void updateVitalSigns() {
  heartRate = 150;  // Above threshold
  spO2 = 88;        // Below threshold
  temperature = 39.0; // Above threshold
}
```

**Method 2: Use nRF Connect**
Update characteristic value to:
```
HR:150,SpO2:88,Temp:39.0
```

### 6.3 Verify Alerts
- Notification should appear
- Device should vibrate
- Alert color should be red in app

---

## Step 7: Testing Background Operation

### 7.1 Background Test
```
1. Connect to device
2. Press Home button (app goes to background)
3. Check notification bar - should see "Health Monitor" service
4. Wait 30 seconds
5. Return to app - data should still be updating
```

### 7.2 Reconnection Test
```
1. Connect to device
2. Turn off Arduino or move out of range
3. App should show "Disconnected"
4. Turn on Arduino or move back in range
5. App should automatically reconnect
```

---

## Step 8: Viewing Historical Data

### 8.1 Chart View
The monitoring screen shows a line chart of heart rate history:
- Last 50 readings
- Scrollable and zoomable
- Updates in real-time

### 8.2 Database Inspection (Advanced)
To view stored data:
```
1. Android Studio → View → Tool Windows → Device File Explorer
2. Navigate to: /data/data/com.example.healthmonitor/databases/
3. Download health_monitor_db
4. Open with SQLite browser
```

---

## Troubleshooting

### Problem: App crashes on launch
**Solution:**
- Check Logcat for errors
- Verify all dependencies synced
- Clean and rebuild project

### Problem: Cannot find device during scan
**Solution:**
- Ensure Bluetooth is enabled
- Grant location permission (Android < 12)
- Check Arduino is advertising (Serial Monitor)
- Reduce distance between devices
- Restart both devices

### Problem: Connection fails
**Solution:**
- Check UUIDs match exactly
- Verify Arduino code uploaded correctly
- Try forgetting device in Bluetooth settings
- Restart app

### Problem: No data received
**Solution:**
- Check Serial Monitor - Arduino should show "Sent: HR:..."
- Verify characteristic has Notify property
- Check data format matches exactly
- Look for errors in Logcat

### Problem: Alerts not working
**Solution:**
- Check notification permission granted
- Verify thresholds are saved
- Check data is outside threshold range
- Wait for cooldown period (30 seconds)

### Problem: Service stops in background
**Solution:**
- Check battery optimization settings
- Disable battery optimization for app
- Verify foreground service notification shows

---

## Development Tips

### Debugging BLE
```java
// Enable verbose BLE logging
adb shell setprop log.tag.BluetoothGatt VERBOSE
adb logcat -s BluetoothGatt
```

### Viewing Logs
```java
// Filter by app package
adb logcat | grep "com.example.healthmonitor"

// Filter by Timber logs
adb logcat | grep "HealthMonitor"
```

### Testing Different Android Versions
- Test on Android 11 (API 30) - Location permission
- Test on Android 12 (API 31) - New BLE permissions
- Test on Android 13 (API 33) - Notification permission

### Performance Testing
- Monitor battery usage
- Check memory leaks with Profiler
- Test with poor BLE signal
- Test rapid connect/disconnect

---

## Building Release APK

### Step 1: Generate Keystore
```bash
keytool -genkey -v -keystore health-monitor.keystore \
  -alias health-monitor -keyalg RSA -keysize 2048 -validity 10000
```

### Step 2: Configure Signing
In `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("health-monitor.keystore")
            storePassword = "your-password"
            keyAlias = "health-monitor"
            keyPassword = "your-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### Step 3: Build
```
Build → Generate Signed Bundle / APK → APK
```

---

## Next Steps

### Enhancements to Consider
1. Add data export (CSV)
2. Implement cloud sync
3. Add multiple user profiles
4. Create web dashboard
5. Add medication reminders
6. Implement emergency contacts

### Learning Resources
- [Android BLE Guide](https://developer.android.com/guide/topics/connectivity/bluetooth-le)
- [Material Design 3](https://m3.material.io/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Navigation Component](https://developer.android.com/guide/navigation)

---

## Support

For issues or questions:
1. Check Logcat for error messages
2. Review PROJECT_DOCUMENTATION.md
3. Consult Android developer documentation
4. Search Stack Overflow with tag `android-bluetooth`

---

## Project Checklist

- [ ] Project opens in Android Studio
- [ ] Gradle sync successful
- [ ] App runs on physical device
- [ ] Permissions granted
- [ ] Can scan for devices
- [ ] Can connect to Arduino
- [ ] Data displays correctly
- [ ] Alerts trigger properly
- [ ] Background service works
- [ ] Chart displays data
- [ ] Settings save correctly
- [ ] Dark mode works
- [ ] App survives rotation
- [ ] Reconnection works

**Once all items checked, your project is ready for demonstration!**
