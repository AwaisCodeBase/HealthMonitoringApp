# Firestore Practical Implementation Guide
## Step-by-Step Integration into Your App

---

## ✅ WHAT'S ALREADY DONE

1. ✅ Gradle dependencies added (Firebase BoM, Firestore)
2. ✅ `FirestoreManager.java` created (singleton pattern)
3. ✅ `HealthDataRepository.java` updated with Firestore methods
4. ✅ Hybrid approach: Room + Firestore (safe migration)

---

## 🚀 STEP 1: Update Your ViewModel

### File: `HealthMonitorViewModel.java`

**Add Firestore write in your storage strategy:**

```java
/**
 * Store health data using DUAL write (Room + Firestore)
 */
private void storeHealthDataIfNeeded(HealthReading reading, HealthStatus status) {
    long currentTime = System.currentTimeMillis();
    HealthStatus.Condition currentStatus = status.getOverallCondition();
    
    boolean shouldStore = false;
    
    // Store every 10 seconds
    if (currentTime - lastStoredTimestamp >= STORAGE_INTERVAL_MS) {
        shouldStore = true;
        Timber.d("Storing data: Time interval reached");
    } 
    // OR store on status change
    else if (lastStoredStatus != currentStatus) {
        shouldStore = true;
        Timber.d("Storing data: Status changed from %s to %s", lastStoredStatus, currentStatus);
    }
    
    if (shouldStore && reading.isHrValid() && reading.isTempValid()) {
        // DUAL WRITE: Both Room and Firestore
        repository.insertHealthRecordDual(reading, status);
        
        lastStoredTimestamp = currentTime;
        lastStoredStatus = currentStatus;
    }
}
```

---

## 🚀 STEP 2: Update History Fragment (Charts)

### File: `HistoryFragment.java` or wherever you display charts

**Replace Room query with Firestore real-time listener:**

```java
private void loadHealthData() {
    // OLD: Room query
    // viewModel.getAllRecords().observe(this, records -> {
    //     updateChart(records);
    // });
    
    // NEW: Firestore real-time query
    repository.getHealthRecordsFromFirestore(new HealthDataRepository.FirestoreRecordsCallback() {
        @Override
        public void onSuccess(List<HealthRecordEntity> records) {
            // Update UI on main thread
            requireActivity().runOnUiThread(() -> {
                updateChart(records);
                Log.d(TAG, "Chart updated with " + records.size() + " records");
            });
        }
        
        @Override
        public void onError(String error) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), 
                    "Error loading data: " + error, 
                    Toast.LENGTH_SHORT).show();
            });
        }
    });
}
```

**For time range queries (24h, 7d, 30d):**

```java
private void loadDataForTimeRange(long startTime, long endTime) {
    repository.getFirestoreRecordsByTimeRange(startTime, endTime, 
        new HealthDataRepository.FirestoreRecordsCallback() {
            @Override
            public void onSuccess(List<HealthRecordEntity> records) {
                requireActivity().runOnUiThread(() -> {
                    updateChart(records);
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading time range data: " + error);
            }
        });
}
```

---

## 🚀 STEP 3: Enable Firestore Logging (Development)

### File: `SensoryControlApplication.java` or `MainActivity.java`

```java
@Override
public void onCreate() {
    super.onCreate();
    
    // Enable Firestore debug logging (development only)
    if (BuildConfig.DEBUG) {
        FirestoreManager.enableLogging();
    }
    
    // Initialize Timber
    Timber.plant(new Timber.DebugTree());
}
```

---

## 🚀 STEP 4: Deploy Firestore Security Rules

### Go to Firebase Console

1. Open: https://console.firebase.google.com
2. Select your project
3. Navigate to: **Firestore Database** → **Rules**
4. Replace with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Health records: User can only access their own data
    match /users/{userId}/health_records/{recordId} {
      allow read, write: if request.auth != null 
                         && request.auth.uid == userId;
    }
    
    // Deny all other access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

5. Click **Publish**

---

## 🚀 STEP 5: Test Firestore Integration

### Test 1: Write Data

```java
// In your monitoring code (where you call repository.insertHealthRecord)
// Replace with:
repository.insertHealthRecordDual(reading, status);
```

**Expected result:**
- Data written to Room (local)
- Data written to Firestore (cloud)
- Check Logcat: `"Firestore record inserted: abc123xyz"`

### Test 2: View Data in Firebase Console

1. Open Firebase Console
2. Navigate to: **Firestore Database** → **Data**
3. You should see:

```
users
  └── {your-user-id}
       └── health_records
            └── {auto-generated-id}
                 ├── heartRate: 75
                 ├── spo2: 98
                 ├── temperature: 36.7
                 ├── status: "GOOD"
                 ├── timestamp: 1710000000
                 ├── hrValid: true
                 └── tempValid: true
```

### Test 3: Real-time Updates

1. Open your app on Device A
2. Navigate to History/Charts
3. Open Firebase Console on your computer
4. Manually add a document in Firestore
5. **Watch your app update automatically!** 🔥

---

## 🚀 STEP 6: Gradual Migration Strategy

### Phase 1: Dual Write (Current)
```java
// Write to both Room and Firestore
repository.insertHealthRecordDual(reading, status);

// Read from Room (existing code works)
viewModel.getAllRecords().observe(this, records -> {
    updateChart(records);
});
```

### Phase 2: Dual Write, Firestore Read
```java
// Write to both
repository.insertHealthRecordDual(reading, status);

// Read from Firestore (real-time)
repository.getHealthRecordsFromFirestore(callback);
```

### Phase 3: Firestore Only
```java
// Write to Firestore only
repository.insertHealthRecordToFirestore(reading, status);

// Read from Firestore
repository.getHealthRecordsFromFirestore(callback);

// Disable Room writes
repository.setUseFirestore(true);
```

---

## 🧪 TESTING CHECKLIST

### ✅ Authentication Test
```java
FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
if (user != null) {
    Log.d(TAG, "User authenticated: " + user.getUid());
} else {
    Log.e(TAG, "User NOT authenticated!");
}
```

### ✅ Write Test
```java
// Trigger a health reading
// Check Logcat for:
// "Firestore record inserted: abc123xyz"

// Check Firebase Console:
// Firestore Database → Data → users → {uid} → health_records
```

### ✅ Read Test
```java
repository.getHealthRecordsFromFirestore(new HealthDataRepository.FirestoreRecordsCallback() {
    @Override
    public void onSuccess(List<HealthRecordEntity> records) {
        Log.d(TAG, "Records received: " + records.size());
        // Should match Firebase Console count
    }
    
    @Override
    public void onError(String error) {
        Log.e(TAG, "Error: " + error);
    }
});
```

### ✅ Offline Test
```java
// 1. Enable airplane mode
// 2. Trigger health readings
// 3. Check Logcat: "Firestore record inserted" (queued locally)
// 4. Disable airplane mode
// 5. Check Firebase Console: Data appears!
```

### ✅ Real-time Test
```java
// 1. Open app
// 2. Navigate to charts
// 3. Open Firebase Console
// 4. Add document manually
// 5. Watch app update automatically (no refresh needed!)
```

---

## 🐛 TROUBLESHOOTING

### Issue 1: "User not authenticated"

**Cause:** FirebaseAuth.getCurrentUser() returns null

**Fix:**
```java
// Ensure user is logged in before accessing repository
if (FirebaseAuth.getInstance().getCurrentUser() == null) {
    // Redirect to login
    startActivity(new Intent(this, LoginActivity.class));
    finish();
}
```

### Issue 2: "PERMISSION_DENIED"

**Cause:** Firestore security rules not deployed or incorrect

**Fix:**
1. Check Firebase Console → Firestore → Rules
2. Verify rules match the template above
3. Click "Publish"
4. Wait 1 minute for propagation

### Issue 3: Data not showing in Firebase Console

**Cause:** Write failed silently

**Fix:**
```java
// Add failure listener
.addOnFailureListener(e -> {
    Log.e(TAG, "Firestore write failed", e);
    Toast.makeText(this, "Failed to save: " + e.getMessage(), 
        Toast.LENGTH_LONG).show();
});
```

### Issue 4: Charts not updating

**Cause:** Not using real-time listener

**Fix:**
```java
// Use addSnapshotListener (not get())
firestore.collection("...")
    .addSnapshotListener((snapshots, error) -> {
        // This fires on every change!
    });
```

### Issue 5: Too many Firestore writes (high cost)

**Cause:** Writing every second instead of every 10 seconds

**Fix:**
```java
// Verify storage strategy
private static final long STORAGE_INTERVAL_MS = 10000; // 10 seconds

// Check Logcat for:
// "Storing data: Time interval reached" (should be every 10s, not 1s)
```

---

## 📊 MONITORING FIRESTORE USAGE

### Check Usage in Firebase Console

1. Navigate to: **Firestore Database** → **Usage**
2. Monitor:
   - **Document reads:** Should be low (real-time listeners = 1 read)
   - **Document writes:** Should be ~8,640/day (every 10 seconds)
   - **Storage:** Should grow slowly (~1MB/month)

### Expected Costs (Single User)

```
Writes: 8,640/day (10-second interval)
Free tier: 20,000/day
Cost: $0/month ✅

Reads: ~100/day (with real-time listeners)
Free tier: 50,000/day
Cost: $0/month ✅

Storage: ~1MB/month
Free tier: 1GB
Cost: $0/month ✅

Total: $0/month for single user
```

---

## 🎯 VERIFICATION STEPS

### Step 1: Check Logcat
```bash
adb logcat | grep Firestore
# Should see:
# "Firestore record inserted: abc123xyz"
# "Firestore records received: 10"
```

### Step 2: Check Firebase Console
```
Firestore Database → Data
└── users
    └── {your-uid}
        └── health_records
            └── Multiple documents with health data
```

### Step 3: Check Real-time Updates
```
1. Open app
2. Navigate to charts
3. Add document in Firebase Console
4. App updates automatically (no refresh!)
```

### Step 4: Check Offline Mode
```
1. Enable airplane mode
2. Trigger readings
3. Disable airplane mode
4. Data syncs to cloud automatically
```

---

## 🚀 NEXT STEPS

Once Firestore is working:

1. ✅ **Test thoroughly** (all 5 tests above)
2. ✅ **Monitor costs** (Firebase Console → Usage)
3. ✅ **Migrate charts** to use Firestore queries
4. ✅ **Remove Room** (optional, after 30 days of testing)
5. ✅ **Implement WiFi** (replace BLE with WebSocket)

---

## 💡 PRO TIPS

### Tip 1: Use Dual Write During Migration
```java
// Safe approach: Write to both
repository.insertHealthRecordDual(reading, status);

// Fallback: If Firestore fails, Room still has data
```

### Tip 2: Enable Offline Persistence
```java
// Already enabled in FirestoreManager
FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true) // ✅ Offline support
    .build();
```

### Tip 3: Monitor Real-time Listener Count
```java
// Limit listeners to avoid memory leaks
private ListenerRegistration listenerRegistration;

@Override
public void onStart() {
    super.onStart();
    // Start listening
    listenerRegistration = firestore.collection("...")
        .addSnapshotListener(...);
}

@Override
public void onStop() {
    super.onStop();
    // Stop listening
    if (listenerRegistration != null) {
        listenerRegistration.remove();
    }
}
```

### Tip 4: Batch Deletes for Old Data
```java
// Delete records older than 30 days (cost optimization)
repository.deleteOldRecords(30);
```

---

## ✅ SUCCESS CRITERIA

**Firestore integration is successful when:**

1. ✅ Data writes to Firestore (check Firebase Console)
2. ✅ Data reads from Firestore (check Logcat)
3. ✅ Charts update in real-time (no refresh needed)
4. ✅ Offline mode works (queued writes sync later)
5. ✅ Security rules enforced (can't access other users' data)
6. ✅ Costs within budget ($0/month for single user)

**You're ready for WiFi migration when all 6 criteria are met!**

---

*Last Updated: Current Session*
*Status: Ready for Implementation*
*Estimated Time: 1-2 hours*
