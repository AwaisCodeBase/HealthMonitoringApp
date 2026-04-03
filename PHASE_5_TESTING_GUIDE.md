# Phase 5: Testing Guide

## Complete Testing Procedure for BLE Health Monitoring

---

## 🔧 Pre-Testing Setup

### Arduino Setup (Phase 3)
1. Upload `PHASE_3_BLE_IMPLEMENTATION.ino` to Arduino Nano 33 BLE
2. Open Serial Monitor (115200 baud)
3. Verify output:
   ```
   =================================
   PHASE 3: BLE Communication
   =================================
   
   ✓ Sensors initialized successfully
   ✓ BLE initialized successfully
   
   BLE Advertising started
   Device Name: ChildHealthWearable
   ```
4. Place finger on MAX30102 sensor
5. Verify readings appear in Serial Monitor

### Android Setup
1. Build and install app
2. Login with test account
3. Grant Bluetooth permissions when prompted
4. Ensure Bluetooth is enabled

---

## 📱 Test Cases

### TC-01: Initial State

**Objective**: Verify dashboard initial state

**Steps**:
1. Login to app
2. Navigate to Dashboard (should be default)

**Expected Result**:
- [ ] Welcome message shows user name
- [ ] Connection status: "Not connected" (red)
- [ ] Three dots are gray
- [ ] Health status: "Waiting for data..."
- [ ] Heart rate: "--"
- [ ] SpO2: "--"
- [ ] Temperature: "--"
- [ ] Connect button enabled
- [ ] Disconnect button disabled

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-02: BLE Scanning

**Objective**: Test device scanning

**Steps**:
1. Ensure Arduino is powered on and advertising
2. Click "Connect to Device" button

**Expected Result**:
- [ ] Toast message: "Scanning for device..."
- [ ] Connection status: "Scanning..." (orange)
- [ ] Connect button disabled
- [ ] Disconnect button disabled
- [ ] Scan completes within 10 seconds

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-03: Device Connection

**Objective**: Test BLE connection establishment

**Prerequisites**: Device found during scan

**Expected Result**:
- [ ] Connection status: "Connecting..." (orange)
- [ ] Connection status: "Connected" (green)
- [ ] Connect button disabled
- [ ] Disconnect button enabled
- [ ] Arduino Serial Monitor shows: ">>> BLE Device Connected"

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-04: Real-Time Data Reception

**Objective**: Verify live data updates

**Prerequisites**: Device connected, finger on sensor

**Steps**:
1. Place finger firmly on MAX30102 sensor
2. Wait 5-10 seconds for stabilization
3. Observe dashboard

**Expected Result**:
- [ ] Heart rate updates every ~1 second
- [ ] SpO2 updates every ~1 second
- [ ] Temperature updates every ~1 second
- [ ] Values are numeric (not "--")
- [ ] Values are reasonable:
  - HR: 40-200 BPM
  - SpO2: 70-100%
  - Temp: 30-42°C
- [ ] Arduino Serial Monitor shows same values

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-05: No Finger Detection

**Objective**: Test invalid data handling

**Prerequisites**: Device connected

**Steps**:
1. Remove finger from sensor
2. Wait 2 seconds
3. Observe dashboard

**Expected Result**:
- [ ] Heart rate changes to "--"
- [ ] SpO2 changes to "--"
- [ ] Temperature may still show value (if valid)
- [ ] Health status based on valid data only
- [ ] Arduino Serial Monitor shows "No finger detected"

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-06: Health Status - Good Condition

**Objective**: Test green indicator for normal vitals

**Prerequisites**: Device connected, finger on sensor

**Test Data** (simulate or wait for normal readings):
- Heart Rate: 60-120 BPM
- SpO2: ≥95%
- Temperature: 36.0-37.5°C

**Expected Result**:
- [ ] Three dots are GREEN
- [ ] Dots are SOLID (not blinking)
- [ ] Health status text: "All vitals normal"
- [ ] Status text color: green

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-07: Health Status - Warning Condition

**Objective**: Test yellow indicator for borderline vitals

**Test Data** (simulate):
- Heart Rate: 55 BPM (below 60) OR 125 BPM (above 120)
- SpO2: 92% (below 95%)
- Temperature: 37.8°C (above 37.5°C)

**Expected Result**:
- [ ] Three dots are YELLOW
- [ ] Dots are SOLID (not blinking)
- [ ] Health status text: "Attention needed"
- [ ] Status text color: orange

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-08: Health Status - Critical Condition

**Objective**: Test red blinking indicator for critical vitals

**Test Data** (simulate):
- Heart Rate: <40 BPM OR >150 BPM
- SpO2: <90%
- Temperature: ≥38.5°C

**Expected Result**:
- [ ] Three dots are RED
- [ ] Dots are BLINKING (fade in/out animation)
- [ ] Health status text: "Critical condition!"
- [ ] Status text color: red
- [ ] Animation is smooth (500ms cycle)
- [ ] Animation repeats continuously

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-09: Blinking Animation Details

**Objective**: Verify animation behavior

**Prerequisites**: Critical condition (red dots)

**Observations**:
- [ ] Dots fade from visible to invisible
- [ ] Fade out duration: ~500ms
- [ ] Dots fade from invisible to visible
- [ ] Fade in duration: ~500ms
- [ ] Total cycle: ~1 second
- [ ] Animation repeats infinitely
- [ ] All three dots blink in sync
- [ ] Animation is smooth (no jitter)

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-10: Status Transition

**Objective**: Test status changes

**Steps**:
1. Start with normal vitals (green)
2. Simulate warning condition (yellow)
3. Simulate critical condition (red)
4. Return to normal (green)

**Expected Result**:
- [ ] Dots change color immediately
- [ ] Blinking starts when critical
- [ ] Blinking stops when no longer critical
- [ ] Status text updates correctly
- [ ] No animation artifacts

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-11: Disconnection

**Objective**: Test manual disconnect

**Prerequisites**: Device connected

**Steps**:
1. Click "Disconnect" button

**Expected Result**:
- [ ] Connection status: "Disconnecting..." (orange)
- [ ] Connection status: "Not connected" (red)
- [ ] All values reset to "--"
- [ ] Three dots turn gray
- [ ] Health status: "Waiting for data..."
- [ ] Blinking animation stops (if was blinking)
- [ ] Connect button enabled
- [ ] Disconnect button disabled
- [ ] Arduino Serial Monitor shows: "<<< BLE Device Disconnected"

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-12: Auto-Reconnection

**Objective**: Test automatic reconnection

**Prerequisites**: Device connected

**Steps**:
1. Turn off Arduino OR move out of BLE range
2. Wait 2 seconds
3. Turn on Arduino OR move back in range
4. Wait up to 10 seconds

**Expected Result**:
- [ ] Connection status: "Not connected"
- [ ] Automatic reconnection attempt starts
- [ ] Connection status: "Connecting..."
- [ ] Connection re-established
- [ ] Connection status: "Connected"
- [ ] Data resumes updating
- [ ] Up to 3 reconnection attempts if needed

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-13: Reconnection Failure

**Objective**: Test reconnection limit

**Prerequisites**: Device connected

**Steps**:
1. Turn off Arduino
2. Wait for 3 reconnection attempts (~6 seconds)
3. Keep Arduino off

**Expected Result**:
- [ ] 3 reconnection attempts made
- [ ] Connection status: "Not connected"
- [ ] No more reconnection attempts
- [ ] Toast or error message shown
- [ ] User can manually reconnect

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-14: Bluetooth Disabled

**Objective**: Test Bluetooth off handling

**Steps**:
1. Disable Bluetooth on Android device
2. Click "Connect to Device"

**Expected Result**:
- [ ] Toast message: "Please enable Bluetooth"
- [ ] No scan starts
- [ ] Connection status remains "Not connected"

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-15: Permissions Denied

**Objective**: Test missing permissions

**Steps**:
1. Deny Bluetooth permissions
2. Click "Connect to Device"

**Expected Result**:
- [ ] Error message about missing permissions
- [ ] No scan starts
- [ ] User prompted to grant permissions

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-16: Device Not Found

**Objective**: Test scan timeout

**Prerequisites**: Arduino off or not advertising

**Steps**:
1. Ensure Arduino is off
2. Click "Connect to Device"
3. Wait 10 seconds

**Expected Result**:
- [ ] Scan runs for 10 seconds
- [ ] Toast message: "Device not found. Make sure the wearable is powered on."
- [ ] Connection status: "Not connected"
- [ ] Connect button enabled

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-17: Multiple Connect Attempts

**Objective**: Test rapid button clicks

**Steps**:
1. Click "Connect to Device" multiple times rapidly

**Expected Result**:
- [ ] Only one scan starts
- [ ] Button disabled during scan
- [ ] No crashes or errors
- [ ] Scan completes normally

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-18: Screen Rotation

**Objective**: Test configuration changes

**Prerequisites**: Device connected, data updating

**Steps**:
1. Rotate device (portrait ↔ landscape)
2. Observe dashboard

**Expected Result**:
- [ ] Connection maintained
- [ ] Data continues updating
- [ ] No reconnection needed
- [ ] UI recreated correctly
- [ ] ViewModel survives rotation

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-19: App Backgrounding

**Objective**: Test app lifecycle

**Prerequisites**: Device connected

**Steps**:
1. Press Home button (background app)
2. Wait 5 seconds
3. Return to app

**Expected Result**:
- [ ] Connection maintained
- [ ] Data continues updating
- [ ] No reconnection needed
- [ ] UI updates resume

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-20: Logout While Connected

**Objective**: Test logout with active connection

**Prerequisites**: Device connected

**Steps**:
1. Click "Logout" button

**Expected Result**:
- [ ] Device disconnects automatically
- [ ] User signed out
- [ ] Navigate to LoginActivity
- [ ] Cannot return without login
- [ ] Arduino shows disconnection

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-21: Data Accuracy

**Objective**: Verify data matches Arduino output

**Prerequisites**: Device connected, finger on sensor

**Steps**:
1. Observe Arduino Serial Monitor
2. Observe Android dashboard
3. Compare values

**Expected Result**:
- [ ] Heart rate matches (±1 BPM)
- [ ] SpO2 matches (±1%)
- [ ] Temperature matches (±0.1°C)
- [ ] Validity flags match
- [ ] Update rate is ~1 second

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-22: Long-Term Stability

**Objective**: Test extended operation

**Prerequisites**: Device connected

**Steps**:
1. Leave app running for 5 minutes
2. Keep finger on sensor
3. Observe for issues

**Expected Result**:
- [ ] Connection remains stable
- [ ] Data updates continuously
- [ ] No memory leaks
- [ ] No crashes
- [ ] UI remains responsive
- [ ] Animation continues smoothly (if critical)

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-23: Edge Case - Invalid Packet

**Objective**: Test malformed data handling

**Note**: This requires modifying Arduino to send invalid data

**Expected Result**:
- [ ] App doesn't crash
- [ ] Invalid data ignored
- [ ] Previous valid data retained
- [ ] Error logged (check Logcat)

**Status**: ⬜ Pass | ⬜ Fail | ⬜ N/A

---

### TC-24: UI Responsiveness

**Objective**: Test UI performance

**Prerequisites**: Device connected, data updating

**Observations**:
- [ ] UI updates are smooth
- [ ] No lag or stuttering
- [ ] Scrolling is smooth
- [ ] Button clicks are responsive
- [ ] Animation is smooth
- [ ] No frame drops

**Status**: ⬜ Pass | ⬜ Fail

---

## 📊 Test Summary

### Overall Results

**Total Test Cases**: 24  
**Passed**: ___  
**Failed**: ___  
**N/A**: ___  

**Pass Rate**: ____%

### Critical Issues Found

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

### Minor Issues Found

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

---

## 🔍 Debugging Tips

### Check Arduino Serial Monitor
```
Expected output:
- "BLE Advertising started"
- ">>> BLE Device Connected"
- Sensor readings every 2 seconds
- "<<< BLE Device Disconnected" on disconnect
```

### Check Android Logcat
```bash
# Filter for app logs
adb logcat | grep "sensorycontrol"

# Filter for BLE logs
adb logcat | grep "HealthMonitorBle"

# Filter for Timber logs
adb logcat | grep "Timber"
```

### Common Issues

**Issue**: Device not found
- Ensure Arduino is powered on
- Check Arduino Serial Monitor for "BLE Advertising started"
- Verify device name is "ChildHealthWearable"
- Try restarting Arduino

**Issue**: Connection fails
- Check Bluetooth permissions
- Ensure Bluetooth is enabled
- Try restarting Bluetooth
- Check Arduino is not connected to another device

**Issue**: No data received
- Check finger is on sensor
- Verify Arduino Serial Monitor shows readings
- Check notifications are enabled
- Try reconnecting

**Issue**: Wrong values
- Verify Arduino data format (6 bytes)
- Check little-endian parsing
- Compare with Arduino Serial Monitor
- Check validity flags

**Issue**: Animation not working
- Verify critical condition is triggered
- Check animation setup in onViewCreated
- Ensure animation not stopped prematurely
- Check view references are not null

---

## ✅ Acceptance Criteria

Phase 5 is considered complete when:

- [ ] All critical test cases pass (TC-01 to TC-12)
- [ ] BLE connection works reliably
- [ ] Real-time data displays correctly
- [ ] Health status evaluates correctly
- [ ] Three-dot indicator works (all colors)
- [ ] Blinking animation works for critical
- [ ] No crashes during normal usage
- [ ] UI is responsive and smooth
- [ ] Disconnection and reconnection work
- [ ] Logout works correctly

---

## 📝 Testing Notes

**Tester Name**: _______________  
**Date**: _______________  
**Device**: _______________  
**Android Version**: _______________  
**App Version**: _______________  
**Arduino Version**: Phase 3  

**Additional Comments**:
_______________________________________________
_______________________________________________
_______________________________________________

---

**Testing Status**: ⬜ Not Started | ⬜ In Progress | ⬜ Complete  
**Phase 5 Status**: ⬜ Pass | ⬜ Fail
