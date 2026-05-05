# 📡 ESP32 WiFi Setup Guide

## 🎯 Quick Setup (8 minutes)

### What You Need
- ✅ ESP32 board
- ✅ MAX30102 sensor (Heart Rate + SpO2)
- ✅ MLX90614 sensor (Temperature)
- ✅ Arduino IDE installed
- ✅ WiFi network name and password

---

## 📋 Step-by-Step Instructions

### Step 1: Install Arduino IDE (if not installed)
1. Download from: https://www.arduino.cc/en/software
2. Install for your OS
3. Open Arduino IDE

### Step 2: Install ESP32 Board Support
1. Open Arduino IDE
2. Go to **File** → **Preferences**
3. In "Additional Board Manager URLs", add:
   ```
   https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
   ```
4. Click **OK**
5. Go to **Tools** → **Board** → **Boards Manager**
6. Search for "ESP32"
7. Install "esp32 by Espressif Systems"
8. Wait for installation to complete

### Step 3: Install Required Libraries
1. Go to **Sketch** → **Include Library** → **Manage Libraries**
2. Install these libraries:

**Library 1: WebSockets**
- Search: "WebSockets by Markus Sattler"
- Click **Install**

**Library 2: ArduinoJson**
- Search: "ArduinoJson by Benoit Blanchon"
- Click **Install**
- Version: 6.x or higher

**Library 3: MAX30105**
- Search: "MAX30105 by SparkFun"
- Click **Install**

**Library 4: Adafruit MLX90614**
- Search: "Adafruit MLX90614"
- Click **Install**
- Also install dependencies if prompted

### Step 4: Open ESP32 Firmware
1. Locate the file: `ESP32_WiFi_Health_Monitor.ino`
2. Open it in Arduino IDE

### Step 5: Configure WiFi Credentials
Find these lines near the top of the code:

```cpp
// ============================================
// WIFI CONFIGURATION - CHANGE THESE!
// ============================================
const char* WIFI_SSID = "YOUR_WIFI_NAME";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
```

**Replace with your WiFi details:**
```cpp
const char* WIFI_SSID = "MyHomeWiFi";        // Your WiFi name
const char* WIFI_PASSWORD = "mypassword123";  // Your WiFi password
```

### Step 6: Select Board and Port
1. Go to **Tools** → **Board** → **ESP32 Arduino**
2. Select: **ESP32 Dev Module**
3. Go to **Tools** → **Port**
4. Select the port with your ESP32 connected
   - Windows: COM3, COM4, etc.
   - Mac: /dev/cu.usbserial-xxx
   - Linux: /dev/ttyUSB0, /dev/ttyUSB1

### Step 7: Upload Firmware
1. Click the **Upload** button (→ arrow icon)
2. Wait for compilation (1-2 minutes)
3. Wait for upload (30 seconds)
4. You should see "Done uploading"

### Step 8: Get ESP32 IP Address
1. Open **Serial Monitor** (magnifying glass icon)
2. Set baud rate to: **115200**
3. Press **Reset** button on ESP32
4. You should see:

```
=================================
ESP32 WiFi Health Monitor
=================================

Connecting to WiFi: MyHomeWiFi
.....
WiFi Connected!
IP Address: 192.168.1.100    ← WRITE THIS DOWN!
WebSocket Server Started
Port: 8080
Waiting for Android connection...
```

5. **Write down the IP address!** (e.g., 192.168.1.100)

---

## 🔌 Hardware Connections

### MAX30102 (Heart Rate + SpO2)
```
MAX30102    →    ESP32
VCC         →    3.3V
GND         →    GND
SDA         →    GPIO 21
SCL         →    GPIO 22
```

### MLX90614 (Temperature)
```
MLX90614    →    ESP32
VCC         →    3.3V
GND         →    GND
SDA         →    GPIO 21 (shared with MAX30102)
SCL         →    GPIO 22 (shared with MAX30102)
```

**Note:** Both sensors use I2C and share the same pins.

---

## 🧪 Testing

### Test 1: WiFi Connection
**Expected Serial Monitor Output:**
```
Connecting to WiFi: MyHomeWiFi
.....
WiFi Connected!
IP Address: 192.168.1.100
Signal Strength: -45 dBm (Excellent)
```

**If it fails:**
- Check WiFi name and password
- Make sure WiFi is 2.4GHz (ESP32 doesn't support 5GHz)
- Move ESP32 closer to router

### Test 2: Sensor Readings
**Expected Serial Monitor Output:**
```
=================================
Sensor Readings:
=================================
Heart Rate: 75 BPM (Valid)
SpO2: 98% (Valid)
Temperature: 36.5°C (Valid)
Status: GOOD
```

**If sensors fail:**
- Check wiring connections
- Make sure sensors are powered (3.3V)
- Place finger on MAX30102 sensor
- Point MLX90614 at forehead (5cm distance)

### Test 3: WebSocket Server
**Expected Serial Monitor Output:**
```
WebSocket Server Started
Port: 8080
Waiting for Android connection...

[When Android connects]
>>> Client Connected: 192.168.1.50
Sending data to Android...
```

---

## 📱 Connect Android App

### Step 1: Make Sure Phone and ESP32 are on Same WiFi
- ESP32: Connected to "MyHomeWiFi"
- Phone: Connected to "MyHomeWiFi"
- **Must be the same network!**

### Step 2: Open Android App
1. Launch Child Health Monitor app
2. Go to Dashboard
3. Click "Connect Device"

### Step 3: Enter ESP32 IP Address
1. Dialog will appear
2. Enter the IP address from Serial Monitor
   - Example: `192.168.1.100`
3. Click "Connect"

### Step 4: Verify Connection
**On Android:**
- Connection badge should show "Connected (WiFi)"
- Green dot indicator
- Vitals should start updating

**On Serial Monitor:**
```
>>> Client Connected: 192.168.1.50
Sending health data...
HR: 75, SpO2: 98, Temp: 36.5
```

---

## 🔧 Troubleshooting

### Problem: ESP32 won't connect to WiFi
**Solutions:**
1. Check WiFi credentials (case-sensitive!)
2. Make sure WiFi is 2.4GHz (not 5GHz)
3. Move ESP32 closer to router
4. Restart ESP32 (press reset button)
5. Check if WiFi has MAC address filtering

### Problem: Can't find ESP32 IP address
**Solutions:**
1. Open Serial Monitor (115200 baud)
2. Press Reset button on ESP32
3. IP address will be printed
4. Alternatively, check your router's connected devices list

### Problem: Android can't connect to ESP32
**Solutions:**
1. Verify phone and ESP32 on same WiFi
2. Check IP address is correct
3. Ping ESP32 from phone (use network tools app)
4. Check firewall settings on router
5. Try restarting ESP32

### Problem: Sensors not working
**Solutions:**
1. Check wiring (especially GND and VCC)
2. Verify I2C connections (SDA=21, SCL=22)
3. Place finger firmly on MAX30102
4. Point MLX90614 at forehead (5cm away)
5. Check Serial Monitor for sensor errors

### Problem: Connection drops frequently
**Solutions:**
1. Move ESP32 closer to router
2. Reduce WiFi interference
3. Check power supply (use good USB cable)
4. Update ESP32 firmware
5. Check Serial Monitor for error messages

---

## 📊 Expected Performance

### WiFi Connection
- **Range:** 50-100 meters (line of sight)
- **Latency:** 5-20ms
- **Reconnect Time:** 2-5 seconds
- **Signal Strength:** -30 to -70 dBm (good)

### Data Transmission
- **Update Rate:** 1 Hz (every second)
- **Packet Size:** ~150 bytes (JSON)
- **Bandwidth:** ~1.2 Kbps (very low)
- **Reliability:** 99.9% (with good WiFi)

### Sensor Accuracy
- **Heart Rate:** ±2 BPM
- **SpO2:** ±2%
- **Temperature:** ±0.5°C

---

## 🎯 Configuration Options

### Change Update Rate
Find this line in the code:
```cpp
#define SENSOR_UPDATE_INTERVAL 1000  // 1 second
```

Change to:
```cpp
#define SENSOR_UPDATE_INTERVAL 500   // 0.5 seconds (faster)
#define SENSOR_UPDATE_INTERVAL 2000  // 2 seconds (slower)
```

### Change WebSocket Port
Find this line:
```cpp
#define WS_PORT 8080
```

Change to any port (e.g., 8081, 9000)

### Enable Debug Logging
Find this line:
```cpp
#define DEBUG_MODE false
```

Change to:
```cpp
#define DEBUG_MODE true  // More detailed logs
```

---

## 💡 Tips for Best Performance

1. **WiFi Signal:**
   - Keep ESP32 within 10m of router for best performance
   - Avoid metal obstacles between ESP32 and router
   - Use 2.4GHz WiFi (better range than 5GHz)

2. **Power Supply:**
   - Use good quality USB cable
   - Avoid long/thin cables
   - Consider external power supply for stability

3. **Sensor Placement:**
   - MAX30102: Finger should cover sensor completely
   - MLX90614: Point at center of forehead, 3-5cm away
   - Keep sensors clean

4. **Static IP (Optional):**
   - Configure router to assign static IP to ESP32
   - Prevents IP address from changing
   - Makes connection easier

---

## ✅ Verification Checklist

Before using with Android app:

- [ ] ESP32 connects to WiFi successfully
- [ ] IP address is displayed in Serial Monitor
- [ ] WebSocket server starts (port 8080)
- [ ] MAX30102 sensor reads heart rate and SpO2
- [ ] MLX90614 sensor reads temperature
- [ ] All readings show "Valid" status
- [ ] Serial Monitor shows "Waiting for Android connection"
- [ ] Phone is on same WiFi network as ESP32
- [ ] IP address is written down

---

## 🎉 Success!

If you see this in Serial Monitor:
```
WiFi Connected!
IP Address: 192.168.1.100
WebSocket Server Started
Sensor Readings:
  HR: 75 BPM (Valid)
  SpO2: 98% (Valid)
  Temp: 36.5°C (Valid)
Waiting for Android connection...
```

**You're ready to connect the Android app!** 🚀

---

**Next Step:** Open Android app and enter the IP address!

