# Phase 5: Troubleshooting Guide

## Common Issues and Solutions

---

## 🔴 Connection Issues

### Issue: Device Not Found During Scan

**Symptoms**:
- Scan completes but no device found
- Toast message: "Device not found"
- Connection status stays "Not connected"

**Possible Causes**:
1. Arduino not powered on
2. Arduino not advertising
3. Wrong device name
4. Bluetooth permissions not granted
5. Location services disabled (Android < 12)

**Solutions**:

**Check Arduino**:
```bash
# Open Arduino Serial Monitor (115200 baud)
# Look for:
✓ BLE initialized successfully
BLE Advertising started
Device Name: ChildHealthWearable
```

**Check Android Permissions**:
```java
// Android 12+
BLUETOOTH_SCAN
BLUETOOTH_CONNECT

// Android 11 and below
ACCESS_FINE_LOCATION
```

**Check Bluetooth**:
- Settings → Bluetooth → Ensure enabled
- Try toggling Bluetooth off/on
- Restart Android device

**Check Device Name**:
```java
// In HealthMonitorBleConstants.java
DEVICE_NAME = "ChildHealthWearable"  // Must match Arduino
```

**Check Location Services** (Android < 12):
- Settings → Location → Ensure enabled
- Required for BLE scanning on older Android

---

### Issue: Connection Fails After Device Found

**Symptoms**:
- Device found during scan
- Connection status: "Connecting..."
- Connection never completes
- Eventually times out

**Possible Causes**:
1. Arduino already connected to another device
2. Bluetooth interference
3. Out of range
4. Arduino crashed

**Solutions**:

**Reset Arduino**:
1. Press reset button on Arduino
2. Wait for "BLE Advertising started" in Serial Monitor
3. Try connecting again

**Check Range**:
- Move Android device closer to Arduino (< 5 meters)
- Remove obstacles between devices

**Check Other Connections**:
- Disconnect Arduino from other apps (nRF Connect, etc.)
- Close other BLE apps on Android

**Restart Bluetooth**:
```
Settings → Bluetooth → Toggle off/on
```

---

### Issue: Connection Drops Frequently

**Symptoms**:
- Connects successfully
- Disconnects after few seconds
- Auto-reconnection attempts

**Possible Causes**:
1. Weak signal (too far)
2. Interference
3. Low battery (Arduino or Android)
4. Arduino firmware issue

**Solutions**:

**Check Signal Strength**:
- Move devices closer
- Remove metal objects between devices
- Avoid WiFi routers, microwaves

**Check Power**:
- Use USB power for Arduino (not battery)
- Charge Android device
- Check Arduino power LED is on

**Check Arduino Serial Monitor**:
```
Look for:
- Repeated disconnections
- Error messages
- Sensor failures
```

**Reduce Interference**:
- Turn off WiFi temporarily
- Move away from other BLE devices
- Avoid crowded 2.4GHz environments

---

## 📊 Data Issues

### Issue: No Data Received (Shows "--")

**Symptoms**:
- Connected successfully
- All values show "--"
- Health status: "Waiting for data..."

**Possible Causes**:
1. Notifications not enabled
2. No finger on sensor
3. Sensor not working
4. Data packet format mismatch

**Solutions**:

**Check Finger Placement**:
- Place finger firmly on MAX30102 sensor
- Wait 5-10 seconds for stabilization
- Ensure good contact

**Check Arduino Serial Monitor**:
```
Should show:
Time(ms)    HR(BPM)  SpO2(%)  Temp(°C)  BLE   Status
-----------------------------------------------------------------------
2000        72       98       36.3      CONN  OK
```

**Check Notifications**:
```java
// In HealthMonitorBleManager
// Verify enableNotifications() is called
// Check Logcat for "Notifications enabled successfully"
```

**Check Data Format**:
```java
// Packet must be exactly 6 bytes
if (data.length != 6) {
    // Invalid packet
}
```

---

### Issue: Wrong Data Values

**Symptoms**:
- Data received but values are incorrect
- Values don't match Arduino Serial Monitor
- Unrealistic values (HR: 0, 65535, etc.)

**Possible Causes**:
1. Byte order (endianness) error
2. Parsing error
3. Data corruption
4. Arduino firmware mismatch

**Solutions**:

**Verify Parsing**:
```java
// Correct parsing (little-endian)
int heartRate = ((data[1] & 0xFF) << 8) | (data[0] & 0xFF);

// WRONG (big-endian)
int heartRate = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
```

**Check Temperature Parsing**:
```java
// Correct (divide by 10)
int tempRaw = ((data[4] & 0xFF) << 8) | (data[3] & 0xFF);
float temperature = tempRaw / 10.0f;

// WRONG (no division)
float temperature = tempRaw;  // Will be 10x too large
```

**Compare with Arduino**:
```
Arduino Serial Monitor:
HR: 72 BPM, SpO2: 98%, Temp: 36.5°C

Android Dashboard:
Should match exactly (±1 for rounding)
```

**Check Validity Flags**:
```java
boolean hrValid = (data[5] & 0x01) != 0;
boolean tempValid = (data[5] & 0x02) != 0;

// Only display data if valid
if (hrValid) {
    // Show heart rate
}
```

---

### Issue: Data Updates Slowly or Freezes

**Symptoms**:
- Data updates every few seconds (not every second)
- UI freezes
- Values don't change

**Possible Causes**:
1. Main thread blocking
2. Too many UI updates
3. Memory leak
4. Arduino not sending data

**Solutions**:

**Check Arduino Update Rate**:
```cpp
// In Arduino firmware
#define BLE_UPDATE_INTERVAL   1000  // Should be 1000ms
```

**Check UI Thread**:
```java
// All UI updates should be on main thread
handler.post(() -> bleCallback.onHealthDataReceived(reading));
```

**Check Logcat for Errors**:
```bash
adb logcat | grep "ERROR"
adb logcat | grep "ANR"  # Application Not Responding
```

**Restart App**:
- Force stop app
- Clear app data (Settings → Apps → Sensory Control → Clear Data)
- Reinstall app

---

## 🎨 UI Issues

### Issue: Three Dots Don't Change Color

**Symptoms**:
- Dots stay gray
- Health status doesn't update
- Data is valid

**Possible Causes**:
1. Health status not evaluated
2. LiveData not observed
3. View references null
4. Color tint not applied

**Solutions**:

**Check LiveData Observer**:
```java
// In DashboardFragment.setupObservers()
viewModel.getHealthStatus().observe(getViewLifecycleOwner(), status -> {
    updateHealthIndicator(status);
});
```

**Check View References**:
```java
// In DashboardFragment.initializeViews()
healthDot1 = view.findViewById(R.id.health_dot_1);
healthDot2 = view.findViewById(R.id.health_dot_2);
healthDot3 = view.findViewById(R.id.health_dot_3);

// Verify not null
if (healthDot1 == null) {
    Log.e("Dashboard", "healthDot1 is null!");
}
```

**Check Color Application**:
```java
// Correct way to set color
int color = ContextCompat.getColor(requireContext(), status.getConditionColor());
healthDot1.setBackgroundTintList(ColorStateList.valueOf(color));
```

**Check Health Evaluation**:
```java
// In HealthMonitorViewModel
HealthStatus status = HealthStatus.evaluate(reading);
healthStatus.postValue(status);  // Must post to LiveData
```

---

### Issue: Blinking Animation Not Working

**Symptoms**:
- Critical condition detected
- Dots are red but not blinking
- Animation doesn't start

**Possible Causes**:
1. Animation not setup
2. Animation not started
3. View references null
4. Animation cleared prematurely

**Solutions**:

**Check Animation Setup**:
```java
// In DashboardFragment.setupBlinkAnimation()
blinkAnimation = new AlphaAnimation(1.0f, 0.0f);
blinkAnimation.setDuration(500);
blinkAnimation.setRepeatMode(Animation.REVERSE);
blinkAnimation.setRepeatCount(Animation.INFINITE);
```

**Check Animation Start**:
```java
// In updateHealthIndicator()
if (status.shouldBlink()) {
    startBlinkAnimation();
}
```

**Check View State**:
```java
// Verify views are visible
healthDot1.setVisibility(View.VISIBLE);
```

**Check Lifecycle**:
```java
// Animation should be setup in onViewCreated()
// Not in onCreate() or onCreateView()
```

---

### Issue: Animation Continues After Status Changes

**Symptoms**:
- Status changes from critical to good
- Dots still blinking
- Animation doesn't stop

**Possible Causes**:
1. Animation not stopped
2. stopBlinkAnimation() not called
3. Animation reference lost

**Solutions**:

**Check Stop Logic**:
```java
// In updateHealthIndicator()
stopBlinkAnimation();  // Always stop first

if (status.shouldBlink()) {
    startBlinkAnimation();  // Only start if critical
}
```

**Check stopBlinkAnimation()**:
```java
private void stopBlinkAnimation() {
    if (healthDot1 != null) {
        healthDot1.clearAnimation();
        healthDot2.clearAnimation();
        healthDot3.clearAnimation();
        
        // Reset alpha
        healthDot1.setAlpha(1.0f);
        healthDot2.setAlpha(1.0f);
        healthDot3.setAlpha(1.0f);
    }
}
```

---

### Issue: UI Not Updating

**Symptoms**:
- Data received (check Logcat)
- UI shows old values
- LiveData not triggering observers

**Possible Causes**:
1. Observer not setup
2. Wrong lifecycle owner
3. LiveData not posted
4. UI thread issue

**Solutions**:

**Check Observer Lifecycle**:
```java
// Use getViewLifecycleOwner() not this
viewModel.getHeartRate().observe(getViewLifecycleOwner(), hr -> {
    // Update UI
});
```

**Check LiveData Posting**:
```java
// In ViewModel, use postValue() not setValue()
heartRate.postValue(hr);  // Thread-safe
```

**Check UI Thread**:
```java
// Ensure UI updates on main thread
requireActivity().runOnUiThread(() -> {
    heartRateValue.setText(String.valueOf(hr));
});
```

---

## ⚙️ Permission Issues

### Issue: Permission Denied Errors

**Symptoms**:
- SecurityException in Logcat
- Scan fails immediately
- Connection fails

**Possible Causes**:
1. Permissions not granted
2. Permissions not requested
3. Wrong permissions for Android version

**Solutions**:

**Check Manifest**:
```xml
<!-- Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Android 11 and below -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**Request Runtime Permissions**:
```java
// In MainActivity
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    requestPermissions(new String[]{
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    }, REQUEST_CODE);
}
```

**Check Permission Status**:
```java
if (ContextCompat.checkSelfPermission(context, 
        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
    // Permission not granted
}
```

---

## 🔧 Build Issues

### Issue: Compilation Errors

**Symptoms**:
- Build fails
- Red underlines in code
- Cannot resolve symbol

**Solutions**:

**Sync Gradle**:
```
File → Sync Project with Gradle Files
```

**Clean and Rebuild**:
```
Build → Clean Project
Build → Rebuild Project
```

**Check Dependencies**:
```kotlin
// In app/build.gradle.kts
dependencies {
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.timber)
}
```

**Invalidate Caches**:
```
File → Invalidate Caches / Restart
```

---

## 📱 Device-Specific Issues

### Issue: Works on Emulator but Not Real Device

**Possible Causes**:
1. Emulator doesn't support BLE properly
2. Real device has different Android version
3. Permission differences

**Solutions**:
- Always test on real device for BLE
- Emulator BLE support is limited
- Check Android version compatibility

### Issue: Works on One Device but Not Another

**Possible Causes**:
1. Different Android versions
2. Different Bluetooth chipsets
3. Manufacturer-specific issues

**Solutions**:
- Check Android version (minSdk = 26)
- Test on multiple devices
- Check manufacturer-specific BLE issues

---

## 🐛 Debugging Tools

### Logcat Filters

```bash
# All app logs
adb logcat | grep "com.example.sensorycontrol"

# BLE logs
adb logcat | grep "HealthMonitorBle"

# ViewModel logs
adb logcat | grep "HealthMonitorViewModel"

# Timber logs
adb logcat | grep "Timber"

# Errors only
adb logcat *:E

# BLE system logs
adb logcat | grep "BluetoothGatt"
```

### Enable Bluetooth HCI Snoop Log

```
Settings → Developer Options → Enable Bluetooth HCI snoop log
```

This captures all BLE packets for analysis with Wireshark.

### Check BLE Service

```bash
# Check if BLE service is running
adb shell dumpsys bluetooth_manager
```

---

## ✅ Quick Fixes Checklist

When something doesn't work, try these in order:

1. [ ] Check Arduino Serial Monitor for errors
2. [ ] Restart Arduino
3. [ ] Toggle Bluetooth off/on
4. [ ] Force stop and restart app
5. [ ] Check Logcat for errors
6. [ ] Verify permissions granted
7. [ ] Move devices closer together
8. [ ] Restart Android device
9. [ ] Clean and rebuild project
10. [ ] Reinstall app

---

## 📞 Getting Help

### Information to Provide

When asking for help, include:

1. **Android Version**: Settings → About Phone
2. **Device Model**: e.g., Pixel 6, Samsung Galaxy S21
3. **App Version**: Check build.gradle.kts
4. **Arduino Output**: Copy from Serial Monitor
5. **Logcat Output**: Use filters above
6. **Steps to Reproduce**: Exact steps that cause issue
7. **Expected vs Actual**: What should happen vs what happens

### Useful Commands

```bash
# Get device info
adb shell getprop ro.build.version.release  # Android version
adb shell getprop ro.product.model          # Device model

# Get app info
adb shell dumpsys package com.example.sensorycontrol | grep version

# Clear app data
adb shell pm clear com.example.sensorycontrol

# Reinstall app
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎯 Prevention Tips

### Best Practices

1. **Always check Arduino first** - Verify it's working before debugging Android
2. **Use Logcat** - Add Timber logs for debugging
3. **Test incrementally** - Test each feature as you build
4. **Handle errors gracefully** - Don't crash on BLE errors
5. **Test on real device** - Emulator BLE is unreliable
6. **Keep devices close** - < 5 meters during development
7. **Use USB power** - More stable than battery
8. **Monitor Serial output** - Keep Arduino Serial Monitor open

### Code Quality

```java
// Always check for null
if (bluetoothGatt != null && hasPermissions()) {
    // Safe to use
}

// Always use try-catch for BLE operations
try {
    bluetoothGatt.discoverServices();
} catch (SecurityException e) {
    Timber.e(e, "Permission denied");
}

// Always post to LiveData from background thread
heartRate.postValue(hr);  // Not setValue()
```

---

**Remember**: Most BLE issues are related to permissions, range, or Arduino state. Always check these first!
