# Phase 3: BLE Communication Implementation

## Overview
Phase 3 extends the stable Phase 2 sensor firmware with Bluetooth Low Energy (BLE) communication, enabling real-time wireless transmission of health data to an Android application.

## Implementation Summary

### What Was Added
1. **ArduinoBLE Library Integration** - BLE stack for Arduino Nano 33 BLE
2. **GATT Service Design** - Custom health monitoring service with sensor data characteristic
3. **Binary Data Protocol** - Compact 6-byte payload for efficient transmission
4. **Connection Management** - Automatic advertising, connection detection, and graceful disconnection
5. **Notification System** - 1Hz data updates sent only when connected
6. **Fault Tolerance** - Handles disconnections without crashing sensor loop

### What Was NOT Changed
- All Phase 2 sensor reading logic remains intact
- Filtering algorithms unchanged
- Validation ranges preserved
- Serial Monitor output enhanced (not replaced)

---

## BLE Architecture

### Service & Characteristic Design

```
Service UUID: 19B10000-E8F2-537E-4F6C-D104768A1214
└── Characteristic UUID: 19B10001-E8F2-537E-4F6C-D104768A1214
    ├── Properties: Read, Notify
    ├── Size: 6 bytes
    └── Update Rate: 1 Hz (1000ms interval)
```

### Data Packet Format (6 Bytes)

```
┌─────────┬─────────┬──────┬─────────┬─────────┬───────┐
│ Byte 0  │ Byte 1  │ Byte 2│ Byte 3  │ Byte 4  │ Byte 5│
├─────────┼─────────┼──────┼─────────┼─────────┼───────┤
│ HR Low  │ HR High │ SpO2 │Temp Low │Temp High│ Flags │
└─────────┴─────────┴──────┴─────────┴─────────┴───────┘
```

#### Field Descriptions

| Field | Bytes | Type | Range | Description |
|-------|-------|------|-------|-------------|
| Heart Rate | 0-1 | uint16 (LE) | 0-200 | BPM, little-endian |
| SpO2 | 2 | uint8 | 0-100 | Oxygen saturation % |
| Temperature | 3-4 | uint16 (LE) | 300-420 | Temp × 10 (e.g., 365 = 36.5°C) |
| Flags | 5 | uint8 | Bitmask | Bit 0: HR valid, Bit 1: Temp valid |

#### Example Packet

```
Raw bytes: [0x48, 0x00, 0x62, 0x6D, 0x01, 0x03]

Parsing:
- Heart Rate = 0x0048 = 72 BPM
- SpO2 = 0x62 = 98%
- Temperature = 0x016D = 365 → 36.5°C
- Flags = 0x03 = 0b00000011 → Both HR and Temp valid
```

---

## Code Structure

### Key Functions

#### `initBLE()`
Initializes BLE stack, creates service/characteristic, and starts advertising.

```cpp
bool initBLE() {
  if (!BLE.begin()) return false;
  
  BLE.setDeviceName("ChildHealthWearable");
  BLE.setLocalName("Child Health Monitor");
  BLE.setAdvertisedService(healthService);
  
  healthService.addCharacteristic(sensorDataChar);
  BLE.addService(healthService);
  
  BLE.advertise();
  return true;
}
```

#### `sendBLENotification()`
Packs sensor data into 6-byte format and sends notification.

```cpp
void sendBLENotification() {
  if (!sensorData.hrValid && !sensorData.tempValid) return;
  
  byte data[6];
  
  // Pack heart rate (little-endian)
  uint16_t hr = sensorData.hrValid ? sensorData.heartRate : 0;
  data[0] = hr & 0xFF;
  data[1] = (hr >> 8) & 0xFF;
  
  // Pack SpO2
  data[2] = sensorData.hrValid ? sensorData.spO2 : 0;
  
  // Pack temperature × 10 (little-endian)
  uint16_t temp = sensorData.tempValid ? (uint16_t)(sensorData.temperature * 10) : 0;
  data[3] = temp & 0xFF;
  data[4] = (temp >> 8) & 0xFF;
  
  // Pack validity flags
  data[5] = 0;
  if (sensorData.hrValid) data[5] |= 0x01;
  if (sensorData.tempValid) data[5] |= 0x02;
  
  sensorDataChar.writeValue(data, 6);
}
```

### Main Loop Integration

```cpp
void loop() {
  unsigned long currentTime = millis();
  
  // 1. Poll BLE events (non-blocking)
  BLE.poll();
  
  // 2. Check connection status
  BLEDevice central = BLE.central();
  if (central && !bleConnected) {
    bleConnected = true;
    Serial.println(">>> BLE Device Connected");
  } else if (!central && bleConnected) {
    bleConnected = false;
    Serial.println("<<< BLE Device Disconnected");
  }
  
  // 3. Read sensors (unchanged from Phase 2)
  if (currentTime - sensorData.lastHrSample >= HR_SAMPLE_INTERVAL) {
    readHeartRateSensor();
  }
  
  if (currentTime - sensorData.lastTempSample >= TEMP_SAMPLE_INTERVAL) {
    readTemperatureSensor();
  }
  
  // 4. Send BLE notification (only if connected)
  if (bleConnected && (currentTime - sensorData.lastBleUpdate >= BLE_UPDATE_INTERVAL)) {
    sendBLENotification();
  }
  
  // 5. Print to Serial Monitor
  if (currentTime - sensorData.lastPrint >= PRINT_INTERVAL) {
    printSensorData(currentTime);
  }
}
```

---

## Testing Procedure

### 1. Upload Firmware
```bash
# Using Arduino IDE:
# 1. Open PHASE_3_BLE_IMPLEMENTATION.ino
# 2. Select Board: Arduino Nano 33 BLE
# 3. Select Port: /dev/cu.usbmodem*
# 4. Click Upload
```

### 2. Verify Serial Output
Open Serial Monitor (115200 baud):

```
=================================
PHASE 3: BLE Communication
=================================

✓ Sensors initialized successfully
✓ BLE initialized successfully

Starting data acquisition and BLE advertising...

BLE Advertising started
Device Name: ChildHealthWearable
Waiting for Android connection...

Time(ms)    HR(BPM)  SpO2(%)  Temp(°C)  BLE   Status
-----------------------------------------------------------------------
2000        --       --       36.2      ADV   No finger detected
4000        72       98       36.3      ADV   OK
6000        74       98       36.3      ADV   OK

>>> BLE Device Connected: AA:BB:CC:DD:EE:FF

8000        73       98       36.4      CONN  OK
10000       72       98       36.4      CONN  OK
```

### 3. Test with nRF Connect App

#### Step 1: Scan for Device
1. Install **nRF Connect** (iOS/Android)
2. Open app and tap "SCAN"
3. Look for "Child Health Monitor" or "ChildHealthWearable"
4. Tap "CONNECT"

#### Step 2: Explore Service
1. Expand "Unknown Service" (19B10000...)
2. Find characteristic (19B10001...)
3. Tap the **↓** (download) icon to enable notifications

#### Step 3: Verify Data
You should see hex values updating every second:
```
48 00 62 6D 01 03
↓  ↓  ↓  ↓  ↓  ↓
HR    SpO2 Temp Flags
```

#### Step 4: Decode Values
Use the nRF Connect "Value" tab or manually decode:
- **Heart Rate**: Bytes 0-1 (little-endian) → 0x0048 = 72 BPM
- **SpO2**: Byte 2 → 0x62 = 98%
- **Temperature**: Bytes 3-4 (little-endian) → 0x016D = 365 → 36.5°C
- **Flags**: Byte 5 → 0x03 = both valid

### 4. Test Disconnection
1. Tap "DISCONNECT" in nRF Connect
2. Verify Serial Monitor shows: `<<< BLE Device Disconnected`
3. Verify device returns to advertising mode (BLE column shows "ADV")
4. Reconnect and verify notifications resume

---

## Android Integration Guide

### Parsing BLE Data in Android

```java
// In your BLE notification callback
@Override
public void onCharacteristicChanged(BluetoothGatt gatt, 
                                     BluetoothGattCharacteristic characteristic) {
    byte[] data = characteristic.getValue();
    
    if (data.length == 6) {
        // Parse heart rate (little-endian uint16)
        int heartRate = (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
        
        // Parse SpO2 (uint8)
        int spO2 = data[2] & 0xFF;
        
        // Parse temperature (little-endian uint16, divide by 10)
        int tempRaw = (data[4] & 0xFF) << 8 | (data[3] & 0xFF);
        float temperature = tempRaw / 10.0f;
        
        // Parse validity flags
        boolean hrValid = (data[5] & 0x01) != 0;
        boolean tempValid = (data[5] & 0x02) != 0;
        
        // Update UI
        if (hrValid) {
            updateHeartRate(heartRate);
            updateSpO2(spO2);
        }
        if (tempValid) {
            updateTemperature(temperature);
        }
    }
}
```

### Connection Flow

```java
// 1. Scan for device
BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
scanner.startScan(scanCallback);

// 2. Connect when found
device.connectGatt(context, false, gattCallback);

// 3. Discover services
gatt.discoverServices();

// 4. Enable notifications
BluetoothGattService service = gatt.getService(SERVICE_UUID);
BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_UUID);
gatt.setCharacteristicNotification(characteristic, true);

// 5. Enable descriptor for notifications
BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCCD_UUID);
descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
gatt.writeDescriptor(descriptor);
```

---

## Troubleshooting

### Issue: "BLE initialization failed"
**Cause**: ArduinoBLE library not installed or wrong board selected  
**Solution**:
1. Install ArduinoBLE via Library Manager
2. Verify board is "Arduino Nano 33 BLE" (not "Arduino Nano")

### Issue: Device not visible in scan
**Cause**: BLE not advertising or name mismatch  
**Solution**:
1. Check Serial Monitor for "BLE Advertising started"
2. Search for "Child Health Monitor" or "ChildHealthWearable"
3. Restart Arduino if needed

### Issue: Notifications not received
**Cause**: Descriptor not enabled on Android side  
**Solution**:
1. Ensure you write to CCCD descriptor (0x2902)
2. Verify `setCharacteristicNotification()` is called
3. Check characteristic has Notify property

### Issue: Data parsing errors
**Cause**: Byte order mismatch (endianness)  
**Solution**:
- Arduino sends little-endian (LSB first)
- Android: `(high << 8) | low` for uint16
- Example: `[0x48, 0x00]` → `(0x00 << 8) | 0x48` = 72

### Issue: Connection drops frequently
**Cause**: Power saving or distance  
**Solution**:
1. Keep devices within 5 meters
2. Disable Android battery optimization for your app
3. Check Arduino power supply (USB or battery)

---

## Performance Characteristics

| Metric | Value | Notes |
|--------|-------|-------|
| BLE Update Rate | 1 Hz | Configurable via `BLE_UPDATE_INTERVAL` |
| Packet Size | 6 bytes | Minimal overhead |
| Connection Latency | ~50ms | Typical for BLE |
| Range | 5-10m | Line of sight |
| Power Consumption | ~15mA | With sensors active |
| Reconnection Time | ~2s | Automatic |

---

## Next Steps (Phase 4)

### Android App Development
1. **BLE Scanner** - Discover and list nearby devices
2. **Connection Manager** - Handle connect/disconnect/reconnect
3. **Data Parser** - Decode 6-byte packets
4. **Real-time UI** - Display HR, SpO2, Temperature with charts
5. **Data Persistence** - Store readings in Room database
6. **Firebase Sync** - Upload to cloud for caregiver access

### Recommended Android Libraries
- **RxAndroidBle** - Reactive BLE wrapper
- **MPAndroidChart** - Real-time charts
- **Room** - Local database
- **WorkManager** - Background sync

---

## Academic Considerations

### For Final Year Project Report

#### Technical Contributions
1. **Custom BLE Protocol** - Designed efficient 6-byte format
2. **Non-blocking Architecture** - BLE doesn't interfere with sensor sampling
3. **Fault Tolerance** - Graceful handling of disconnections
4. **Power Efficiency** - 1Hz updates balance responsiveness and battery life

#### Testing Evidence
- Include nRF Connect screenshots showing:
  - Device advertising
  - Service/characteristic discovery
  - Notification data stream
  - Connection stability over time

#### Limitations & Future Work
- Current implementation uses simplified SpO2 calculation
- No encryption (add BLE pairing for production)
- Fixed 1Hz update rate (could be adaptive based on activity)
- No battery level reporting (add Battery Service)

---

## Summary

Phase 3 successfully adds BLE communication to the stable Phase 2 sensor firmware. The implementation:

✅ Preserves all Phase 2 sensor logic  
✅ Uses standard BLE GATT architecture  
✅ Implements compact binary protocol  
✅ Handles connections gracefully  
✅ Maintains Serial Monitor debugging  
✅ Ready for Android app integration  

The Arduino firmware is now complete and ready for Phase 4 Android development.
