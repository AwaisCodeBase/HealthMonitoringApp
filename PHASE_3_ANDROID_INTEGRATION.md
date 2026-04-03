# Phase 3: Android Integration Reference

## Quick Start for Android Developers

This guide provides ready-to-use Android code for connecting to the Phase 3 BLE firmware.

---

## BLE Constants

```java
public class BleConstants {
    // Device identification
    public static final String DEVICE_NAME = "ChildHealthWearable";
    public static final String LOCAL_NAME = "Child Health Monitor";
    
    // Service and Characteristic UUIDs
    public static final UUID SERVICE_UUID = 
        UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
    
    public static final UUID SENSOR_DATA_CHAR_UUID = 
        UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214");
    
    // Client Characteristic Configuration Descriptor (for notifications)
    public static final UUID CCCD_UUID = 
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
}
```

---

## Data Model

```java
public class SensorReading {
    private int heartRate;        // BPM (0-200)
    private int spO2;             // Percentage (0-100)
    private float temperature;    // Celsius (30.0-42.0)
    private boolean hrValid;      // Heart rate validity
    private boolean tempValid;    // Temperature validity
    private long timestamp;       // System time when received
    
    // Constructor
    public SensorReading(int heartRate, int spO2, float temperature, 
                         boolean hrValid, boolean tempValid) {
        this.heartRate = heartRate;
        this.spO2 = spO2;
        this.temperature = temperature;
        this.hrValid = hrValid;
        this.tempValid = tempValid;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Parse from BLE data packet (6 bytes)
    public static SensorReading fromBytes(byte[] data) {
        if (data == null || data.length != 6) {
            return null;
        }
        
        // Parse heart rate (little-endian uint16)
        int heartRate = ((data[1] & 0xFF) << 8) | (data[0] & 0xFF);
        
        // Parse SpO2 (uint8)
        int spO2 = data[2] & 0xFF;
        
        // Parse temperature (little-endian uint16, divide by 10)
        int tempRaw = ((data[4] & 0xFF) << 8) | (data[3] & 0xFF);
        float temperature = tempRaw / 10.0f;
        
        // Parse validity flags
        boolean hrValid = (data[5] & 0x01) != 0;
        boolean tempValid = (data[5] & 0x02) != 0;
        
        return new SensorReading(heartRate, spO2, temperature, hrValid, tempValid);
    }
    
    // Getters
    public int getHeartRate() { return heartRate; }
    public int getSpO2() { return spO2; }
    public float getTemperature() { return temperature; }
    public boolean isHrValid() { return hrValid; }
    public boolean isTempValid() { return tempValid; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("HR: %d BPM, SpO2: %d%%, Temp: %.1f°C [HR:%s, Temp:%s]",
            heartRate, spO2, temperature,
            hrValid ? "✓" : "✗",
            tempValid ? "✓" : "✗");
    }
}
```

---

## BLE Manager (Simplified)

```java
public class BleManager {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BleCallback callback;
    
    // Callback interface for UI updates
    public interface BleCallback {
        void onDeviceConnected();
        void onDeviceDisconnected();
        void onSensorDataReceived(SensorReading reading);
        void onError(String error);
    }
    
    public BleManager(Context context, BleCallback callback) {
        this.context = context;
        this.callback = callback;
        
        BluetoothManager bluetoothManager = 
            (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
    }
    
    // Start scanning for device
    public void startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            callback.onError("Bluetooth not enabled");
            return;
        }
        
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        
        // Filter for our device name
        ScanFilter filter = new ScanFilter.Builder()
            .setDeviceName(BleConstants.DEVICE_NAME)
            .build();
        
        ScanSettings settings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build();
        
        scanner.startScan(Arrays.asList(filter), settings, scanCallback);
    }
    
    // Stop scanning
    public void stopScan() {
        if (bluetoothAdapter != null) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner != null) {
                scanner.stopScan(scanCallback);
            }
        }
    }
    
    // Scan callback
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            stopScan();
            connectToDevice(device);
        }
        
        @Override
        public void onScanFailed(int errorCode) {
            callback.onError("Scan failed: " + errorCode);
        }
    };
    
    // Connect to device
    private void connectToDevice(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }
    
    // Disconnect
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
    
    // GATT callback
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                callback.onDeviceConnected();
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                callback.onDeviceDisconnected();
            }
        }
        
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                enableNotifications(gatt);
            }
        }
        
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, 
                                             BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            SensorReading reading = SensorReading.fromBytes(data);
            
            if (reading != null) {
                callback.onSensorDataReceived(reading);
            }
        }
    };
    
    // Enable notifications
    private void enableNotifications(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(BleConstants.SERVICE_UUID);
        if (service == null) {
            callback.onError("Service not found");
            return;
        }
        
        BluetoothGattCharacteristic characteristic = 
            service.getCharacteristic(BleConstants.SENSOR_DATA_CHAR_UUID);
        if (characteristic == null) {
            callback.onError("Characteristic not found");
            return;
        }
        
        // Enable local notifications
        gatt.setCharacteristicNotification(characteristic, true);
        
        // Enable remote notifications via CCCD
        BluetoothGattDescriptor descriptor = 
            characteristic.getDescriptor(BleConstants.CCCD_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
    }
}
```

---

## Activity Example

```java
public class MonitoringActivity extends AppCompatActivity {
    private BleManager bleManager;
    private TextView tvHeartRate, tvSpO2, tvTemperature, tvStatus;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        
        // Initialize views
        tvHeartRate = findViewById(R.id.tv_heart_rate);
        tvSpO2 = findViewById(R.id.tv_spo2);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvStatus = findViewById(R.id.tv_status);
        
        // Initialize BLE manager
        bleManager = new BleManager(this, bleCallback);
        
        // Check permissions and start scan
        if (hasBluetoothPermissions()) {
            bleManager.startScan();
            tvStatus.setText("Scanning...");
        } else {
            requestBluetoothPermissions();
        }
    }
    
    private BleManager.BleCallback bleCallback = new BleManager.BleCallback() {
        @Override
        public void onDeviceConnected() {
            runOnUiThread(() -> {
                tvStatus.setText("Connected");
                tvStatus.setTextColor(Color.GREEN);
            });
        }
        
        @Override
        public void onDeviceDisconnected() {
            runOnUiThread(() -> {
                tvStatus.setText("Disconnected");
                tvStatus.setTextColor(Color.RED);
                
                // Auto-reconnect after 2 seconds
                new Handler().postDelayed(() -> {
                    bleManager.startScan();
                    tvStatus.setText("Reconnecting...");
                }, 2000);
            });
        }
        
        @Override
        public void onSensorDataReceived(SensorReading reading) {
            runOnUiThread(() -> {
                if (reading.isHrValid()) {
                    tvHeartRate.setText(reading.getHeartRate() + " BPM");
                    tvSpO2.setText(reading.getSpO2() + "%");
                } else {
                    tvHeartRate.setText("--");
                    tvSpO2.setText("--");
                }
                
                if (reading.isTempValid()) {
                    tvTemperature.setText(String.format("%.1f°C", reading.getTemperature()));
                } else {
                    tvTemperature.setText("--");
                }
            });
        }
        
        @Override
        public void onError(String error) {
            runOnUiThread(() -> {
                tvStatus.setText("Error: " + error);
                tvStatus.setTextColor(Color.RED);
            });
        }
    };
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bleManager != null) {
            bleManager.disconnect();
        }
    }
    
    // Permission handling (Android 12+)
    private boolean hasBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == 
                   PackageManager.PERMISSION_GRANTED &&
                   checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == 
                   PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            }, 1);
        }
    }
}
```

---

## Layout Example (activity_monitoring.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:gravity="center">
    
    <!-- Status -->
    <TextView
        android:id="@+id/tv_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Disconnected"
        android:textSize="18sp"
        android:textColor="@android:color/holo_red_dark"
        android:layout_marginBottom="32dp"/>
    
    <!-- Heart Rate -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:layout_marginBottom="16dp">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Heart Rate: "
            android:textSize="20sp"/>
        
        <TextView
            android:id="@+id/tv_heart_rate"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="--"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="@android:color/holo_red_dark"/>
    </LinearLayout>
    
    <!-- SpO2 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:layout_marginBottom="16dp">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="SpO2: "
            android:textSize="20sp"/>
        
        <TextView
            android:id="@+id/tv_spo2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="--"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="@android:color/holo_blue_dark"/>
    </LinearLayout>
    
    <!-- Temperature -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Temperature: "
            android:textSize="20sp"/>
        
        <TextView
            android:id="@+id/tv_temperature"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="--"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="@android:color/holo_orange_dark"/>
    </LinearLayout>
    
</LinearLayout>
```

---

## AndroidManifest.xml Permissions

```xml
<!-- Bluetooth permissions -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<!-- Android 12+ (API 31+) -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Location (required for BLE scanning on Android 10-11) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Feature declaration -->
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
```

---

## Gradle Dependencies

```gradle
dependencies {
    // Core Android
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    
    // Optional: RxAndroidBle for easier BLE handling
    implementation 'com.polidea.rxandroidble2:rxandroidble:1.17.2'
    implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
}
```

---

## Testing Checklist

### Arduino Side
- [ ] Upload PHASE_3_BLE_IMPLEMENTATION.ino
- [ ] Verify "BLE Advertising started" in Serial Monitor
- [ ] Confirm sensor readings are stable
- [ ] Test with nRF Connect app first

### Android Side
- [ ] Add Bluetooth permissions to manifest
- [ ] Request runtime permissions (Android 12+)
- [ ] Scan finds "ChildHealthWearable"
- [ ] Connection succeeds
- [ ] Notifications received every 1 second
- [ ] Data parsing is correct
- [ ] UI updates in real-time
- [ ] Disconnection handled gracefully
- [ ] Auto-reconnect works

---

## Common Issues

### Issue: Device not found in scan
**Solution**: 
- Verify Arduino is advertising (check Serial Monitor)
- Ensure Bluetooth is enabled on Android
- Grant location permissions (required for BLE scan)
- Try restarting Arduino

### Issue: Connection fails immediately
**Solution**:
- Check Android Bluetooth permissions
- Verify device is not already connected to another app
- Try clearing Bluetooth cache: Settings → Apps → Bluetooth → Clear Cache

### Issue: Notifications not received
**Solution**:
- Ensure CCCD descriptor is written
- Verify `setCharacteristicNotification()` is called
- Check Arduino Serial Monitor shows "CONN" status
- Confirm characteristic has Notify property

### Issue: Wrong data values
**Solution**:
- Verify byte order (little-endian)
- Check bit masking: `data[i] & 0xFF`
- Confirm temperature division by 10
- Validate flags parsing

---

## Next Steps

1. **Implement this basic connection** in your Android app
2. **Test with real hardware** to verify data flow
3. **Add data persistence** (Room database)
4. **Create charts** for historical data (MPAndroidChart)
5. **Implement Firebase sync** for caregiver access
6. **Add alerts** for abnormal readings

This completes the BLE communication layer. Your Arduino firmware is now ready for full Android app integration!
