# 🚀 WiFi + Firestore Migration - Quick Guide

## ✅ What You Have Ready

Your app already has **complete WiFi + Firestore code**! You just need to switch from BLE to WiFi.

**Ready to use:**
- ✅ `WifiHealthMonitorManager.java` - WiFi WebSocket client
- ✅ `FirestoreHealthRepository.java` - Cloud database
- ✅ `HealthMonitorViewModelWifi.java` - WiFi + Firestore ViewModel
- ✅ `ESP32_WiFi_Health_Monitor.ino` - Arduino firmware

---

## 📋 Migration Steps (30 minutes)

### **Step 1: Deploy Firestore Security Rules** (5 min)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Click **Firestore Database** → **Rules**
4. Replace with these rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own health data
    match /users/{userId}/health_records/{recordId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Validate data types and ranges
      allow create, update: if request.auth != null 
        && request.auth.uid == userId
        && request.resource.data.heartRate is int
        && request.resource.data.heartRate >= 0
        && request.resource.data.heartRate <= 250
        && request.resource.data.spo2 is int
        && request.resource.data.spo2 >= 0
        && request.resource.data.spo2 <= 100
        && request.resource.data.temperature is number
        && request.resource.data.temperature >= 30
        && request.resource.data.temperature <= 45
        && request.resource.data.status is string
        && request.resource.data.status in ["GOOD", "WARNING", "CRITICAL"];
    }
  }
}
```

5. Click **Publish**

---

### **Step 2: Create Firestore Indexes** (2 min)

1. In Firebase Console → **Firestore Database** → **Indexes**
2. Click **Create Index**
3. Add these indexes:

**Index 1: Time Range Queries**
- Collection: `health_records`
- Fields:
  - `timestamp` (Ascending)
- Query scope: Collection

**Index 2: Critical Records**
- Collection: `health_records`
- Fields:
  - `status` (Ascending)
  - `timestamp` (Descending)
- Query scope: Collection

---

### **Step 3: Update Fragments to Use WiFi ViewModel** (10 min)

I'll update these files for you:
- `ModernDashboardFragment.java`
- `HistoryFragment.java`
- `DashboardFragment.java` (if used)

**Change:**
```java
// OLD (BLE + Room)
viewModel = new ViewModelProvider(requireActivity()).get(HealthMonitorViewModel.class);

// NEW (WiFi + Firestore)
viewModel = new ViewModelProvider(requireActivity()).get(HealthMonitorViewModelWifi.class);
```

---

### **Step 4: Update Connection Logic** (5 min)

**OLD (BLE):**
```java
viewModel.startScanning(); // Scan for BLE devices
```

**NEW (WiFi):**
```java
viewModel.connect("192.168.1.100"); // Connect to ESP32 IP address
```

You'll need to know your ESP32's IP address (shown in Arduino Serial Monitor).

---

### **Step 5: Flash ESP32 Firmware** (8 min)

1. Open Arduino IDE
2. Open `ESP32_WiFi_Health_Monitor.ino`
3. Update WiFi credentials:

```cpp
const char* WIFI_SSID = "YOUR_WIFI_NAME";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
```

4. Select **Board:** ESP32 Dev Module
5. Upload to ESP32
6. Open Serial Monitor (115200 baud)
7. Note the IP address shown (e.g., `192.168.1.100`)

---

## 🔧 Files I'll Update For You

### 1. ModernDashboardFragment.java
- Change ViewModel to `HealthMonitorViewModelWifi`
- Update connection method
- Add IP address input (or hardcode)

### 2. HistoryFragment.java
- Change ViewModel to `HealthMonitorViewModelWifi`
- Update data types (Firestore entities)

### 3. DashboardFragment.java
- Same changes as ModernDashboardFragment

---

## 📊 What Changes

| Feature | Before (BLE + Room) | After (WiFi + Firestore) |
|---------|---------------------|--------------------------|
| **Communication** | Bluetooth (10-30m) | WiFi (50-100m) ✅ |
| **Database** | Local SQLite | Cloud + Local ✅ |
| **Latency** | 10-50ms | 5-20ms ✅ |
| **Multi-device** | No | Yes ✅ |
| **Offline** | Yes | Yes (cached) ✅ |
| **Backup** | Manual | Automatic ✅ |
| **Cost** | Free | Free tier (single user) |

---

## ⚠️ Important Notes

### WiFi Setup
- ESP32 and phone must be on **same WiFi network**
- Note ESP32 IP address from Serial Monitor
- IP address may change (use static IP or mDNS)

### Firestore Costs (Single User)
- Writes: 8,640/day (10s interval)
- Reads: ~100/day (with caching)
- **Cost: $0/month** (within free tier)

### Data Migration
- Old Room data stays on device
- New data goes to Firestore
- Can export old data to CSV first

---

## 🧪 Testing After Migration

### 1. Test WiFi Connection
```
1. Flash ESP32 firmware
2. Note IP address from Serial Monitor
3. Run Android app
4. Enter IP address
5. Click "Connect"
6. Should show "Connected" status
```

### 2. Test Data Storage
```
1. Monitor vitals for 1 minute
2. Go to Firebase Console → Firestore
3. Navigate to: users/{yourUserId}/health_records
4. Should see documents appearing
5. Check timestamp, heartRate, spo2, temperature
```

### 3. Test Historical Data
```
1. Open History tab
2. Should see charts with data
3. Try different time ranges (24h, 7d, 30d)
4. Export to CSV
5. Verify data is correct
```

---

## 🚀 Ready to Migrate?

Reply with **"Yes, migrate now"** and I'll:

1. ✅ Update all 3 fragments to use WiFi ViewModel
2. ✅ Add IP address configuration
3. ✅ Update connection logic
4. ✅ Test for compilation errors
5. ✅ Provide ESP32 setup instructions

**Estimated time:** 30 minutes total
**Reversible:** Yes (can switch back to BLE if needed)

---

## 📞 What You Need

Before we start:
- [ ] ESP32 device with sensors
- [ ] WiFi network name and password
- [ ] Firebase project with Firestore enabled
- [ ] Arduino IDE installed

---

**Ready? Let's migrate to WiFi + Firestore!** 🎉
