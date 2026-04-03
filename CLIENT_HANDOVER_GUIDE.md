# Child Health Monitor - Client Handover Guide

## 📊 System Architecture Overview

### Database Architecture

The app uses a **HYBRID DATABASE APPROACH**:

#### 1. **Room Database (SQLite) - PRIMARY DATA STORAGE** ✅
- **Purpose:** Local storage for all health monitoring data
- **Location:** On-device (Android SQLite)
- **Data Stored:**
  - Real-time vital signs (Heart Rate, SpO2, Temperature)
  - Historical health records
  - Timestamps
  - Health status evaluations
  - User-specific data

**Why Room/SQLite?**
- ✅ Offline-first architecture
- ✅ Fast data access
- ✅ No internet required for monitoring
- ✅ Perfect for time-series sensor data
- ✅ Medical-grade data logging
- ✅ Battery efficient
- ✅ Industry standard for wearables

**Database Schema:**
```sql
Table: health_records
- id (INTEGER, PRIMARY KEY, AUTOINCREMENT)
- timestamp (INTEGER, NOT NULL)
- heartRate (INTEGER, NOT NULL)
- spO2 (INTEGER, NOT NULL)
- temperature (REAL, NOT NULL)
- healthStatus (TEXT, NOT NULL) -- "GOOD", "WARNING", "CRITICAL"
- userId (TEXT, NOT NULL) -- Firebase User ID
```

**Storage Strategy:**
- Data stored every 10 seconds
- Data stored when health status changes
- Prevents excessive writes
- Captures all critical events

#### 2. **Firebase - AUTHENTICATION ONLY** ✅
- **Purpose:** User authentication and session management
- **Services Used:**
  - Firebase Authentication (Email/Password + Google Sign-In)
  - User management
  - Session tokens

**What Firebase DOES:**
- ✅ User registration
- ✅ User login (Email/Password)
- ✅ Google Sign-In integration
- ✅ Session management
- ✅ User ID generation

**What Firebase DOES NOT DO:**
- ❌ Store health data
- ❌ Store vital signs
- ❌ Store historical records
- ❌ Real-time data sync

**Why This Approach?**
- Firebase is ONLY for authentication
- All health data stays LOCAL on device
- Better privacy and security
- Faster data access
- Works offline
- Lower costs (no Firestore reads/writes)

---

## 🔬 Sensors Integrated

### Hardware Sensors (Arduino/ESP32 BLE Device)

#### 1. **MAX30102 Sensor** 🫀
**Purpose:** Heart Rate and Blood Oxygen Monitoring

**Specifications:**
- **Type:** Pulse Oximeter and Heart Rate Sensor
- **Measurements:**
  - Heart Rate (BPM): 40-200 BPM range
  - SpO2 (Blood Oxygen): 0-100% range
- **Technology:** Photoplethysmography (PPG)
- **Interface:** I2C
- **Sampling Rate:** Configurable (50-400 Hz)
- **LEDs:** Red and Infrared
- **Power:** 3.3V

**What It Measures:**
- **Heart Rate (HR):** Beats per minute
- **SpO2:** Oxygen saturation percentage in blood
- **Validity Flag:** Indicates if reading is reliable

**Normal Ranges (Children):**
- Heart Rate: 60-120 BPM (normal)
- SpO2: ≥95% (normal)

**Connection:**
- SDA → Pin 21 (ESP32) / A4 (Arduino)
- SCL → Pin 22 (ESP32) / A5 (Arduino)
- VCC → 3.3V
- GND → GND

#### 2. **MLX90614 Sensor** 🌡️
**Purpose:** Non-Contact Temperature Monitoring

**Specifications:**
- **Type:** Infrared Temperature Sensor
- **Measurement:** Non-contact (infrared)
- **Range:** -70°C to +380°C
- **Accuracy:** ±0.5°C (around body temperature)
- **Interface:** I2C
- **Distance:** 0-5cm optimal for body temperature
- **Power:** 3.3V

**What It Measures:**
- **Body Temperature:** In Celsius (°C)
- **Object Temperature:** Infrared reading
- **Ambient Temperature:** Also available

**Normal Range (Children):**
- Temperature: 36.0-37.5°C (normal)

**Connection:**
- SDA → Pin 21 (ESP32) / A4 (Arduino)
- SCL → Pin 22 (ESP32) / A5 (Arduino)
- VCC → 3.3V
- GND → GND

---

## 📡 BLE Communication

### Bluetooth Low Energy (BLE) Configuration

**Service UUID:**
```
4fafc201-1fb5-459e-8fcc-c5c9c331914b
```

**Characteristics:**

1. **Heart Rate & SpO2 Characteristic:**
   - UUID: `beb5483e-36e1-4688-b7f5-ea07361b26a8`
   - Properties: READ, NOTIFY
   - Data Format (4 bytes):
     - Byte 0: Heart Rate (BPM)
     - Byte 1: SpO2 (%)
     - Byte 2: HR Valid Flag (0 or 1)
     - Byte 3: Reserved

2. **Temperature Characteristic:**
   - UUID: `cba1d466-344c-4be3-ab3f-189f80dd7518`
   - Properties: READ, NOTIFY
   - Data Format (4 bytes):
     - Bytes 0-3: Temperature (float, °C)

**Update Frequency:**
- Heart Rate & SpO2: Every 1 second
- Temperature: Every 1 second
- BLE notifications sent automatically

---

## 🏗️ Complete System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    HARDWARE LAYER                            │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │  MAX30102    │  │  MLX90614    │                        │
│  │  (HR + SpO2) │  │  (Temp)      │                        │
│  └──────┬───────┘  └──────┬───────┘                        │
│         │                 │                                 │
│         └────────┬────────┘                                 │
│                  │ I2C                                      │
│         ┌────────▼────────┐                                 │
│         │ Arduino/ESP32   │                                 │
│         │ BLE Server      │                                 │
│         └────────┬────────┘                                 │
└──────────────────┼──────────────────────────────────────────┘
                   │ BLE
┌──────────────────▼──────────────────────────────────────────┐
│                 ANDROID APP LAYER                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         HealthMonitorBleManager                      │   │
│  │         • Scans for device                           │   │
│  │         • Connects via GATT                          │   │
│  │         • Receives notifications                     │   │
│  │         • Parses sensor data                         │   │
│  └──────────────────┬───────────────────────────────────┘   │
│                     │                                        │
│  ┌──────────────────▼───────────────────────────────────┐   │
│  │         HealthMonitorViewModel                       │   │
│  │         • Manages data flow                          │   │
│  │         • Evaluates health status                    │   │
│  │         • Exposes LiveData                           │   │
│  │         • Triggers storage                           │   │
│  └──────────────────┬───────────────────────────────────┘   │
│                     │                                        │
│         ┌───────────┴───────────┐                           │
│         │                       │                           │
│  ┌──────▼────────┐     ┌───────▼────────┐                  │
│  │  UI Fragments │     │  Repository    │                  │
│  │  • Dashboard  │     │  • Background  │                  │
│  │  • Monitoring │     │    threading   │                  │
│  │  • History    │     │  • Data access │                  │
│  └───────────────┘     └───────┬────────┘                  │
│                                 │                           │
└─────────────────────────────────┼───────────────────────────┘
                                  │
┌─────────────────────────────────▼───────────────────────────┐
│                    DATA LAYER                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Room Database (SQLite)                       │   │
│  │         • Local storage                              │   │
│  │         • health_records table                       │   │
│  │         • Offline-first                              │   │
│  │         • Fast queries                               │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                 AUTHENTICATION LAYER                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Firebase Authentication                      │   │
│  │         • Email/Password                             │   │
│  │         • Google Sign-In                             │   │
│  │         • User management                            │   │
│  │         • Session tokens                             │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔐 Firebase Configuration for Client

### What Client Needs Access To:

#### 1. **Firebase Console Access**
- URL: https://console.firebase.google.com
- Project Name: [Your Project Name]
- Client needs: Owner or Editor role

#### 2. **Firebase Services Used**

**Authentication:**
- Email/Password provider (enabled)
- Google Sign-In provider (enabled)
- User management

**Configuration Files:**
- `google-services.json` (Android app)
- Contains API keys and project configuration

#### 3. **What Client Should Know**

**Firebase is ONLY for:**
- ✅ User registration
- ✅ User login
- ✅ Google Sign-In
- ✅ User session management

**Firebase is NOT used for:**
- ❌ Health data storage
- ❌ Vital signs storage
- ❌ Historical records
- ❌ Real-time database
- ❌ Cloud Firestore

**Why?**
- All health data is stored locally in Room (SQLite)
- Better privacy and security
- Faster performance
- Works offline
- Lower Firebase costs

---

## 📊 Data Flow Summary

### Real-Time Monitoring Flow

```
1. Sensors (MAX30102 + MLX90614)
   ↓
2. Arduino/ESP32 reads sensor data
   ↓
3. BLE transmits data to Android app
   ↓
4. HealthMonitorBleManager receives data
   ↓
5. HealthMonitorViewModel processes data
   ↓
6. Health status evaluated (GOOD/WARNING/CRITICAL)
   ↓
7. UI updates in real-time (LiveData)
   ↓
8. Data stored to Room database (every 10s or status change)
```

### Authentication Flow

```
1. User opens app
   ↓
2. SplashActivity checks Firebase auth
   ↓
3. If logged in → MainActivity
   If not → LoginActivity
   ↓
4. User logs in (Email or Google)
   ↓
5. Firebase authenticates
   ↓
6. User ID stored for database queries
   ↓
7. MainActivity loads with user session
```

---

## 📱 App Features Summary

### Real-Time Monitoring
- Heart Rate (BPM)
- Blood Oxygen (SpO2 %)
- Body Temperature (°C)
- Health Status (Good/Warning/Critical)
- Animated indicators
- Trend arrows

### Historical Data
- Interactive line charts
- Time range filtering (24h, 7d, 30d)
- Statistical analysis
- Status distribution
- CSV export

### Alerts & Thresholds
- Configurable thresholds
- Color-coded status (Green/Yellow/Red)
- Visual indicators
- Blinking animation for critical

### Data Storage
- Local SQLite database
- Offline-first
- Efficient storage strategy
- Fast queries
- Privacy-focused

---

## 🔧 Technical Specifications

### Android App
- **Language:** Java 17
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)
- **Architecture:** MVVM
- **Database:** Room (SQLite)
- **Authentication:** Firebase Auth
- **BLE:** Android BLE APIs

### Hardware
- **Microcontroller:** Arduino Nano 33 BLE or ESP32
- **Sensors:** MAX30102, MLX90614
- **Communication:** BLE 4.0+
- **Power:** USB or Battery

### Libraries Used
- Room Persistence Library
- Firebase Authentication
- MPAndroidChart (charts)
- Material Components (UI)
- Navigation Component
- Lifecycle Components

---

## 💰 Cost Implications

### Firebase Costs
**Current Usage:**
- Authentication only
- No Firestore reads/writes
- No Realtime Database
- No Cloud Storage

**Expected Costs:**
- **Free Tier:** Up to 10,000 phone auth/month
- **Google Sign-In:** Free (unlimited)
- **Email/Password:** Free (unlimited)
- **Total:** $0/month for typical usage

### Room Database Costs
- **Cost:** $0 (local storage)
- **Storage:** Device storage only
- **No cloud costs**

---

## 📋 Client Checklist

### Firebase Setup
- [ ] Access Firebase Console
- [ ] Verify Authentication providers enabled
- [ ] Check user list
- [ ] Review security rules (if any)
- [ ] Download `google-services.json` (backup)

### App Configuration
- [ ] Verify `google-services.json` in app
- [ ] Check SHA-1 fingerprint registered
- [ ] Test Email/Password login
- [ ] Test Google Sign-In
- [ ] Verify user creation works

### Hardware Setup
- [ ] Arduino/ESP32 with sensors
- [ ] MAX30102 connected (I2C)
- [ ] MLX90614 connected (I2C)
- [ ] BLE firmware uploaded
- [ ] Test sensor readings

### Testing
- [ ] User registration works
- [ ] Login works (Email + Google)
- [ ] BLE connection works
- [ ] Sensor data displays
- [ ] Data saves to database
- [ ] Historical charts work
- [ ] CSV export works

---

## 📞 Support Information

### Firebase Documentation
- Firebase Console: https://console.firebase.google.com
- Firebase Auth Docs: https://firebase.google.com/docs/auth
- Android Setup: https://firebase.google.com/docs/android/setup

### Sensor Documentation
- MAX30102: https://datasheets.maximintegrated.com/en/ds/MAX30102.pdf
- MLX90614: https://www.melexis.com/en/product/MLX90614

### App Documentation
- See `DOCUMENTATION_INDEX.md` for complete documentation
- See `PROJECT_STATUS_FINAL.md` for project overview
- See `QUICK_START.md` for quick reference

---

## ✅ Summary for Client

**Database Architecture:**
- **Primary:** Room (SQLite) - All health data stored locally
- **Secondary:** Firebase - Authentication ONLY

**Sensors:**
- **MAX30102:** Heart Rate + SpO2
- **MLX90614:** Temperature

**Firebase Usage:**
- Authentication only
- No health data in cloud
- Low/zero cost
- Privacy-focused

**Client Access Needed:**
- Firebase Console access (Owner/Editor role)
- Ability to manage users
- View authentication logs

**No Additional Costs:**
- Room database is free (local)
- Firebase auth is free (within limits)
- No Firestore/Realtime Database costs

---

*Last Updated: Current Session*
*Document: Client Handover Guide*
