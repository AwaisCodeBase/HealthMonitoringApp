# Child Health Monitor - Visual App Structure

## 📱 App Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      APP LAUNCH                              │
│                    SplashActivity                            │
│                  (Auto-login check)                          │
└────────────────────┬────────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
    Not Logged In           Logged In
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────────────────────────┐
│  LoginActivity  │    │         MainActivity                 │
│                 │    │    (Bottom Navigation)               │
│  • Email/Pass   │    │                                      │
│  • Google Sign  │    │  ┌────────────────────────────────┐ │
│  • Sign Up Link │    │  │   ModernDashboardFragment      │ │
└─────────────────┘    │  │   (Default Home Screen)        │ │
         │             │  │                                │ │
         │             │  │  • Connection Status Badge     │ │
         └─────────────┤  │  • Large Health Status Dot     │ │
                       │  │  • Heart Rate Card (animated)  │ │
                       │  │  • SpO2 Card                   │ │
                       │  │  • Temperature Card            │ │
                       │  │  • Connect/Disconnect Button   │ │
                       │  │  • History Button              │ │
                       │  └────────────────────────────────┘ │
                       │                                      │
                       │  ┌────────────────────────────────┐ │
                       │  │    HistoryFragment             │ │
                       │  │                                │ │
                       │  │  • Time Range Buttons          │ │
                       │  │  • 3 Line Charts (HR/SpO2/Temp)│ │
                       │  │  • Statistics Card             │ │
                       │  │  • Record List (RecyclerView)  │ │
                       │  │  • Export CSV Button           │ │
                       │  └────────────────────────────────┘ │
                       │                                      │
                       │  ┌────────────────────────────────┐ │
                       │  │    ScanFragment                │ │
                       │  │                                │ │
                       │  │  • Device List                 │ │
                       │  │  • Scan Button                 │ │
                       │  │  • Connection Status           │ │
                       │  └────────────────────────────────┘ │
                       │                                      │
                       │  ┌────────────────────────────────┐ │
                       │  │    SettingsFragment            │ │
                       │  │                                │ │
                       │  │  • User Profile                │ │
                       │  │  • Logout Button               │ │
                       │  │  • App Info                    │ │
                       │  └────────────────────────────────┘ │
                       └──────────────────────────────────────┘
```

---

## 🏗️ Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                        UI LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Activities  │  │  Fragments   │  │   Adapters   │      │
│  │              │  │              │  │              │      │
│  │ • Login      │  │ • Dashboard  │  │ • Health     │      │
│  │ • Signup     │  │ • History    │  │   Record     │      │
│  │ • Splash     │  │ • Scan       │  │              │      │
│  │ • Main       │  │ • Settings   │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────────┬────────────────────────────────────┘
                         │ Observes LiveData
┌────────────────────────▼────────────────────────────────────┐
│                   VIEWMODEL LAYER                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         HealthMonitorViewModel                       │   │
│  │                                                      │   │
│  │  • Manages BLE connection                           │   │
│  │  • Exposes LiveData for UI                          │   │
│  │  • Handles data storage strategy                    │   │
│  │  • Coordinates between layers                       │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │ Uses
┌────────────────────────▼────────────────────────────────────┐
│                   BUSINESS LOGIC LAYER                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ HealthStatus │  │HealthReading │  │ DeviceState  │      │
│  │              │  │              │  │              │      │
│  │ • Evaluate   │  │ • Parse data │  │ • Track      │      │
│  │   vitals     │  │ • Validate   │  │   connection │      │
│  │ • Determine  │  │ • Format     │  │              │      │
│  │   condition  │  │              │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────────┬────────────────────────────────────┘
                         │ Uses
┌────────────────────────▼────────────────────────────────────┐
│                     DATA LAYER                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Repository  │  │   Database   │  │  BLE Manager │      │
│  │              │  │              │  │              │      │
│  │ • Health     │  │ • AppDatabase│  │ • Health     │      │
│  │   Data Repo  │  │ • DAO        │  │   Monitor    │      │
│  │              │  │ • Entity     │  │   BLE Mgr    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────────┬────────────────────────────────────┘
                         │ Accesses
┌────────────────────────▼────────────────────────────────────┐
│                   EXTERNAL LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Firebase   │  │  SQLite DB   │  │  BLE Device  │      │
│  │              │  │              │  │              │      │
│  │ • Auth       │  │ • Local      │  │ • Arduino/   │      │
│  │ • Google     │  │   Storage    │  │   ESP32      │      │
│  │   Sign-In    │  │              │  │ • Sensors    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Data Flow

### Real-Time Monitoring Flow

```
┌─────────────────┐
│  Arduino/ESP32  │
│                 │
│  • MAX30102     │ ──┐
│    (HR + SpO2)  │   │
│  • MLX90614     │   │ BLE
│    (Temp)       │   │ Transmission
└─────────────────┘   │
                      ▼
┌─────────────────────────────────────┐
│  HealthMonitorBleManager            │
│                                     │
│  1. Scan for device                │
│  2. Connect via GATT               │
│  3. Subscribe to characteristics   │
│  4. Parse incoming data            │
│  5. Validate readings              │
└──────────────┬──────────────────────┘
               │ Callback
               ▼
┌─────────────────────────────────────┐
│  HealthMonitorViewModel             │
│                                     │
│  1. Receive HealthReading          │
│  2. Update LiveData                │
│  3. Evaluate HealthStatus          │
│  4. Store to database (strategy)   │
└──────────────┬──────────────────────┘
               │ LiveData
               ▼
┌─────────────────────────────────────┐
│  ModernDashboardFragment            │
│                                     │
│  1. Observe LiveData changes       │
│  2. Update UI values               │
│  3. Trigger animations             │
│  4. Update status indicators       │
│  5. Calculate trends               │
└─────────────────────────────────────┘
```

### Data Storage Flow

```
┌─────────────────────────────────────┐
│  HealthMonitorViewModel             │
│                                     │
│  Storage Strategy:                 │
│  • Every 10 seconds                │
│  • On status change                │
│  • Only valid data                 │
└──────────────┬──────────────────────┘
               │ Insert
               ▼
┌─────────────────────────────────────┐
│  HealthDataRepository               │
│                                     │
│  1. Receive reading + status       │
│  2. Create HealthRecordEntity      │
│  3. Execute on background thread   │
└──────────────┬──────────────────────┘
               │ DAO
               ▼
┌─────────────────────────────────────┐
│  AppDatabase (Room)                 │
│                                     │
│  health_records table:             │
│  • id (PK)                         │
│  • timestamp                       │
│  • heartRate                       │
│  • spO2                            │
│  • temperature                     │
│  • healthStatus                    │
│  • userId                          │
└─────────────────────────────────────┘
```

### Historical Data Flow

```
┌─────────────────────────────────────┐
│  HistoryFragment                    │
│                                     │
│  User selects time range:          │
│  • 24 hours                        │
│  • 7 days                          │
│  • 30 days                         │
└──────────────┬──────────────────────┘
               │ Query
               ▼
┌─────────────────────────────────────┐
│  HealthMonitorViewModel             │
│                                     │
│  getRecordsByTimeRange()           │
└──────────────┬──────────────────────┘
               │ Repository
               ▼
┌─────────────────────────────────────┐
│  HealthDataRepository               │
│                                     │
│  Query database with time range    │
└──────────────┬──────────────────────┘
               │ DAO
               ▼
┌─────────────────────────────────────┐
│  AppDatabase (Room)                 │
│                                     │
│  SELECT * FROM health_records      │
│  WHERE timestamp BETWEEN ? AND ?   │
└──────────────┬──────────────────────┘
               │ LiveData
               ▼
┌─────────────────────────────────────┐
│  HistoryFragment                    │
│                                     │
│  1. Receive List<HealthRecord>     │
│  2. Update charts (ChartHelper)    │
│  3. Calculate stats (StatsHelper)  │
│  4. Update RecyclerView            │
└─────────────────────────────────────┘
```

---

## 🎨 UI Component Hierarchy

### Modern Dashboard Screen

```
ModernDashboardFragment
│
├── CoordinatorLayout
│   │
│   ├── AppBarLayout
│   │   └── MaterialToolbar
│   │       ├── Baby Icon (ImageView)
│   │       ├── App Name + Connection Badge (LinearLayout)
│   │       │   ├── App Name (TextView)
│   │       │   └── Connection Badge (LinearLayout)
│   │       │       ├── Status Dot (View)
│   │       │       └── Status Text (TextView)
│   │       └── User Avatar (MaterialCardView)
│   │
│   └── NestedScrollView
│       └── LinearLayout (Main Content)
│           │
│           ├── Status Card (MaterialCardView)
│           │   └── LinearLayout
│           │       ├── "Overall Health Status" (TextView)
│           │       ├── Large Status Dot (View) ⭐ 48dp
│           │       └── Status Text (TextView)
│           │
│           ├── Heart Rate Card (MaterialCardView)
│           │   └── LinearLayout
│           │       ├── Icon Frame (FrameLayout)
│           │       │   ├── Background Circle (View)
│           │       │   └── Heart Icon (ImageView) ⭐ Animated
│           │       ├── Values (LinearLayout)
│           │       │   ├── "Heart Rate" Label (TextView)
│           │       │   ├── Value + Unit (LinearLayout)
│           │       │   │   ├── Value (TextView) ⭐ 48sp
│           │       │   │   └── "BPM" (TextView)
│           │       │   └── Range (TextView)
│           │       └── Trend Arrow (TextView) ⭐ ↑↓
│           │
│           ├── SpO2 Card (MaterialCardView)
│           │   └── [Similar structure to HR Card]
│           │
│           ├── Temperature Card (MaterialCardView)
│           │   └── [Similar structure to HR Card]
│           │
│           ├── Info Section (LinearLayout)
│           │   ├── Last Reading (TextView)
│           │   └── Battery Level (TextView)
│           │
│           └── Action Buttons (LinearLayout)
│               ├── Connect Device (MaterialButton)
│               └── History (MaterialButton)
```

---

## 🎭 Animation System

```
┌─────────────────────────────────────────────────────────────┐
│                    ANIMATION TRIGGERS                        │
└─────────────────────────────────────────────────────────────┘

Connection State = CONNECTED
    │
    ├──► Heart Icon Pulse Animation
    │    • Scale: 1.0 → 1.15 → 1.0
    │    • Duration: 1000ms
    │    • Repeat: Infinite
    │
    └──► Connection Badge Updates
         • Dot: Green
         • Text: "Connected"

Health Status = CRITICAL
    │
    └──► Status Dot Blink Animation
         • Alpha: 1.0 → 0.3 → 1.0
         • Duration: 500ms
         • Repeat: Infinite

Vital Value Changes
    │
    └──► Value Scale Animation
         • Scale: 1.0 → 1.1 → 1.0
         • Duration: 300ms
         • Repeat: Once

Trend Calculation
    │
    ├──► Value Increased
    │    • Show ↑ arrow
    │    • Color: Red (concerning)
    │
    └──► Value Decreased
         • Show ↓ arrow
         • Color: Green (good)

Health Status Changes
    │
    └──► Card Border Update
         • GOOD: Green border (4dp)
         • WARNING: Yellow border (4dp)
         • CRITICAL: Red border (4dp)
```

---

## 🎯 Status Evaluation Logic

```
┌─────────────────────────────────────────────────────────────┐
│                  HEALTH STATUS EVALUATION                    │
└─────────────────────────────────────────────────────────────┘

HealthReading
    │
    ├──► Heart Rate Evaluation
    │    │
    │    ├──► <40 or >150 BPM ──► CRITICAL
    │    ├──► 40-59 or 121-150 ──► WARNING
    │    └──► 60-120 BPM ──────► GOOD
    │
    ├──► SpO2 Evaluation
    │    │
    │    ├──► <90% ──────────► CRITICAL
    │    ├──► 90-94% ─────────► WARNING
    │    └──► ≥95% ───────────► GOOD
    │
    └──► Temperature Evaluation
         │
         ├──► ≥38.5°C ─────────► CRITICAL
         ├──► 37.6-38.4°C ─────► WARNING
         ├──► <36.0°C ─────────► WARNING
         └──► 36.0-37.5°C ─────► GOOD

Overall Status Determination
    │
    ├──► Any vital = CRITICAL ──► Overall = CRITICAL
    ├──► Any vital = WARNING ───► Overall = WARNING
    └──► All vitals = GOOD ─────► Overall = GOOD

UI Updates
    │
    ├──► Status Dot Color
    │    ├──► CRITICAL: Red + Blink
    │    ├──► WARNING: Yellow
    │    └──► GOOD: Green
    │
    ├──► Status Text
    │    ├──► CRITICAL: "CRITICAL"
    │    ├──► WARNING: "Attention Needed"
    │    └──► GOOD: "All Vitals Normal"
    │
    └──► Card Borders
         ├──► CRITICAL: Red (4dp)
         ├──► WARNING: Yellow (4dp)
         └──► GOOD: Green (4dp)
```

---

## 📊 Database Schema Visual

```
┌─────────────────────────────────────────────────────────────┐
│                    health_records TABLE                      │
├─────────────────────────────────────────────────────────────┤
│  id              │ INTEGER │ PRIMARY KEY │ AUTOINCREMENT    │
│  timestamp       │ INTEGER │ NOT NULL    │ Unix timestamp   │
│  heartRate       │ INTEGER │ NOT NULL    │ BPM              │
│  spO2            │ INTEGER │ NOT NULL    │ Percentage       │
│  temperature     │ REAL    │ NOT NULL    │ Celsius          │
│  healthStatus    │ TEXT    │ NOT NULL    │ GOOD/WARN/CRIT   │
│  userId          │ TEXT    │ NOT NULL    │ Firebase UID     │
└─────────────────────────────────────────────────────────────┘

Indexes:
  • timestamp (for time range queries)
  • userId (for user-specific queries)
  • healthStatus (for critical record queries)

Sample Data:
┌────┬─────────────┬────┬─────┬──────┬────────┬──────────┐
│ id │ timestamp   │ HR │ SpO2│ Temp │ Status │ userId   │
├────┼─────────────┼────┼─────┼──────┼────────┼──────────┤
│ 1  │ 1706284800  │ 85 │ 98  │ 36.8 │ GOOD   │ user123  │
│ 2  │ 1706284810  │ 87 │ 97  │ 36.9 │ GOOD   │ user123  │
│ 3  │ 1706284820  │ 125│ 93  │ 37.8 │ WARNING│ user123  │
│ 4  │ 1706284830  │ 160│ 88  │ 38.8 │ CRITICAL│user123  │
└────┴─────────────┴────┴─────┴──────┴────────┴──────────┘
```

---

## 🎨 Color System

```
┌─────────────────────────────────────────────────────────────┐
│                      COLOR PALETTE                           │
└─────────────────────────────────────────────────────────────┘

Primary Colors:
  • Primary:        #2196F3 (Medical Blue)
  • Secondary:      #4CAF50 (Medical Green)
  • Background:     #FAFAFA (Light Gray)
  • Surface:        #FFFFFF (White)

Status Colors:
  • Good:           #4CAF50 (Green)
  • Warning:        #FFB300 (Amber)
  • Critical:       #F44336 (Red)

Vital Colors:
  • Heart Rate:     #E91E63 (Pink/Red)
  • SpO2:           #2196F3 (Blue)
  • Temperature:    #FF9800 (Orange)

Text Colors:
  • Primary:        #212121 (Dark Gray)
  • Secondary:      #757575 (Medium Gray)
  • Hint:           #9E9E9E (Light Gray)

Gradient:
  • Start:          #E3F2FD (Light Blue)
  • End:            #E8F5E9 (Light Green)
```

---

## 📱 Screen States

```
┌─────────────────────────────────────────────────────────────┐
│                    DASHBOARD STATES                          │
└─────────────────────────────────────────────────────────────┘

State 1: DISCONNECTED (Initial)
  • Connection Badge: Yellow dot + "Disconnected"
  • Status Dot: Gray
  • Status Text: "Waiting for data..."
  • Vital Values: "--"
  • Trend Arrows: Hidden
  • Card Borders: None
  • Button: "Connect Device"
  • Heart Animation: Stopped

State 2: SCANNING
  • Connection Badge: Yellow dot + "Scanning..."
  • Button: "Scanning..." (disabled)
  • [Rest same as State 1]

State 3: CONNECTED - Normal Vitals
  • Connection Badge: Green dot + "Connected"
  • Status Dot: Green (solid)
  • Status Text: "All Vitals Normal"
  • Vital Values: Actual numbers (48sp)
  • Trend Arrows: Visible (↑↓)
  • Card Borders: Green (4dp)
  • Button: "Disconnect"
  • Heart Animation: Pulsing

State 4: CONNECTED - Warning Vitals
  • Connection Badge: Green dot + "Connected"
  • Status Dot: Yellow (solid)
  • Status Text: "Attention Needed"
  • Vital Values: Actual numbers
  • Trend Arrows: Visible
  • Card Borders: Yellow on affected cards
  • Button: "Disconnect"
  • Heart Animation: Pulsing

State 5: CONNECTED - Critical Vitals
  • Connection Badge: Green dot + "Connected"
  • Status Dot: Red (blinking) ⚠️
  • Status Text: "CRITICAL"
  • Vital Values: Actual numbers
  • Trend Arrows: Visible
  • Card Borders: Red on affected cards
  • Button: "Disconnect"
  • Heart Animation: Pulsing
```

---

This visual guide provides a comprehensive overview of the app's structure, data flow, and UI hierarchy. Use it as a reference for understanding how all components work together! 🎉
