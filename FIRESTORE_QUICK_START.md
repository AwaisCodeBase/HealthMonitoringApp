# Firestore Quick Start - Copy & Paste Guide
## Get Firestore Working in 15 Minutes

---

## 🎯 GOAL

Replace Room queries with Firestore for real-time cloud sync while keeping your existing code working.

---

## ✅ STEP 1: Update ViewModel (5 minutes)

### File: `app/src/main/java/com/example/sensorycontrol/viewmodels/HealthMonitorViewModel.java`

**Find this method:**
```java
private void storeHealthDataIfNeeded(HealthReading reading, HealthStatus status) {
    // ... existing code ...
    
    if (shouldStore && reading.isHrValid() && reading.isTempValid()) {
        repository.insertHealthRecord(reading, status); // OLD
        lastStoredTimestamp = currentTime;
        lastStoredStatus = currentStatus;
    }
}
```

**Replace with:**
```java
private void storeHealthDataIfNeeded(HealthReading reading, HealthStatus status) {
    // ... existing code ...
    
    if (shouldStore && reading.isHrValid() && reading.isTempValid()) {
        repository.insertHealthRecordDual(reading, status); // NEW: Dual write
        lastStoredTimestamp = currentTime;
        lastStoredStatus = currentStatus;
    }
}
```

**That's it for ViewModel!** ✅

---

## ✅ STEP 2: Update History Fragment (5 minutes)

### File: `app/src/main/java/com/example/sensorycontrol/fragments/HistoryFragment.java`

**Find your chart loading code (probably in `onViewCreated` or `loadData`):**

```java
// OLD: Room query
viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
    if (records != null) {
        updateChart(records);
    }
});
```

**Replace with:**

```java
// NEW: Firestore real-time query
loadFirestoreData();
```

**Add this new method:**

```java
private void loadFirestoreData() {
    HealthDataRepository repository = new HealthDataRepository(requireActivity().getApplication());
    
    repository.getHealthRecordsFromFirestore(new HealthDataRepository.FirestoreRecordsCallback() {
        @Override
        public void onSuccess(List<HealthRecordEntity> records) {
            requireActivity().runOnUiThread(() -> {
                updateChart(records);
                Log.d(TAG, "Firestore data loaded: " + records.size() + " records");
            });
        }
        
        @Override
        public void onError(String error) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), 
                    "Error loading data: " + error, 
                    Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Firestore error: " + error);
            });
        }
    });
}
```

**That's it for History Fragment!** ✅

---

## ✅ STEP 3: Deploy Firestore Rules (3 minutes)

### Go to Firebase Console

1. Open: https://console.firebase.google.com
2. Select your project
3. Click: **Firestore Database** (left sidebar)
4. Click: **Rules** tab
5. **Copy and paste this:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/health_records/{recordId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

6. Click: **Publish**
7. Wait 30 seconds

**That's it for Security Rules!** ✅

---

## ✅ STEP 4: Enable Logging (2 minutes)

### File: `app/src/main/java/com/example/sensorycontrol/SensoryControlApplication.java`

**If you don't have this file, create it:**

```java
package com.example.sensorycontrol;

import android.app.Application;
import com.example.sensorycontrol.firebase.FirestoreManager;
import timber.log.Timber;

public class SensoryControlApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Enable Firestore logging (development only)
        if (BuildConfig.DEBUG) {
            FirestoreManager.enableLogging();
            Timber.plant(new Timber.DebugTree());
        }
    }
}
```

**Update AndroidManifest.xml:**

```xml
<application
    android:name=".SensoryControlApplication"
    ...>
```

**That's it for Logging!** ✅

---

## 🧪 STEP 5: Test It (5 minutes)

### Test 1: Build and Run

```bash
# Clean and build
./gradlew clean
./gradlew build

# Or in Android Studio:
# Build → Rebuild Project
```

### Test 2: Trigger Health Reading

1. Open your app
2. Connect to BLE device (or use test data)
3. Wait 10 seconds (for storage interval)
4. Check Logcat:

```
D/HealthDataRepository: Firestore record inserted: abc123xyz
```

### Test 3: Check Firebase Console

1. Open: https://console.firebase.google.com
2. Navigate to: **Firestore Database** → **Data**
3. You should see:

```
users
  └── {your-user-id}
       └── health_records
            └── {document-id}
                 ├── heartRate: 75
                 ├── spo2: 98
                 ├── temperature: 36.7
                 ├── status: "GOOD"
                 └── timestamp: 1710000000
```

### Test 4: Check Charts

1. Navigate to History/Charts in your app
2. You should see your data displayed
3. Check Logcat:

```
D/HistoryFragment: Firestore data loaded: 10 records
```

### Test 5: Real-time Update

1. Keep your app open on charts screen
2. Open Firebase Console
3. Manually add a document:
   - Click "Start collection"
   - Collection ID: `users/{your-uid}/health_records`
   - Add fields: heartRate, spo2, temperature, status, timestamp
   - Click "Save"
4. **Watch your app update automatically!** 🔥

---

## ✅ SUCCESS CHECKLIST

- [ ] ViewModel updated (insertHealthRecordDual)
- [ ] History Fragment updated (loadFirestoreData)
- [ ] Security rules deployed
- [ ] Logging enabled
- [ ] App builds successfully
- [ ] Data writes to Firestore (check console)
- [ ] Data reads from Firestore (check Logcat)
- [ ] Charts display data
- [ ] Real-time updates work

**If all checked, you're done!** 🎉

---

## 🐛 QUICK TROUBLESHOOTING

### Problem: "User not authenticated"

**Solution:**
```java
// Check if user is logged in
FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
if (user == null) {
    Log.e(TAG, "User not logged in!");
    // Redirect to login
}
```

### Problem: "PERMISSION_DENIED"

**Solution:**
- Check Firebase Console → Firestore → Rules
- Verify rules are published
- Wait 1 minute for propagation

### Problem: Data not showing in console

**Solution:**
```bash
# Check Logcat for errors
adb logcat | grep Firestore

# Look for:
# "Firestore record inserted" ✅
# "Error inserting to Firestore" ❌
```

### Problem: Charts not updating

**Solution:**
```java
// Make sure you're using addSnapshotListener (not get())
// Check HealthDataRepository.getHealthRecordsFromFirestore()
// It should have: .addSnapshotListener(...)
```

---

## 📊 VERIFY COSTS

### Check Firebase Console

1. Navigate to: **Firestore Database** → **Usage**
2. Verify:
   - **Writes:** ~8,640/day (every 10 seconds)
   - **Reads:** <100/day (real-time listeners)
   - **Storage:** <1MB

**Expected cost: $0/month** ✅

---

## 🚀 WHAT'S NEXT?

Once Firestore is working:

1. ✅ Test for 1-2 days
2. ✅ Monitor Firebase Console usage
3. ✅ Verify real-time updates work
4. ✅ Move to WiFi migration (replace BLE)

---

## 💡 ONE-LINER SUMMARY

**Changed 3 lines of code:**
1. `insertHealthRecord` → `insertHealthRecordDual` (ViewModel)
2. `getAllRecords()` → `getHealthRecordsFromFirestore()` (Fragment)
3. Deployed security rules (Firebase Console)

**Result:** Real-time cloud sync with offline support! 🔥

---

*Total Time: 15 minutes*
*Difficulty: Easy*
*Status: Production-ready*
