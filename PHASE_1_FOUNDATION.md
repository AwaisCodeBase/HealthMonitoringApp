# Phase 1: Foundation & Scope Definition

## Project Overview

**Project Title:** Android-Based Healthcare Monitoring Application for Children

**Academic Context:** BSc (Hons) Computer Science Final Year Project

**System Description:** An integrated healthcare monitoring solution comprising a wearable Arduino-based device that collects vital signs from children and transmits real-time data to an Android application via Bluetooth Low Energy (BLE). The system enables parents and caregivers to monitor health metrics and receive alerts for abnormal readings.

---

## 1. Project Scope

### 1.1 In Scope

The system will deliver the following capabilities:

- **User Authentication System:** Secure email and password-based authentication using Firebase Authentication
- **Real-Time Vital Signs Monitoring:** Continuous display of heart rate, SpO₂, and body temperature readings
- **BLE Communication Layer:** Reliable wireless data transmission between Arduino device and Android application
- **Local Data Persistence:** Historical storage of sensor readings using Room database
- **Historical Data Visualization:** Time-series display of past vital sign measurements
- **Alert Generation System:** Automated detection and notification of abnormal vital sign readings
- **User Profile Management:** Basic account management functionality (login, signup, logout)
- **Foreground Service Implementation:** Background data collection while app is in use

### 1.2 Out of Scope

The following features are intentionally excluded from this project phase:

- Cloud-based data storage and synchronization
- Multi-user family account management
- Integration with external healthcare systems or APIs
- Medication tracking or reminder functionality
- Appointment scheduling features
- Telemedicine or video consultation capabilities
- Machine learning-based predictive analytics
- Wearable device battery management interface
- Data export to PDF or other formats
- Social sharing features
- Multi-language support

---

## 2. Functional Requirements

### FR1: User Authentication
- **FR1.1:** Users must register using email and password
- **FR1.2:** Users must authenticate before accessing application features
- **FR1.3:** System shall maintain user session across app restarts
- **FR1.4:** Users can securely log out from the application
- **FR1.5:** Password reset functionality via email

### FR2: Device Connection Management
- **FR2.1:** Application shall scan for available BLE devices
- **FR2.2:** Users can select and pair with Arduino device
- **FR2.3:** System shall maintain persistent connection during monitoring
- **FR2.4:** Application shall handle connection loss gracefully
- **FR2.5:** Connection status shall be visible to users

### FR3: Real-Time Data Display
- **FR3.1:** Display current heart rate (BPM)
- **FR3.2:** Display current blood oxygen saturation (SpO₂ %)
- **FR3.3:** Display current body temperature (°C)
- **FR3.4:** Update readings at minimum 1-second intervals
- **FR3.5:** Display timestamp of last received reading

### FR4: Historical Data Management
- **FR4.1:** Store all received sensor readings locally
- **FR4.2:** Display historical data in chronological order
- **FR4.3:** Filter data by date range
- **FR4.4:** Visualize trends using line charts
- **FR4.5:** Calculate and display average values per session

### FR5: Alert System
- **FR5.1:** Generate alerts when heart rate exceeds safe thresholds
- **FR5.2:** Generate alerts when SpO₂ falls below safe thresholds
- **FR5.3:** Generate alerts when temperature indicates fever
- **FR5.4:** Display alert notifications in-app
- **FR5.5:** Maintain alert history for review

---

## 3. Non-Functional Requirements

### NFR1: Performance
- **NFR1.1:** Application launch time shall not exceed 3 seconds
- **NFR1.2:** BLE data transmission latency shall not exceed 500ms
- **NFR1.3:** UI shall remain responsive during data collection
- **NFR1.4:** Database queries shall complete within 200ms
- **NFR1.5:** Support continuous monitoring for minimum 2 hours

### NFR2: Reliability
- **NFR2.1:** System shall recover from BLE disconnections automatically
- **NFR2.2:** No data loss during temporary connection interruptions
- **NFR2.3:** Application crash rate shall be below 1%
- **NFR2.4:** Data integrity maintained across app lifecycle events

### NFR3: Security
- **NFR3.1:** User credentials encrypted in transit and at rest
- **NFR3.2:** Local database encrypted using SQLCipher
- **NFR3.3:** BLE communication uses pairing and bonding
- **NFR3.4:** Session tokens expire after 24 hours of inactivity
- **NFR3.5:** Compliance with GDPR data protection principles

### NFR4: Usability
- **NFR4.1:** Interface follows Material Design guidelines
- **NFR4.2:** Critical information visible without scrolling
- **NFR4.3:** Alert notifications clearly distinguishable
- **NFR4.4:** Maximum 3 taps to reach any feature
- **NFR4.5:** Suitable for users with basic smartphone literacy

### NFR5: Battery Efficiency
- **NFR5.1:** Background service optimized for minimal battery drain
- **NFR5.2:** BLE scanning limited to user-initiated actions
- **NFR5.3:** Screen wake locks used only when necessary
- **NFR5.4:** Database operations batched to reduce I/O

---

## 4. User Roles and Access Control

### 4.1 Primary User Role: Parent/Caregiver

**Responsibilities:**
- Monitor child's vital signs in real-time
- Review historical health data
- Respond to health alerts
- Manage device connections
- Configure alert thresholds

**Access Permissions:**
- Full access to all application features post-authentication
- Read and write access to own data only
- Cannot access other users' data

### 4.2 Access Limitations

- **Unauthenticated Users:** No access to any application features beyond login/signup screens
- **Data Isolation:** Each authenticated user can only access their own monitoring data
- **Device Pairing:** One device can be paired to one user account at a time

---

## 5. Application Flow

### 5.1 Initial Launch Flow

```
App Launch
    ↓
Check Authentication State
    ↓
    ├─→ [Authenticated] → Navigate to Dashboard
    │
    └─→ [Not Authenticated] → Display Login Screen
            ↓
            ├─→ Login with Credentials → Dashboard
            │
            └─→ Navigate to Signup → Create Account → Dashboard
```

### 5.2 Main Application Flow

```
Dashboard (Post-Authentication)
    ↓
    ├─→ Scan Fragment
    │       ↓
    │   Scan for BLE Devices
    │       ↓
    │   Select Device → Pair → Connect
    │       ↓
    │   Navigate to Monitoring
    │
    ├─→ Monitoring Fragment
    │       ↓
    │   Display Real-Time Data
    │       ↓
    │   Store Data Locally
    │       ↓
    │   Evaluate Alert Conditions
    │       ↓
    │   [If Abnormal] → Trigger Alert
    │
    └─→ Settings Fragment
            ↓
        View Historical Data
            ↓
        Configure Alert Thresholds
            ↓
        Logout
```

---

## 6. High-Level System Architecture

### 6.1 Component Overview

```
┌─────────────────────────────────────────────────────────┐
│                    HARDWARE LAYER                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Sensors: MAX30102 (HR/SpO₂) + MLX90614 (Temp)  │  │
│  └────────────────────┬─────────────────────────────┘  │
│                       │                                  │
│  ┌────────────────────▼─────────────────────────────┐  │
│  │         Arduino Nano 33 BLE                       │  │
│  │  - Data acquisition from sensors                  │  │
│  │  - BLE GATT Server implementation                 │  │
│  │  - Data formatting and transmission               │  │
│  └────────────────────┬─────────────────────────────┘  │
└─────────────────────────┼───────────────────────────────┘
                          │ BLE Communication
┌─────────────────────────▼───────────────────────────────┐
│                  ANDROID APPLICATION                     │
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │           BLE Communication Layer                  │  │
│  │  - Device scanning and connection                  │  │
│  │  - GATT client operations                          │  │
│  │  - Characteristic notifications                    │  │
│  └────────────────────┬──────────────────────────────┘  │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐  │
│  │         Foreground Service                         │  │
│  │  - Maintains BLE connection                        │  │
│  │  - Continuous data collection                      │  │
│  │  - Lifecycle management                            │  │
│  └────────────────────┬──────────────────────────────┘  │
│                       │                                  │
│  ┌────────────────────▼──────────────────────────────┐  │
│  │         ViewModel Layer (MVVM)                     │  │
│  │  - Business logic                                  │  │
│  │  - LiveData/StateFlow management                   │  │
│  │  - Alert evaluation logic                          │  │
│  └────────┬───────────────────────────┬───────────────┘  │
│           │                           │                  │
│  ┌────────▼──────────┐    ┌──────────▼──────────────┐  │
│  │   UI Layer        │    │  Data Layer              │  │
│  │  - Fragments      │    │  - Room Database         │  │
│  │  - Activities     │    │  - Repository Pattern    │  │
│  │  - Data Binding   │    │  - Data Entities         │  │
│  └───────────────────┘    └──────────────────────────┘  │
│                                                           │
│  ┌───────────────────────────────────────────────────┐  │
│  │         Firebase Authentication                    │  │
│  │  - User management                                 │  │
│  │  - Session handling                                │  │
│  └───────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
```

### 6.2 Component Interactions

**Sensor → Arduino:**
- I2C protocol for sensor communication
- Continuous polling at 1Hz frequency
- Data validation and error handling

**Arduino → Android (BLE):**
- GATT server exposes custom service UUID
- Three characteristics for HR, SpO₂, and temperature
- Notification-based data push model

**BLE Layer → Foreground Service:**
- Service maintains persistent connection
- Handles connection state changes
- Buffers data during temporary disconnections

**Foreground Service → ViewModel:**
- LiveData streams for real-time updates
- Decoupled architecture using observer pattern
- Thread-safe data passing

**ViewModel → UI:**
- Data binding for automatic UI updates
- State management using StateFlow
- Navigation component for screen transitions

**ViewModel → Repository → Database:**
- Repository pattern abstracts data source
- Room database for structured storage
- Coroutines for asynchronous operations

---

## 7. Data Flow Description

### 7.1 Sensor Data Collection Flow

1. **Hardware Acquisition:**
   - MAX30102 sensor measures heart rate and SpO₂ via photoplethysmography
   - MLX90614 sensor measures body temperature via infrared
   - Arduino polls sensors every 1000ms

2. **Data Transmission:**
   - Arduino formats data into byte arrays
   - Data transmitted via BLE GATT notifications
   - Each characteristic updated independently

3. **Android Reception:**
   - BLE callback receives characteristic change notifications
   - DeviceController parses byte arrays into typed values
   - Data validated for range and integrity

4. **Service Processing:**
   - Foreground service receives parsed data
   - Timestamps added to each reading
   - Data broadcast to active ViewModels

5. **ViewModel Processing:**
   - Alert thresholds evaluated
   - If abnormal: Alert object created and displayed
   - Data passed to Repository for persistence

6. **Database Storage:**
   - Repository inserts reading into Room database
   - Asynchronous operation using Kotlin coroutines
   - Indexed by timestamp for efficient queries

7. **UI Update:**
   - LiveData observers notified of new data
   - UI components update automatically via data binding
   - Charts and graphs recalculated

### 7.2 Alert Generation Flow

```
New Sensor Reading Received
    ↓
ViewModel Evaluates Against Thresholds
    ↓
    ├─→ [Normal] → Update UI Only
    │
    └─→ [Abnormal] → Create Alert Object
            ↓
        Store Alert in Database
            ↓
        Display In-App Notification
            ↓
        Update Alert History List
```

**Alert Thresholds (Pediatric Standards):**
- Heart Rate: 60-100 BPM (normal range)
- SpO₂: Below 95% (alert threshold)
- Temperature: Above 38°C (fever threshold)

---

## 8. Technology Stack Justification

### 8.1 Android Platform

**Rationale:**
- **Market Penetration:** Android holds 70%+ global smartphone market share
- **BLE Support:** Native Android BLE APIs since API level 18
- **Development Ecosystem:** Mature tooling (Android Studio, Gradle)
- **Academic Relevance:** Industry-standard mobile development platform
- **Hardware Compatibility:** Broad device support for BLE functionality

### 8.2 Firebase Authentication

**Rationale:**
- **Rapid Implementation:** Pre-built authentication flows reduce development time
- **Security:** Industry-standard OAuth 2.0 and OpenID Connect protocols
- **Scalability:** Cloud-based infrastructure handles user growth
- **Academic Appropriateness:** Focus on core functionality rather than auth infrastructure
- **Cost:** Free tier sufficient for project scope
- **Integration:** Native Android SDK with minimal configuration

### 8.3 Bluetooth Low Energy (BLE)

**Rationale:**
- **Power Efficiency:** Designed for battery-powered wearable devices
- **Range:** 10-30 meter range suitable for personal monitoring
- **Standardization:** Universal protocol supported by Arduino and Android
- **Data Rate:** Sufficient bandwidth for vital sign transmission
- **Pairing Security:** Built-in encryption and authentication mechanisms
- **No Internet Dependency:** Direct device-to-device communication

### 8.4 Room Database

**Rationale:**
- **Official Support:** Google's recommended persistence library for Android
- **Type Safety:** Compile-time verification of SQL queries
- **LiveData Integration:** Seamless reactive data updates
- **Migration Support:** Structured database versioning
- **Performance:** SQLite-based with optimized query execution
- **Offline-First:** No network dependency for data access
- **Academic Fit:** Demonstrates understanding of local data persistence patterns

---

## 9. Definition of Done

The system will be considered successfully implemented when all of the following criteria are met:

### 9.1 Functional Completeness
- [ ] User can register, login, and logout successfully
- [ ] Application scans and displays available BLE devices
- [ ] User can connect to Arduino device and maintain stable connection
- [ ] Real-time vital signs display with <1 second latency
- [ ] All sensor readings stored in local database
- [ ] Historical data viewable with date filtering
- [ ] Alerts generated for abnormal readings
- [ ] Alert notifications displayed in-app

### 9.2 Technical Requirements
- [ ] Application builds without errors on Android Studio
- [ ] Minimum SDK: API 26 (Android 8.0)
- [ ] Target SDK: API 34 (Android 14)
- [ ] No critical or high-severity bugs in issue tracker
- [ ] Code follows Android architecture best practices (MVVM)
- [ ] Database schema documented and versioned

### 9.3 Performance Benchmarks
- [ ] App launch time < 3 seconds on mid-range device
- [ ] BLE connection established within 5 seconds
- [ ] UI remains responsive during continuous monitoring
- [ ] Database queries complete within 200ms
- [ ] No memory leaks detected during 30-minute monitoring session

### 9.4 Security Validation
- [ ] Firebase Authentication successfully validates users
- [ ] Local database encrypted
- [ ] BLE pairing requires user confirmation
- [ ] No sensitive data logged in production builds

### 9.5 Documentation
- [ ] User manual created with screenshots
- [ ] Technical documentation covers architecture and data flow
- [ ] Setup guide for Arduino device configuration
- [ ] API documentation for BLE characteristics
- [ ] Database schema documented

### 9.6 Testing
- [ ] Unit tests for ViewModel logic (>70% coverage)
- [ ] Integration tests for database operations
- [ ] Manual testing checklist completed
- [ ] BLE communication tested with physical hardware
- [ ] Authentication flow tested end-to-end

### 9.7 Academic Requirements
- [ ] Project aligns with dissertation proposal
- [ ] Demonstrates technical competency appropriate for BSc level
- [ ] Sufficient complexity for final year project
- [ ] Ethical considerations documented
- [ ] Risk assessment completed

---

## 10. Success Criteria

The project will be deemed successful if:

1. **Core Functionality:** All functional requirements (FR1-FR5) are implemented and operational
2. **Hardware Integration:** Reliable BLE communication demonstrated with Arduino device
3. **Data Integrity:** No data loss during normal operation and connection interruptions
4. **User Experience:** Application is intuitive and requires minimal training
5. **Academic Standards:** Project meets university assessment criteria for final year projects
6. **Demonstration Readiness:** System can be reliably demonstrated during viva examination

---

## 11. Risk Assessment

### High-Priority Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| BLE connection instability | High | Implement automatic reconnection logic and connection state monitoring |
| Sensor data accuracy | High | Calibrate sensors against medical-grade devices; document limitations |
| Firebase service outage | Medium | Implement offline authentication caching; document dependency |
| Battery drain on Android device | Medium | Optimize foreground service; implement configurable monitoring intervals |

### Medium-Priority Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Arduino hardware failure | Medium | Maintain backup hardware; document assembly process |
| Database corruption | Medium | Implement regular data validation; export functionality |
| Android version fragmentation | Low | Target API 26+ (covers 95% of devices) |

---

## 12. Project Constraints

### 12.1 Time Constraints
- Development period: 12 weeks
- Dissertation submission deadline: [Insert Date]
- Viva examination: [Insert Date]

### 12.2 Technical Constraints
- Single-user system (no multi-user support)
- Local data storage only (no cloud sync)
- Android platform only (no iOS version)
- English language only

### 12.3 Resource Constraints
- Solo developer project
- Limited to personal hardware (Arduino, Android device)
- No budget for commercial APIs or services
- Academic license restrictions

---

## 13. Next Steps (Phase 2 Preview)

Upon completion and approval of Phase 1, the project will proceed to:

**Phase 2: Hardware Stabilization**
- Arduino firmware development
- Sensor integration and calibration
- BLE GATT server implementation
- Hardware testing and validation

---

**Document Version:** 1.0  
**Date:** January 21, 2026  
**Status:** Foundation Phase Complete - Pending Review  
**Author:** [Your Name]  
**Academic Supervisor:** [Supervisor Name]

