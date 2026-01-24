<div align="center">

# � Sensory Control

### Bluetooth-Enabled Sensory Therapy Device Controller

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg)](https://android-arsenal.com/api?level=23)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

*A professional Android application for controlling sensory therapy devices via Bluetooth Low Energy*

[Features](#-features) • [Architecture](#-architecture) • [Installation](#-installation) • [Documentation](#-documentation)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Installation](#-installation)
- [Usage](#-usage)
- [BLE Specification](#-ble-specification)
- [Project Structure](#-project-structure)
- [Documentation](#-documentation)
- [Contributing](#-contributing)

---

## 🎯 Overview

**Sensory Control** is a comprehensive Android application designed for controlling sensory therapy devices through Bluetooth Low Energy (BLE) connectivity. The app provides real-time control of LED lighting, sound, and vibration outputs, making it ideal for sensory therapy applications, particularly for children with special needs.

### Target Audience
- 👨‍👩‍👧‍👦 Parents and caregivers of children with sensory processing needs
- 🏥 Occupational therapists and healthcare professionals
- 🎓 Special education facilities
- � Home therapy environments

### Key Capabilities
- **Real-time device control** with <100ms response time
- **Multiple therapy modes** for different sensory needs
- **Safety features** including distance monitoring and auto-shutoff
- **Supports Android 6.0+** (API 23+) covering 99%+ devices

---

## ✨ Features

### Core Functionality

#### 🎮 Device Control
- **Power Management** - On/off control with connection state monitoring
- **LED Control** - Brightness and color adjustment for visual stimulation
- **Sound Control** - Volume and tone control for auditory therapy
- **Vibration Control** - Intensity adjustment for tactile feedback
- **Real-time Response** - Immediate device feedback with minimal latency

#### 🔍 BLE Device Management
- Automatic device scanning with RSSI signal strength display
- One-tap connection to compatible BLE devices
- Automatic reconnection with exponential backoff
- Real-time connection state visualization
- Support for custom GATT services and characteristics

#### 🎨 Therapy Modes
- **Calm Mode** - Gentle, soothing sensory output
- **Focus Mode** - Stimulating patterns for attention
- **Sensory Play** - Interactive and engaging patterns
- **Sleep Aid** - Relaxing sequences for bedtime

#### 🛡️ Safety Features
- **Distance Monitoring** - RSSI-based proximity detection
- **Auto Shut-off** - Automatic power down when device is too far
- **Configurable Limits** - User-defined safety thresholds
- **Connection Alerts** - Notifications for connection loss
- **Safety Settings** - Persistent safety configuration

#### 🌙 Modern UI/UX
- **Material Design 3** implementation
- **Full dark mode** support with automatic switching
- **Responsive layouts** for all screen sizes
- **Accessibility-compliant** design
- **Child-friendly interface** with intuitive navigation
- **Bottom navigation** for easy screen switching

#### 🔄 Background Operation
- **Foreground service** maintains BLE connection
- **Persistent notification** during active connection
- **Battery-optimized** implementation
- **Automatic service restart** if terminated

---

## 🏗️ Architecture

### Design Patterns & Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Monitoring  │  │     Scan     │  │   Settings   │      │
│  │   Fragment   │  │   Fragment   │  │   Fragment   │      │
│  │              │  │              │  │              │      │
│  │ • Device     │  │ • Device     │  │ • Safety     │      │
│  │   Control    │  │   List       │  │   Config     │      │
│  │ • Status     │  │ • RSSI       │  │ • Distance   │      │
│  │ • Modes      │  │ • Connect    │  │   Limits     │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                  │               │
│         └─────────────────┴──────────────────┘               │
│                           │                                  │
├───────────────────────────┼──────────────────────────────────┤
│                       DATA LAYER                             │
│         ┌─────────────────┴─────────────────┐               │
│         │                                    │               │
│  ┌──────▼────────┐                  ┌───────▼────────┐      │
│  │     Room      │                  │      BLE       │      │
│  │   Database    │                  │   Controller   │      │
│  │               │                  │                │      │
│  │ • Settings    │                  │ • Scanning     │      │
│  │ • History     │                  │ • Connection   │      │
│  │ • Modes       │                  │ • GATT Ops     │      │
│  └───────────────┘                  └───────┬────────┘      │
│                                              │               │
├──────────────────────────────────────────────┼───────────────┤
│                      SERVICE LAYER                           │
│                     ┌────────────────┴────────────┐          │
│                     │   Device Foreground Service │          │
│                     │                             │          │
│                     │ • Background Connection     │          │
│                     │ • Command Transmission      │          │
│                     │ • Safety Monitoring         │          │
│                     │ • Auto Reconnection         │          │
│                     └─────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

1. **Single Activity Architecture**
   - Modern Android best practice
   - Navigation Component for fragment management
   - Simplified lifecycle management

2. **MVVM Pattern**
   - Clear separation of concerns
   - Testable business logic
   - Reactive UI with LiveData

3. **Repository Pattern**
   - Centralized data management
   - Single source of truth
   - Abstracts data sources

4. **Foreground Service**
   - Reliable background BLE connection
   - Survives app backgrounding
   - User-visible notification

---

## 🔧 Technology Stack

### Core Technologies
- **Language:** Java 11
- **Min SDK:** API 23 (Android 6.0 Marshmallow)
- **Target SDK:** API 36 (Android 15)
- **Compile SDK:** API 36
- **Build System:** Gradle 8.13+ with Kotlin DSL

### Libraries & Frameworks

| Category | Library | Purpose |
|----------|---------|---------|
| **UI** | Material Components | Material Design 3 components |
| **Navigation** | Navigation Component | Fragment navigation & deep linking |
| **Database** | Room | Local SQLite database abstraction |
| **Lifecycle** | AndroidX Lifecycle | ViewModel & LiveData |
| **Charts** | MPAndroidChart | Data visualization |
| **Logging** | Timber | Enhanced debug logging |

### Android Features Used
- Bluetooth Low Energy (BLE) APIs
- Foreground Services
- Runtime Permissions
- Notification Channels
- Room Database
- LiveData & ViewModel
- Navigation Component
- Material Design 3

---

## 🚀 Installation

### Prerequisites

```bash
✓ Android Studio Ladybug or later
✓ JDK 11 or higher
✓ Android device with API 23+ (Android 6.0+)
✓ Arduino Nano 33 BLE (for hardware testing)
✓ Git for version control
```

### Step-by-Step Setup

#### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/sensory-control.git
cd sensory-control
```

#### 2. Open in Android Studio
```
File → Open → Select project folder
Wait for indexing to complete
```

#### 3. Sync Gradle Dependencies
```
File → Sync Project with Gradle Files
```

#### 4. Connect Android Device
- Enable **Developer Options** on your device
- Enable **USB Debugging**
- Connect device via USB
- Accept USB debugging prompt

#### 5. Build and Run
```
Run → Run 'app' (Shift + F10)
Or click the green play button
```

#### 6. Grant Required Permissions
On first launch, grant:
- ✅ Bluetooth permissions (SCAN & CONNECT)
- ✅ Location permission (Android < 12 only)
- ✅ Notification permission (Android 13+)

---

## 📱 Usage

### Quick Start Guide

#### Step 1: First Launch
1. Launch the app
2. Grant all requested permissions
3. Enable Bluetooth when prompted
4. App starts on **Monitor** screen

#### Step 2: Scan for Devices
1. Tap **"Scan"** tab in bottom navigation
2. Tap **"Start Scan"** button
3. Wait for devices to appear
4. Devices show with name, MAC address, and RSSI

#### Step 3: Connect to Device
1. Tap on your device
2. App automatically navigates to Monitor screen
3. Connection status changes to "Connected"
4. Device is ready for control

#### Step 4: Control Device
- **Power** - Toggle device on/off
- **LED** - Adjust brightness and color
- **Sound** - Control volume and tone
- **Vibration** - Set intensity level
- **Modes** - Select therapy mode

#### Step 5: Configure Safety Settings
1. Tap **"Settings"** tab
2. Adjust safety parameters:
   - Distance limit (RSSI threshold)
   - Auto shut-off enable/disable
   - Minimum signal strength
3. Tap **"Save Settings"**
4. Settings persist across app restarts

---

## 📡 BLE Specification

### GATT Profile Configuration

```yaml
Service:
  UUID: 0000180d-0000-1000-8000-00805f9b34fb
  Type: Primary Service
  Name: Sensory Control Service
  
Characteristic:
  UUID: 00002a37-0000-1000-8000-00805f9b34fb
  Properties: [Read, Write, Notify]
  Permissions: [Read, Write]
  Description: Device Control Commands
```

### Command Format

#### Control Commands
```
Format: "CMD:VALUE"

Commands:
  POWER:ON/OFF     - Power control
  LED:0-100        - LED brightness (0-100%)
  COLOR:RRGGBB     - LED color (hex)
  SOUND:0-100      - Sound volume (0-100%)
  TONE:100-2000    - Sound frequency (Hz)
  VIBRATE:0-100    - Vibration intensity (0-100%)
  MODE:CALM/FOCUS/PLAY/SLEEP - Therapy mode

Examples:
  "POWER:ON"
  "LED:75"
  "COLOR:FF5733"
  "MODE:CALM"
```

### Device Requirements

- BLE 4.0 or higher
- GATT server capability
- Write and notify support
- Minimum 50 bytes characteristic value
- Stable connection (RSSI > -80 dBm recommended)

---

## 📂 Project Structure

```
sensory-control/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/sensorycontrol/
│   │   │   │   ├── activities/
│   │   │   │   │   └── MainActivity.java
│   │   │   │   ├── fragments/
│   │   │   │   │   ├── MonitoringFragment.java
│   │   │   │   │   ├── ScanFragment.java
│   │   │   │   │   └── SettingsFragment.java
│   │   │   │   ├── services/
│   │   │   │   │   └── DeviceService.java
│   │   │   │   ├── ble/
│   │   │   │   │   ├── DeviceController.java
│   │   │   │   │   └── BleConstants.java
│   │   │   │   ├── models/
│   │   │   │   │   ├── DeviceState.java
│   │   │   │   │   └── TherapyMode.java
│   │   │   │   └── SensoryControlApplication.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── navigation/
│   │   │   │   ├── values/
│   │   │   │   └── menu/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── docs/
│   ├── PROJECT_DOCUMENTATION.md
│   ├── SETUP_GUIDE.md
│   ├── FEATURE_CHECKLIST.md
│   ├── QUICK_REFERENCE.md
│   └── BLE_SPECIFICATION.md
├── ARDUINO_SAMPLE_CODE.ino
├── README.md
├── build.gradle.kts
└── settings.gradle.kts
```

---

## � Documentation

### Complete Documentation Suite

| Document | Description |
|----------|-------------|
| **[PROJECT_DOCUMENTATION.md](PROJECT_DOCUMENTATION.md)** | Complete technical documentation |
| **[SETUP_GUIDE.md](SETUP_GUIDE.md)** | Setup instructions and troubleshooting |
| **[FEATURE_CHECKLIST.md](FEATURE_CHECKLIST.md)** | Feature verification and status |
| **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** | Quick reference for developers |
| **[BLE_SPECIFICATION.md](BLE_SPECIFICATION.md)** | BLE protocol specification |
| **[ARDUINO_SAMPLE_CODE.ino](ARDUINO_SAMPLE_CODE.ino)** | Arduino firmware sample |

---

## 🧪 Testing

### Manual Testing Checklist

#### Core Functionality
- [x] App launches without crashes
- [x] Permissions requested correctly
- [x] Bluetooth enable prompt works
- [x] Device scanning finds BLE devices
- [x] Device connection succeeds
- [x] Control commands work
- [x] Therapy modes function correctly

#### Safety Features
- [x] Distance monitoring works
- [x] Auto shut-off triggers correctly
- [x] Connection loss detected
- [x] Safety settings persist

#### Background Operation
- [x] Service maintains connection
- [x] Foreground notification shows
- [x] Connection maintained in background
- [x] Service survives app kill

---

## 🚀 Future Enhancements

### Planned Features

1. **Cloud Integration**
   - Session history sync
   - Multi-device management
   - Remote monitoring

2. **Advanced Therapy Modes**
   - Custom mode creation
   - Scheduled sessions
   - Progress tracking

3. **Enhanced Analytics**
   - Usage statistics
   - Session reports
   - Therapy effectiveness tracking

4. **Multi-User Support**
   - Multiple user profiles
   - Family sharing
   - Therapist access

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## � License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

- **Your Name** - *Initial work*

---

## 🙏 Acknowledgments

- Material Design 3 Guidelines
- Android BLE Documentation
- MPAndroidChart Library
- Timber Logging Library

---

## 📞 Support

For questions or issues:
- Open an issue on GitHub
- Check the [documentation](docs/)
- Review the [troubleshooting guide](SETUP_GUIDE.md)

---

<div align="center">

**Built with ❤️ for sensory therapy applications**

</div>
