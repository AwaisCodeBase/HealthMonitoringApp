# Migration Testing Guide
## WiFi + Firestore Migration - Child Health Monitor

---

## 📋 TESTING OVERVIEW

This guide provides comprehensive testing procedures for the BLE→WiFi and Room→Firestore migration.

**Testing Phases:**
1. Unit Testing (Individual components)
2. Integration Testing (Component interaction)
3. System Testing (End-to-end)
4. Performance Testing (Latency, battery, costs)
5. User Acceptance Testing (UI/UX)

---

## 🧪 PHASE 1: UNIT TESTING

### 1.1 WifiHealthMonitorManager Tests

**Test 1.1.1: Connection Establishment**
```
Objective: Verify WebSocket connection to ESP32
Steps:
1. Start ESP32 with WiFi enabled
2. Call wifiManager.connect("192.168.1.100")
3. Observe connection state changes

Expected:
- State: DISCONNECTED → CONNECTING → CONNECTED
- Callback: onConnectionStateChanged() called
- No errors

Pass Criteria:
✓ Connection established within 5 seconds
✓ State transitions correct
✓ No exceptions thrown
```

**Test 1.1.2: Data Reception**
```
Objective: Verify JSON parsing and HealthReading creation
Steps:
1. Connect to ESP32
2. Wait for health data message
3. Verify HealthReading object created

Expected:
- JSON parsed correctly
- HealthReading fields populated
- Callback: onHealthDataReceived() called

Pass Criteria:
✓ Heart rate: 40-200 BPM
✓ SpO2: 0-100%
✓ Temperature: 30-45°C
✓ Validity flags correct
```

**Test 1.1.3: Auto-Reconnect**
```
Objective: Verify reconnection after disconnect
Steps:
1. Establish connection
2. Disconnect ESP32 WiFi
3. Reconnect ESP32 WiFi
4. Observe reconnection attempts

Expected:
- Reconnection attempts: 1-5
- Exponential backoff: 3s, 6s, 9s, 12s, 15s
- Connection restored

Pass Criteria:
✓ Reconnection successful within 30 seconds
✓ No data loss during reconnection
✓ Error callback called on failure
```

**Test 1.1.4: Heartbeat Mechanism**
```
Objective: Verify keep-alive heartbeat
Steps:
1. Establish connection
2. Wait 30 seconds
3. Observe heartbeat messages

Expected:
- Heartbeat sent every 30 seconds
- Pong received from ESP32

Pass Criteria:
✓ Heartbeat interval: 30±2 seconds
✓ Connection maintained
✓ No timeouts
```

**Test 1.1.5: Error Handling**
```
Objective: Verify error handling
Steps:
1. Connect to invalid IP
2. Connect to wrong port
3. Send malformed JSON

Expected:
- Error callback called
- State: ERROR
- Descriptive error messages

Pass Criteria:
✓ No crashes
✓ Error messages clear
✓ Recovery possible
```

---

### 1.2 FirestoreHealthRepository Tests

**Test 1.2.1: Insert Record**
```
Objective: Verify Firestore write operation
Steps:
1. Create HealthReading object
2. Call repository.insertHealthRecord()
3. Check Firestore console

Expected:
- Document created in users/{uid}/health_records
- All fields present
- Timestamp correct

Pass Criteria:
✓ Document ID auto-generated
✓ Data matches input
✓ Write completes within 2 seconds
```

**Test 1.2.2: Query Records**
```
Objective: Verify Firestore read operations
Steps:
1. Insert 10 test records
2. Call repository.getAllRecords()
3. Verify LiveData updates

Expected:
- LiveData emits list of 10 records
- Records ordered by timestamp DESC
- Real-time updates work

Pass Criteria:
✓ All records retrieved
✓ Order correct
✓ LiveData updates on new inserts
```

**Test 1.2.3: Time Range Query**
```
Objective: Verify time-based filtering
Steps:
1. Insert records with different timestamps
2. Call repository.getRecordsByTimeRange(start, end)
3. Verify filtered results

Expected:
- Only records within range returned
- Ordered by timestamp ASC

Pass Criteria:
✓ Filtering accurate
✓ No records outside range
✓ Query completes within 1 second
```

**Test 1.2.4: Offline Persistence**
```
Objective: Verify offline mode
Steps:
1. Enable airplane mode
2. Insert records
3. Query records
4. Disable airplane mode

Expected:
- Writes queued locally
- Reads from cache
- Sync on reconnection

Pass Criteria:
✓ No errors in offline mode
✓ Data available from cache
✓ Sync successful on reconnection
```

**Test 1.2.5: Multi-User Isolation**
```
Objective: Verify user data separation
Steps:
1. Login as User A
2. Insert records
3. Logout and login as User B
4. Query records

Expected:
- User B sees no records from User A
- Each user has separate collection

Pass Criteria:
✓ No cross-user data access
✓ Security rules enforced
✓ User ID correct in documents
```

---

### 1.3 HealthMonitorViewModelWifi Tests

**Test 1.3.1: LiveData Updates**
```
Objective: Verify ViewModel LiveData emissions
Steps:
1. Observe LiveData objects
2. Connect to ESP32
3. Receive health data

Expected:
- currentReading LiveData updates
- heartRate, spO2, temperature update
- healthStatus evaluates correctly

Pass Criteria:
✓ All LiveData objects emit
✓ Values match received data
✓ UI can observe changes
```

**Test 1.3.2: Storage Strategy**
```
Objective: Verify 10-second batching
Steps:
1. Connect and receive data at 1 Hz
2. Monitor Firestore writes
3. Count writes over 60 seconds

Expected:
- 6 writes in 60 seconds (every 10s)
- Not 60 writes (every 1s)

Pass Criteria:
✓ Write frequency: 0.1 Hz (every 10s)
✓ No excessive writes
✓ Critical status triggers immediate write
```

**Test 1.3.3: Status Change Detection**
```
Objective: Verify immediate write on status change
Steps:
1. Receive data with GOOD status
2. Receive data with CRITICAL status
3. Monitor Firestore writes

Expected:
- Immediate write on status change
- Even if < 10 seconds since last write

Pass Criteria:
✓ Status change detected
✓ Write triggered immediately
✓ Critical data not delayed
```

---

## 🔗 PHASE 2: INTEGRATION TESTING

### 2.1 ESP32 → Android → Firestore Flow

**Test 2.1.1: End-to-End Data Flow**
```
Objective: Verify complete data pipeline
Steps:
1. Place finger on MAX30102
2. Point MLX90614 at forehead
3. Observe Android app
4. Check Firestore console

Expected:
- Sensors read data
- ESP32 sends JSON
- Android receives and parses
- Firestore stores data
- UI updates

Pass Criteria:
✓ Latency < 2 seconds (sensor → UI)
✓ Data accuracy maintained
✓ No data loss
✓ UI reflects current state
```

**Test 2.1.2: Network Failure Recovery**
```
Objective: Verify graceful degradation
Steps:
1. Establish connection
2. Disable WiFi on ESP32
3. Re-enable WiFi
4. Verify recovery

Expected:
- Connection lost detected
- Reconnection attempted
- Data flow resumes
- No crashes

Pass Criteria:
✓ Recovery within 30 seconds
✓ No data corruption
✓ UI shows connection status
✓ User notified of issue
```

**Test 2.1.3: Concurrent Users**
```
Objective: Verify multi-user support
Steps:
1. Login as User A on Device 1
2. Login as User B on Device 2
3. Both connect to ESP32 devices
4. Verify data isolation

Expected:
- Each user sees only their data
- No cross-contamination
- Firestore security rules enforced

Pass Criteria:
✓ Data isolation maintained
✓ No unauthorized access
✓ Performance not degraded
```

---

### 2.2 Firestore Offline Mode

**Test 2.2.1: Offline Write Queue**
```
Objective: Verify offline write queuing
Steps:
1. Enable airplane mode
2. Generate health data
3. Disable airplane mode
4. Verify sync

Expected:
- Writes queued locally
- Sync on reconnection
- No data loss

Pass Criteria:
✓ All queued writes synced
✓ Order preserved
✓ No duplicates
```

**Test 2.2.2: Offline Read Cache**
```
Objective: Verify cached reads
Steps:
1. Load historical data
2. Enable airplane mode
3. Navigate to history screen
4. Verify data displayed

Expected:
- Data loaded from cache
- Charts render correctly
- No errors

Pass Criteria:
✓ All cached data accessible
✓ UI fully functional
✓ No network errors shown
```

---

## 🚀 PHASE 3: SYSTEM TESTING

### 3.1 Feature Completeness

**Test 3.1.1: Dashboard**
```
Objective: Verify dashboard functionality
Steps:
1. Open dashboard
2. Connect to device
3. Observe real-time updates

Expected:
- Status dot animates
- Vital values update
- Health status evaluates
- Trend arrows show

Pass Criteria:
✓ All UI elements functional
✓ Animations smooth
✓ Data accurate
✓ No visual glitches
```

**Test 3.1.2: History & Charts**
```
Objective: Verify historical data display
Steps:
1. Generate 24 hours of data
2. Open history screen
3. Select time ranges (24h, 7d, 30d)
4. Verify charts

Expected:
- Charts render correctly
- Data points accurate
- Time range filtering works
- Statistics calculated

Pass Criteria:
✓ Charts display all data
✓ Zoom/pan functional
✓ Statistics correct
✓ Performance acceptable
```

**Test 3.1.3: CSV Export**
```
Objective: Verify data export
Steps:
1. Generate test data
2. Export to CSV
3. Open CSV file
4. Verify contents

Expected:
- CSV file created
- All fields present
- Data matches Firestore

Pass Criteria:
✓ Export completes
✓ File readable
✓ Data integrity maintained
✓ Format correct
```

**Test 3.1.4: Settings & Thresholds**
```
Objective: Verify settings functionality
Steps:
1. Open settings
2. Modify thresholds
3. Save changes
4. Verify health evaluation

Expected:
- Thresholds saved
- Health status recalculated
- Alerts triggered correctly

Pass Criteria:
✓ Settings persist
✓ Evaluation uses new thresholds
✓ UI updates immediately
```

---

### 3.2 Authentication & Multi-User

**Test 3.2.1: User Login/Logout**
```
Objective: Verify auth flow
Steps:
1. Logout current user
2. Login as different user
3. Verify data isolation

Expected:
- User switched
- Data specific to user
- No previous user data visible

Pass Criteria:
✓ Auth state correct
✓ Data isolation enforced
✓ No data leakage
```

**Test 3.2.2: Google Sign-In**
```
Objective: Verify Google auth
Steps:
1. Logout
2. Sign in with Google
3. Verify Firestore access

Expected:
- Google auth successful
- User ID from Google
- Firestore access granted

Pass Criteria:
✓ Sign-in completes
✓ User authenticated
✓ Data accessible
```

---

## ⚡ PHASE 4: PERFORMANCE TESTING

### 4.1 Latency Measurement

**Test 4.1.1: Sensor to UI Latency**
```
Objective: Measure end-to-end latency
Method:
1. Timestamp at sensor read
2. Timestamp at UI update
3. Calculate delta

Target: < 2 seconds
Acceptable: < 3 seconds
Fail: > 5 seconds

Measurements:
- Sensor read: T0
- ESP32 send: T0 + 50ms
- Android receive: T0 + 100ms
- Firestore write: T0 + 500ms
- UI update: T0 + 150ms

Total: ~150ms (excellent)
```

**Test 4.1.2: Firestore Write Latency**
```
Objective: Measure Firestore write time
Method:
1. Timestamp before write
2. Timestamp on success callback
3. Calculate delta

Target: < 500ms
Acceptable: < 1 second
Fail: > 2 seconds

Note: Varies by network conditions
```

**Test 4.1.3: Query Performance**
```
Objective: Measure query response time
Method:
1. Query 1000 records
2. Measure time to LiveData emission

Target: < 1 second
Acceptable: < 2 seconds
Fail: > 5 seconds

Optimization:
- Use indexes
- Limit results
- Cache aggressively
```

---

### 4.2 Battery Consumption

**Test 4.2.1: WiFi vs BLE Power**
```
Objective: Compare power consumption
Method:
1. Fully charge device
2. Run for 8 hours
3. Measure battery drain

BLE (baseline): ~10% drain
WiFi (expected): ~15% drain

Acceptable: < 20% drain
Fail: > 30% drain

Optimization:
- Reduce WebSocket ping frequency
- Use WiFi sleep mode
- Batch operations
```

**Test 4.2.2: ESP32 Power**
```
Objective: Measure ESP32 power consumption
Method:
1. Measure current draw
2. Calculate power usage

WiFi active: ~160mA @ 3.3V = 528mW
WiFi sleep: ~20mA @ 3.3V = 66mW

Battery life (1000mAh):
- Active: ~6 hours
- With sleep: ~40 hours

Recommendation: Use deep sleep between readings
```

---

### 4.3 Firestore Cost Analysis

**Test 4.3.1: Write Cost Monitoring**
```
Objective: Verify write frequency
Method:
1. Monitor for 24 hours
2. Count Firestore writes
3. Calculate cost

Expected: 8,640 writes/day (10s interval)
Free tier: 20,000 writes/day
Cost: $0 (within free tier)

10 users: 86,400 writes/day
Cost: $0.18/day = $5.40/month

Pass Criteria:
✓ Write frequency as expected
✓ No excessive writes
✓ Cost within budget
```

**Test 4.3.2: Read Cost Monitoring**
```
Objective: Verify read efficiency
Method:
1. Monitor for 24 hours
2. Count Firestore reads
3. Calculate cost

Expected: ~100 reads/day (with caching)
Free tier: 50,000 reads/day
Cost: $0 (within free tier)

Optimization:
- Use real-time listeners (1 read)
- Enable offline persistence
- Cache aggressively

Pass Criteria:
✓ Read count low
✓ Caching effective
✓ Cost minimal
```

---

## ✅ PHASE 5: USER ACCEPTANCE TESTING

### 5.1 UI/UX Verification

**Test 5.1.1: No Breaking Changes**
```
Objective: Verify UI unchanged
Steps:
1. Compare screenshots (before/after)
2. Test all navigation flows
3. Verify all features accessible

Expected:
- UI identical
- All buttons work
- Navigation unchanged

Pass Criteria:
✓ No visual differences
✓ No broken features
✓ User experience maintained
```

**Test 5.1.2: Connection Setup**
```
Objective: Verify ease of WiFi setup
Steps:
1. New user setup
2. Enter ESP32 IP address
3. Connect to device

Expected:
- Clear instructions
- IP address validation
- Connection feedback

Pass Criteria:
✓ Setup intuitive
✓ Error messages helpful
✓ Connection reliable
```

**Test 5.1.3: Error Messages**
```
Objective: Verify user-friendly errors
Steps:
1. Trigger various errors
2. Read error messages
3. Verify actionable

Expected:
- Clear error descriptions
- Suggested actions
- No technical jargon

Pass Criteria:
✓ Errors understandable
✓ Actions clear
✓ Recovery possible
```

---

## 📊 TEST RESULTS TEMPLATE

### Test Execution Summary

| Phase | Tests | Passed | Failed | Blocked | Pass Rate |
|-------|-------|--------|--------|---------|-----------|
| Unit Testing | 15 | - | - | - | -% |
| Integration Testing | 6 | - | - | - | -% |
| System Testing | 8 | - | - | - | -% |
| Performance Testing | 6 | - | - | - | -% |
| UAT | 3 | - | - | - | -% |
| **TOTAL** | **38** | **-** | **-** | **-** | **-%** |

### Critical Issues

| ID | Severity | Description | Status | Owner |
|----|----------|-------------|--------|-------|
| - | - | - | - | - |

### Performance Metrics

| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| Sensor→UI Latency | < 2s | - | - |
| Firestore Write | < 500ms | - | - |
| Battery Drain (8h) | < 20% | - | - |
| Daily Writes | 8,640 | - | - |
| Daily Reads | < 100 | - | - |

---

## 🎯 ACCEPTANCE CRITERIA

### Must Pass (Blocking)
- ✅ All unit tests pass
- ✅ End-to-end data flow works
- ✅ No data loss
- ✅ Multi-user isolation enforced
- ✅ UI unchanged
- ✅ Charts functional
- ✅ CSV export works

### Should Pass (Important)
- ✅ Latency < 2 seconds
- ✅ Auto-reconnect works
- ✅ Offline mode functional
- ✅ Battery drain acceptable
- ✅ Firestore costs within budget

### Nice to Have (Optional)
- ✅ Latency < 1 second
- ✅ Battery drain < 15%
- ✅ Zero Firestore costs

---

## 🚨 ROLLBACK CRITERIA

**Trigger rollback if:**
1. Data loss detected
2. Security breach
3. Critical bugs in production
4. Performance degradation > 50%
5. Firestore costs exceed budget by 200%

**Rollback procedure:**
1. Revert to BLE + Room version
2. Restore Room database from backup
3. Notify users of issue
4. Investigate root cause
5. Fix and re-test before re-deployment

---

*Last Updated: Current Session*
*Status: Testing Guide Complete*
*Next: Execute tests and document results*
