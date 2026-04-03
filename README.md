# Child Health Monitor 👶💙

A professional Android health monitoring application for children using BLE wearable devices. Features real-time vital signs monitoring, historical data analysis, and a modern Material You interface designed for worried parents.

![Status](https://img.shields.io/badge/Status-Complete-success)
![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Java-orange)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue)

---

## 📋 Quick Links

- **[Quick Start Guide](QUICK_START.md)** - Get up and running in 5 minutes
- **[Complete Documentation Index](DOCUMENTATION_INDEX.md)** - All documentation organized
- **[Project Status](PROJECT_STATUS_FINAL.md)** - Complete project overview
- **[Testing Guide](MODERN_DASHBOARD_TESTING_GUIDE.md)** - How to test the app
- **[App Structure](APP_STRUCTURE_VISUAL.md)** - Visual architecture guide

---

## 🎯 Overview

**Child Health Monitor** is a comprehensive Android application for monitoring children's vital signs through Bluetooth Low Energy (BLE) wearable devices. The app provides real-time monitoring of heart rate, blood oxygen (SpO2), and body temperature, with intelligent health status evaluation and historical data analysis.

### Target Audience
- 👨‍👩‍👧‍👦 Parents monitoring their child's health
- 🏥 Healthcare professionals
- 👶 Pediatric care facilities
- 🏠 Home health monitoring

### Key Capabilities
- **Real-time vital signs monitoring** with <1s latency
- **Intelligent health status evaluation** (Good/Warning/Critical)
- **Historical data analysis** with interactive charts
- **Local data storage** for offline access
- **Modern Material You design** with medical theming
- **Supports Android 7.0+** (API 24+)

---

## ✨ Features

### 🔴 Real-Time Monitoring
- **Heart Rate** - Continuous BPM monitoring (60-120 normal range)
- **Blood Oxygen (SpO2)** - Oxygen saturation percentage (≥95% normal)
- **Body Temperature** - Non-contact temperature sensing (36.0-37.5°C normal)
- **Health Status Evaluation** - Three-level system (Good/Warning/Critical)
- **Animated Status Indicators** - Pulse animation for heart, blinking for critical
- **Trend Arrows** - Visual indicators for increasing/decreasing values

### 📊 Historical Analysis
- **Interactive Line Charts** - Three separate charts for HR, SpO2, and Temperature
- **Time Range Filtering** - View data for 24 hours, 7 days, or 30 days
- **Statistical Analysis** - Average, min, max for all vitals
- **Status Distribution** - Breakdown of Good/Warning/Critical readings
- **CSV Export** - Export data for external analysis
- **Data Sharing** - Share reports with healthcare providers

### 🎨 Modern UI/UX
- **Material You Design** - Latest Material Design 3 principles across ALL pages
- **Medical Color Theme** - Professional blue/green palette with gradient backgrounds
- **Large Readable Values** - 40-64sp font sizes for vital signs
- **Color-Coded Status** - Green/Yellow/Red for instant recognition
- **Smooth Animations** - Pulse, blink, and scale animations
- **Icon-Enhanced Cards** - Large icons (28-80dp) with colored backgrounds
- **Child-Friendly** - Approachable but professional aesthetic
- **High Accessibility** - WCAG AA compliant, high contrast
- **Consistent Design** - All 5 pages redesigned with modern aesthetic

### 🔐 Authentication
- **Firebase Authentication** - Secure user management
- **Email/Password Login** - Traditional authentication
- **Google Sign-In** - One-tap social login
- **Auto-Login** - Seamless app experience
- **Session Management** - Secure token handling

### 💾 Data Storage
- **Room Database** - Local SQLite storage
- **Offline-First** - Works without internet
- **Efficient Storage** - Smart data persistence strategy
- **Background Threading** - Non-blocking database operations
- **Query Optimization** - Fast data retrieval

### 🔌 BLE Connectivity
- **Automatic Scanning** - Find devices quickly
- **One-Tap Connection** - Simple pairing process
- **Real-Time Data Streaming** - Continuous vital signs updates
- **Connection State Monitoring** - Visual connection status
- **Error Handling** - Graceful failure recovery

---

## 🏗️ Architecture

### MVVM Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                        UI LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Modern     │  │   History    │  │   Settings   │      │
│  │  Dashboard   │  │   Fragment   │  │   Fragment   │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                  │               │
│         └─────────────────┴──────────────────┘               │
│                           │                                  │
├───────────────────────────┼──────────────────────────────────┤
│                    VIEWMODEL LAYER                           │
│         ┌─────────────────┴─────────────────┐               │
│         │   HealthMonitorViewModel          │               │
│         │   • Manages BLE connection        │               │
│         │   • Exposes LiveData              │               │
│         │   • Handles data storage          │               │
│         └─────────────┬───────────────────┬─┘               │
│                       │                   │                 │
├───────────────────────┼───────────────────┼─────────────────┤
│                   DATA LAYER                                 │
│         ┌─────────────┴──────┐   ┌────────┴────────┐        │
│         │  HealthDataRepo    │   │  BLE Manager    │        │
│         │  • Room Database   │   │  • Scanning     │        │
│         │  • Background ops  │   │  • Connection   │        │
│         └────────────────────┘   └─────────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

For detailed architecture diagrams, see [APP_STRUCTURE_VISUAL.md](APP_STRUCTURE_VISUAL.md)

---

## 🔧 Technology Stack

### Core
- **Language:** Java 17
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)
- **Build System:** Gradle with Kotlin DSL

### Key Libraries
| Library | Purpose |
|---------|---------|
| **AndroidX Lifecycle** | ViewModel & LiveData |
| **Room** | Local database |
| **Firebase Auth** | User authentication |
| **MPAndroidChart** | Data visualization |
| **Material Components** | Material Design 3 UI |
| **Navigation Component** | Fragment navigation |
| **Timber** | Logging |

---

## 🚀 Quick Start

### Prerequisites
- Android Studio (latest version)
- Java 17 installed
- Android device with BLE support
- Arduino/ESP32 with sensors (for hardware testing)

### Build & Run

```bash
# 1. Open in Android Studio
File → Open → Select project folder

# 2. Sync Gradle
File → Sync Project with Gradle Files

# 3. Build
./gradlew clean assembleDebug

# 4. Run
Click "Run" ▶️ in Android Studio
```

For detailed instructions, see [QUICK_START.md](QUICK_START.md)

---

## 📱 Usage

### First Launch
1. Launch app → Splash screen
2. Login with email or Google
3. Grant Bluetooth permissions
4. Navigate to Dashboard

### Connect Device
1. Click "Connect Device" button
2. App scans for BLE devices
3. Auto-connects when found
4. Real-time data displays

### View History
1. Click "History" button
2. Select time range (24h, 7d, 30d)
3. View charts and statistics
4. Export data if needed

For complete usage guide, see [QUICK_START.md](QUICK_START.md)

---

## 📡 BLE Specification

### GATT Configuration

```yaml
Service UUID: 4fafc201-1fb5-459e-8fcc-c5c9c331914b

Characteristics:
  - HR & SpO2: beb5483e-36e1-4688-b7f5-ea07361b26a8
  - Temperature: cba1d466-344c-4be3-ab3f-189f80dd7518
```

### Data Format

```
Heart Rate & SpO2 (4 bytes):
  [0] = Heart Rate (BPM)
  [1] = SpO2 (%)
  [2] = HR Valid flag (0/1)
  [3] = Reserved

Temperature (4 bytes):
  [0-3] = Temperature (float, °C)
```

### Hardware Requirements
- **Sensors:** MAX30102 (HR+SpO2), MLX90614 (Temp)
- **Microcontroller:** Arduino Nano 33 BLE or ESP32
- **BLE:** 4.0 or higher

Arduino code available in project root.

---

## 📂 Project Structure

```
app/src/main/
├── java/com/example/sensorycontrol/
│   ├── activities/          # Login, Signup, Main, Splash
│   ├── fragments/           # Dashboard, History, Settings
│   ├── viewmodels/          # HealthMonitorViewModel
│   ├── ble/                 # BLE connection & parsing
│   ├── database/            # Room entities & DAOs
│   ├── repository/          # Data access layer
│   ├── models/              # Data models
│   ├── utils/               # Charts, statistics, export
│   ├── adapters/            # RecyclerView adapters
│   └── auth/                # Authentication manager
├── res/
│   ├── layout/              # XML layouts
│   ├── drawable/            # Icons, backgrounds
│   ├── anim/                # Animations
│   ├── values/              # Colors, strings, themes
│   └── navigation/          # Navigation graph
└── AndroidManifest.xml
```

---

## 📚 Documentation

### Essential Docs
- **[QUICK_START.md](QUICK_START.md)** - Get started in 5 minutes
- **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)** - Complete doc index
- **[PROJECT_STATUS_FINAL.md](PROJECT_STATUS_FINAL.md)** - Project overview
- **[APP_STRUCTURE_VISUAL.md](APP_STRUCTURE_VISUAL.md)** - Visual architecture

### Implementation Guides
- **[MODERN_DASHBOARD_COMPLETE.md](MODERN_DASHBOARD_COMPLETE.md)** - Dashboard implementation
- **[PHASE_7_IMPLEMENTATION_COMPLETE.md](PHASE_7_IMPLEMENTATION_COMPLETE.md)** - Charts & analytics
- **[PHASE_6_IMPLEMENTATION_COMPLETE.md](PHASE_6_IMPLEMENTATION_COMPLETE.md)** - Database
- **[PHASE_5_IMPLEMENTATION_COMPLETE.md](PHASE_5_IMPLEMENTATION_COMPLETE.md)** - Real-time monitoring
- **[PHASE_4_AUTHENTICATION_COMPLETE.md](PHASE_4_AUTHENTICATION_COMPLETE.md)** - Authentication

### Testing Guides
- **[MODERN_DASHBOARD_TESTING_GUIDE.md](MODERN_DASHBOARD_TESTING_GUIDE.md)** - Dashboard tests
- **[PHASE_7_TESTING_GUIDE.md](PHASE_7_TESTING_GUIDE.md)** - History tests
- **[PHASE_6_TESTING_GUIDE.md](PHASE_6_TESTING_GUIDE.md)** - Database tests

### Setup Guides
- **[FIREBASE_SETUP_INSTRUCTIONS.md](FIREBASE_SETUP_INSTRUCTIONS.md)** - Firebase config
- **[GOOGLE_SIGNIN_SETUP_GUIDE.md](GOOGLE_SIGNIN_SETUP_GUIDE.md)** - Google Sign-In
- **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - General setup

---

## 🎯 Health Status Thresholds

### Heart Rate
- **GOOD:** 60-120 BPM
- **WARNING:** 40-59 or 121-150 BPM
- **CRITICAL:** <40 or >150 BPM

### SpO2
- **GOOD:** ≥95%
- **WARNING:** 90-94%
- **CRITICAL:** <90%

### Temperature
- **GOOD:** 36.0-37.5°C
- **WARNING:** 37.6-38.4°C or <36.0°C
- **CRITICAL:** ≥38.5°C

---

## 🧪 Testing

### Manual Testing
See [MODERN_DASHBOARD_TESTING_GUIDE.md](MODERN_DASHBOARD_TESTING_GUIDE.md) for:
- Visual testing checklist
- Functional testing scenarios
- Animation testing
- Edge case testing
- Performance checks

### Test Coverage
- ✅ Authentication flow
- ✅ BLE connection
- ✅ Data parsing
- ✅ Database operations
- ✅ Chart rendering
- ✅ UI responsiveness
- ✅ Navigation
- ✅ Lifecycle management

---

## 🚀 Future Enhancements

### Planned Features
- [ ] Push notifications for critical events
- [ ] Background monitoring service
- [ ] Multiple child profiles
- [ ] Customizable thresholds
- [ ] Dark mode support
- [ ] Battery level display
- [ ] Connection quality indicator
- [ ] Haptic feedback
- [ ] Sound alerts
- [ ] Home screen widget

---

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

## 👥 Authors

- **Development Team** - Complete implementation

---

## 🙏 Acknowledgments

- Material Design 3 Guidelines
- Android BLE Documentation
- MPAndroidChart Library
- Firebase Authentication
- Room Persistence Library

---

## 📞 Support

For questions or issues:
- Check [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)
- Review [QUICK_START.md](QUICK_START.md)
- See [PROJECT_STATUS_FINAL.md](PROJECT_STATUS_FINAL.md)

---

<div align="center">

**Built with ❤️ for child health monitoring**

**Status:** ✅ Complete and Ready for Testing

[Documentation](DOCUMENTATION_INDEX.md) • [Quick Start](QUICK_START.md) • [Testing](MODERN_DASHBOARD_TESTING_GUIDE.md)

</div>
