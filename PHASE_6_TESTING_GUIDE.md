# Phase 6: Room Database - Testing Guide

## 🧪 Testing Checklist

### ✅ Basic Storage Tests

#### Test 1: Data Storage During Monitoring
**Steps:**
1. Connect to BLE device
2. Wait for valid readings
3. Monitor for 30 seconds
4. Check if data is being stored

**Expected:**
- Data stored every 10 seconds
- At least 3 records after 30 seconds

**Verification:**
```java
viewModel.getRecordCount(count -> {
    Log.d("Test", "Records stored: " + count);
    // Should be >= 3 after 30 seconds
});
```

#### Test 2: Status Change Triggers Storage
**Steps:**
1. Connect to device
2. Wait for GOOD status
3. Simulate WARNING condition (if possible)
4. Check immediate storage

**Expected:**
- Record stored immediately on status change
- Even if 10 seconds haven't passed

**Verification:**
```java
viewModel.getLastNRecords(5).observe(this, records -> {
    if (records != null && records.size() >= 2) {
        // Check if consecutive records have different status
        String status1 = records.get(0).getHealthStatus();
        String status2 = records.get(1).getHealthStatus();
        Log.d("Test", "Status change detected: " + status2 + " → " + status1);
    }
});
```

#### Test 3: Invalid Readings Not Stored
**Steps:**
1. Connect to device
2. Disconnect sensor (if possible)
3. Wait for invalid readings
4. Check storage

**Expected:**
- Invalid readings (hrValid=false or tempValid=false) not stored
- Record count doesn't increase

**Verification:**
```java
// Before disconnect
int countBefore = /* get count */;

// After disconnect (wait 30s)
viewModel.getRecordCount(count -> {
    Log.d("Test", "Count before: " + countBefore + ", after: " + count);
    // Count should not increase significantly
});
```

---

### ✅ Data Persistence Tests

#### Test 4: Data Survives App Restart
**Steps:**
1. Monitor for 1 minute (store ~6 records)
2. Note record count
3. Close app completely
4. Reopen app
5. Check record count

**Expected:**
- All records still present
- Count matches or is higher

**Verification:**
```java
viewModel.getRecordCount(count -> {
    Log.d("Test", "Records after restart: " + count);
    // Should be >= count before restart
});
```

#### Test 5: Data Survives Force Stop
**Steps:**
1. Monitor and store data
2. Force stop app (Settings → Apps → Force Stop)
3. Reopen app
4. Check data

**Expected:**
- All data intact
- No data loss

#### Test 6: Data Survives Device Reboot
**Steps:**
1. Store significant data (100+ records)
2. Reboot device
3. Open app
4. Verify data

**Expected:**
- All records present
- Database intact

---

### ✅ Multi-User Tests

#### Test 7: User Data Isolation
**Steps:**
1. Login as User A
2. Monitor and store data
3. Logout
4. Login as User B
5. Check records

**Expected:**
- User B sees no records (or only their own)
- User A's data not visible to User B

**Verification:**
```java
viewModel.getAllRecords().observe(this, records -> {
    Log.d("Test", "User B records: " + (records != null ? records.size() : 0));
    // Should be 0 for new user
});
```

#### Test 8: User Data Persists After Logout
**Steps:**
1. Login as User A
2. Store data
3. Logout
4. Login again as User A
5. Check records

**Expected:**
- All User A's data still present
- No data loss on logout

---

### ✅ Query Tests

#### Test 9: Get Last N Records
**Steps:**
1. Store 50+ records
2. Query last 10 records
3. Verify order and count

**Expected:**
- Exactly 10 records returned
- Ordered by timestamp (newest first)

**Verification:**
```java
viewModel.getLastNRecords(10).observe(this, records -> {
    if (records != null) {
        Log.d("Test", "Records returned: " + records.size());
        // Verify order
        for (int i = 0; i < records.size() - 1; i++) {
            long t1 = records.get(i).getTimestamp();
            long t2 = records.get(i + 1).getTimestamp();
            if (t1 < t2) {
                Log.e("Test", "Order incorrect!");
            }
        }
    }
});
```

#### Test 10: Time Range Query
**Steps:**
1. Store data over 1 hour
2. Query last 30 minutes
3. Verify results

**Expected:**
- Only records from last 30 minutes
- Correct count

**Verification:**
```java
long thirtyMinAgo = System.currentTimeMillis() - (30 * 60 * 1000L);
long now = System.currentTimeMillis();

viewModel.getRecordsByTimeRange(thirtyMinAgo, now).observe(this, records -> {
    if (records != null) {
        Log.d("Test", "Records in last 30 min: " + records.size());
        // Verify all timestamps are within range
        for (HealthRecordEntity record : records) {
            if (record.getTimestamp() < thirtyMinAgo) {
                Log.e("Test", "Record outside range!");
            }
        }
    }
});
```

#### Test 11: Critical Records Filter
**Steps:**
1. Store mix of GOOD, WARNING, CRITICAL records
2. Query critical records only
3. Verify filter

**Expected:**
- Only CRITICAL status records returned
- No GOOD or WARNING records

**Verification:**
```java
viewModel.getCriticalRecords().observe(this, records -> {
    if (records != null) {
        for (HealthRecordEntity record : records) {
            if (!"CRITICAL".equals(record.getHealthStatus())) {
                Log.e("Test", "Non-critical record in results!");
            }
        }
        Log.d("Test", "Critical records: " + records.size());
    }
});
```

---

### ✅ Statistics Tests

#### Test 12: Average Calculations
**Steps:**
1. Store known data (e.g., HR: 70, 80, 90)
2. Calculate averages
3. Verify accuracy

**Expected:**
- Correct mathematical averages
- Handles edge cases (no data, single record)

**Verification:**
```java
long start = /* first record timestamp */;
long end = /* last record timestamp */;

viewModel.getAverageVitals(start, end, (avgHR, avgSpO2, avgTemp) -> {
    Log.d("Test", String.format("Averages - HR: %.1f, SpO2: %.1f, Temp: %.1f",
        avgHR, avgSpO2, avgTemp));
    // Verify against expected values
});
```

#### Test 13: Record Count Accuracy
**Steps:**
1. Store exactly 20 records
2. Get count
3. Verify

**Expected:**
- Count = 20
- Accurate counting

**Verification:**
```java
viewModel.getRecordCount(count -> {
    Log.d("Test", "Expected: 20, Actual: " + count);
    if (count != 20) {
        Log.e("Test", "Count mismatch!");
    }
});
```

---

### ✅ Data Management Tests

#### Test 14: Delete All Records
**Steps:**
1. Store 50+ records
2. Call deleteAllRecords()
3. Verify deletion

**Expected:**
- All user's records deleted
- Count = 0

**Verification:**
```java
viewModel.deleteAllRecords();

// Wait a moment for background operation
new Handler().postDelayed(() -> {
    viewModel.getRecordCount(count -> {
        Log.d("Test", "Records after delete: " + count);
        // Should be 0
    });
}, 1000);
```

#### Test 15: Delete Old Records
**Steps:**
1. Store records with various timestamps
2. Call deleteOldRecords(7) // Keep last 7 days
3. Verify only recent records remain

**Expected:**
- Records older than 7 days deleted
- Recent records intact

**Verification:**
```java
viewModel.deleteOldRecords(7);

new Handler().postDelayed(() -> {
    viewModel.getAllRecords().observe(this, records -> {
        if (records != null) {
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
            for (HealthRecordEntity record : records) {
                if (record.getTimestamp() < sevenDaysAgo) {
                    Log.e("Test", "Old record not deleted!");
                }
            }
        }
    });
}, 1000);
```

---

### ✅ Performance Tests

#### Test 16: Storage Performance
**Steps:**
1. Monitor for 10 minutes
2. Check UI responsiveness
3. Verify no lag

**Expected:**
- No UI freezing
- Smooth operation
- Background storage doesn't block UI

**Metrics:**
- Storage operation < 100ms
- No ANR (Application Not Responding)

#### Test 17: Query Performance
**Steps:**
1. Store 1000+ records
2. Query last 100 records
3. Measure time

**Expected:**
- Query completes quickly (< 500ms)
- UI remains responsive

**Verification:**
```java
long startTime = System.currentTimeMillis();
viewModel.getLastNRecords(100).observe(this, records -> {
    long endTime = System.currentTimeMillis();
    Log.d("Test", "Query time: " + (endTime - startTime) + "ms");
    // Should be < 500ms
});
```

#### Test 18: Large Dataset Handling
**Steps:**
1. Store 10,000+ records (simulate long-term use)
2. Perform various queries
3. Check performance

**Expected:**
- App remains responsive
- Queries still fast
- No memory issues

---

### ✅ Edge Case Tests

#### Test 19: No Internet Connection
**Steps:**
1. Turn off WiFi and mobile data
2. Monitor and store data
3. Verify storage works

**Expected:**
- Data stored successfully
- No errors
- Offline operation confirmed

#### Test 20: Low Storage Space
**Steps:**
1. Fill device storage (leave < 100MB)
2. Try to store data
3. Check error handling

**Expected:**
- Graceful error handling
- No app crash
- User notified if needed

#### Test 21: Rapid Status Changes
**Steps:**
1. Simulate rapid status changes (if possible)
2. Verify all changes captured
3. Check for duplicates

**Expected:**
- All status changes stored
- No duplicate records
- Correct timestamps

#### Test 22: Concurrent Access
**Steps:**
1. Monitor on dashboard
2. Open history fragment simultaneously
3. Verify both work correctly

**Expected:**
- No database locks
- Both views update correctly
- No crashes

---

### ✅ Integration Tests

#### Test 23: BLE + Storage Integration
**Steps:**
1. Connect to real BLE device
2. Monitor for 5 minutes
3. Verify continuous storage

**Expected:**
- ~30 records stored (5 min / 10 sec)
- All readings valid
- No gaps in data

#### Test 24: Auth + Storage Integration
**Steps:**
1. Login
2. Store data
3. Logout
4. Login as different user
5. Verify data isolation

**Expected:**
- Each user sees only their data
- No cross-user data leakage

---

## 🐛 Common Issues & Solutions

### Issue 1: No Data Stored
**Symptoms:** Record count stays at 0

**Checks:**
```java
// Add logging to ViewModel
Log.d("Storage", "BLE Connected: " + isConnected());
Log.d("Storage", "HR Valid: " + reading.isHrValid());
Log.d("Storage", "Temp Valid: " + reading.isTempValid());
Log.d("Storage", "Time since last: " + (currentTime - lastStoredTimestamp));
```

**Solutions:**
- Ensure BLE is connected
- Verify readings are valid
- Check 10-second interval
- Verify user is authenticated

### Issue 2: Data Not Showing in UI
**Symptoms:** Data stored but not displayed

**Checks:**
```java
// Verify LiveData observation
viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
    Log.d("UI", "Records received: " + (records != null ? records.size() : "null"));
});
```

**Solutions:**
- Use correct lifecycle owner
- Ensure observer is set up
- Check if data exists in DB

### Issue 3: Duplicate Records
**Symptoms:** Same data stored multiple times

**Checks:**
```java
// Check storage logic
Log.d("Storage", "Should store: " + shouldStore);
Log.d("Storage", "Last stored time: " + lastStoredTimestamp);
```

**Solutions:**
- Verify storage interval logic
- Check status change detection
- Ensure single ViewModel instance

---

## 📊 Test Results Template

```
PHASE 6 TESTING RESULTS
Date: ___________
Tester: ___________

Basic Storage Tests:
[ ] Test 1: Data Storage During Monitoring
[ ] Test 2: Status Change Triggers Storage
[ ] Test 3: Invalid Readings Not Stored

Data Persistence Tests:
[ ] Test 4: Data Survives App Restart
[ ] Test 5: Data Survives Force Stop
[ ] Test 6: Data Survives Device Reboot

Multi-User Tests:
[ ] Test 7: User Data Isolation
[ ] Test 8: User Data Persists After Logout

Query Tests:
[ ] Test 9: Get Last N Records
[ ] Test 10: Time Range Query
[ ] Test 11: Critical Records Filter

Statistics Tests:
[ ] Test 12: Average Calculations
[ ] Test 13: Record Count Accuracy

Data Management Tests:
[ ] Test 14: Delete All Records
[ ] Test 15: Delete Old Records

Performance Tests:
[ ] Test 16: Storage Performance
[ ] Test 17: Query Performance
[ ] Test 18: Large Dataset Handling

Edge Case Tests:
[ ] Test 19: No Internet Connection
[ ] Test 20: Low Storage Space
[ ] Test 21: Rapid Status Changes
[ ] Test 22: Concurrent Access

Integration Tests:
[ ] Test 23: BLE + Storage Integration
[ ] Test 24: Auth + Storage Integration

OVERALL RESULT: [ ] PASS [ ] FAIL

Notes:
_________________________________
_________________________________
_________________________________
```

---

## ✅ Success Criteria

Phase 6 is considered successful when:
- ✅ All 24 tests pass
- ✅ Data persists across app restarts
- ✅ Multi-user isolation works
- ✅ No UI lag during storage
- ✅ Queries return correct data
- ✅ Offline operation confirmed
- ✅ No crashes or ANRs
- ✅ Ready for Phase 7 (charts)

**Phase 6 provides reliable, medical-grade local data storage for the health monitoring application!**
