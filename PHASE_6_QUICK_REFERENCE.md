# Phase 6: Room Database - Quick Reference

## 🎯 Quick Start

### Access Historical Data in Your Fragment/Activity:

```java
// Get ViewModel
HealthMonitorViewModel viewModel = new ViewModelProvider(this)
    .get(HealthMonitorViewModel.class);

// Get last 100 records
viewModel.getLastNRecords(100).observe(this, records -> {
    // Update UI with records
    for (HealthRecordEntity record : records) {
        Log.d("Health", "HR: " + record.getHeartRate() + 
              ", SpO2: " + record.getSpO2() + 
              ", Temp: " + record.getTemperature());
    }
});
```

---

## 📊 Common Queries

### 1. Get Today's Records
```java
Calendar calendar = Calendar.getInstance();
calendar.set(Calendar.HOUR_OF_DAY, 0);
calendar.set(Calendar.MINUTE, 0);
calendar.set(Calendar.SECOND, 0);
long startOfDay = calendar.getTimeInMillis();
long now = System.currentTimeMillis();

viewModel.getRecordsByTimeRange(startOfDay, now)
    .observe(this, records -> {
        // Display today's data
    });
```

### 2. Get Last Week's Records
```java
long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
long now = System.currentTimeMillis();

viewModel.getRecordsByTimeRange(weekAgo, now)
    .observe(this, records -> {
        // Display weekly data
    });
```

### 3. Get Critical Events
```java
viewModel.getCriticalRecords().observe(this, criticalRecords -> {
    if (criticalRecords != null && !criticalRecords.isEmpty()) {
        // Show alert: "You have " + criticalRecords.size() + " critical events"
    }
});
```

### 4. Get Record Count
```java
viewModel.getRecordCount(count -> {
    runOnUiThread(() -> {
        tvRecordCount.setText("Total records: " + count);
    });
});
```

### 5. Get Weekly Averages
```java
long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
long now = System.currentTimeMillis();

viewModel.getAverageVitals(weekAgo, now, (avgHR, avgSpO2, avgTemp) -> {
    runOnUiThread(() -> {
        tvAvgHR.setText(String.format("Avg HR: %.1f BPM", avgHR));
        tvAvgSpO2.setText(String.format("Avg SpO2: %.1f%%", avgSpO2));
        tvAvgTemp.setText(String.format("Avg Temp: %.1f°C", avgTemp));
    });
});
```

---

## 🗑️ Data Management

### Delete All Records
```java
viewModel.deleteAllRecords();
Toast.makeText(this, "All records deleted", Toast.LENGTH_SHORT).show();
```

### Delete Old Records (Keep Last 30 Days)
```java
viewModel.deleteOldRecords(30);
Toast.makeText(this, "Old records deleted", Toast.LENGTH_SHORT).show();
```

---

## 📈 Chart Data Preparation

### Convert Records to Chart Data
```java
viewModel.getLastNRecords(100).observe(this, records -> {
    if (records == null || records.isEmpty()) return;
    
    // Prepare data for MPAndroidChart
    List<Entry> heartRateEntries = new ArrayList<>();
    List<Entry> spO2Entries = new ArrayList<>();
    List<Entry> tempEntries = new ArrayList<>();
    
    for (int i = 0; i < records.size(); i++) {
        HealthRecordEntity record = records.get(i);
        float x = i; // or use timestamp
        
        heartRateEntries.add(new Entry(x, record.getHeartRate()));
        spO2Entries.add(new Entry(x, record.getSpO2()));
        tempEntries.add(new Entry(x, (float) record.getTemperature()));
    }
    
    // Create datasets and update chart
    updateChart(heartRateEntries, spO2Entries, tempEntries);
});
```

---

## 🔧 Storage Configuration

### Change Storage Interval
**File:** `HealthMonitorViewModel.java`

```java
// Current: Store every 10 seconds
private static final long STORAGE_INTERVAL_MS = 10000;

// Change to 30 seconds:
private static final long STORAGE_INTERVAL_MS = 30000;

// Change to 5 seconds (more frequent):
private static final long STORAGE_INTERVAL_MS = 5000;
```

---

## 📊 Database Schema

### Table: health_records
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER | Primary key (auto-increment) |
| timestamp | INTEGER | Unix timestamp (milliseconds) |
| heart_rate | INTEGER | Heart rate in BPM |
| spo2 | INTEGER | Blood oxygen percentage |
| temperature | REAL | Body temperature in Celsius |
| health_status | TEXT | "GOOD", "WARNING", or "CRITICAL" |
| user_id | TEXT | Firebase user ID |

---

## 🎨 Display Formatted Data

### Format Timestamp
```java
SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
String formattedDate = sdf.format(new Date(record.getTimestamp()));
```

### Format Health Status with Color
```java
String status = record.getHealthStatus();
int color;
switch (status) {
    case "GOOD":
        color = getResources().getColor(R.color.status_good);
        break;
    case "WARNING":
        color = getResources().getColor(R.color.status_warning);
        break;
    case "CRITICAL":
        color = getResources().getColor(R.color.status_critical);
        break;
    default:
        color = getResources().getColor(R.color.text_secondary);
}
tvStatus.setTextColor(color);
tvStatus.setText(status);
```

---

## 🔍 Debug Database

### View Database in Android Studio
1. Open **Device File Explorer**
2. Navigate to: `/data/data/com.example.sensorycontrol/databases/`
3. Find: `child_health_monitor.db`
4. Right-click → Save As
5. Open with SQLite browser

### Log All Records
```java
viewModel.getAllRecords().observe(this, records -> {
    if (records != null) {
        Log.d("Database", "Total records: " + records.size());
        for (HealthRecordEntity record : records) {
            Log.d("Database", record.toString());
        }
    }
});
```

---

## ⚡ Performance Tips

### 1. Use Appropriate Limits
```java
// Good: Get last 100 records for chart
viewModel.getLastNRecords(100);

// Bad: Get all records (could be thousands)
viewModel.getAllRecords(); // Only use for specific needs
```

### 2. Use Time Ranges
```java
// Good: Get specific time range
viewModel.getRecordsByTimeRange(startTime, endTime);

// Better than: Get all and filter in code
```

### 3. Observe Lifecycle
```java
// Good: Observe in lifecycle-aware component
viewModel.getLastNRecords(100).observe(getViewLifecycleOwner(), records -> {
    // Update UI
});

// Avoid: Observing forever without cleanup
```

---

## 🐛 Troubleshooting

### No Data Stored?
**Check:**
1. Is BLE connected?
2. Are readings valid? (hrValid && tempValid)
3. Has 10 seconds passed since last storage?
4. Is user authenticated?

**Debug:**
```java
// Add to ViewModel's storeHealthDataIfNeeded method
Log.d("Storage", "Time since last: " + (currentTime - lastStoredTimestamp));
Log.d("Storage", "Should store: " + shouldStore);
Log.d("Storage", "HR valid: " + reading.isHrValid());
Log.d("Storage", "Temp valid: " + reading.isTempValid());
```

### Data Not Showing?
**Check:**
1. Is LiveData being observed?
2. Is observer lifecycle-aware?
3. Is user ID correct?

**Debug:**
```java
viewModel.getRecordCount(count -> {
    Log.d("Database", "Total records in DB: " + count);
});
```

### Database Locked?
**Solution:** Room handles this automatically with background threads. If you see this error, ensure you're not calling DAO methods directly on main thread.

---

## 📱 Example: History Fragment

```java
public class HistoryFragment extends Fragment {
    
    private HealthMonitorViewModel viewModel;
    private RecyclerView recyclerView;
    private HealthRecordAdapter adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new HealthRecordAdapter();
        recyclerView.setAdapter(adapter);
        
        viewModel = new ViewModelProvider(requireActivity())
            .get(HealthMonitorViewModel.class);
        
        // Load last 100 records
        viewModel.getLastNRecords(100).observe(getViewLifecycleOwner(), records -> {
            if (records != null) {
                adapter.setRecords(records);
            }
        });
        
        return view;
    }
}
```

---

## 🎯 Key Points

1. **Data is stored automatically** - No manual calls needed
2. **Storage interval: 10 seconds** - Configurable
3. **Status changes trigger storage** - Captures critical events
4. **LiveData for reactive UI** - Automatic updates
5. **Background threads** - No UI blocking
6. **User-specific data** - Privacy by design
7. **Offline-first** - Works without internet

---

## 📚 Related Files

- **Entity:** `database/HealthRecordEntity.java`
- **DAO:** `database/HealthRecordDao.java`
- **Database:** `database/AppDatabase.java`
- **Repository:** `repository/HealthDataRepository.java`
- **ViewModel:** `viewmodels/HealthMonitorViewModel.java`

---

## 🚀 Ready for Phase 7

With Room database complete, you can now:
- ✅ Build historical charts
- ✅ Show trend analysis
- ✅ Display statistics
- ✅ Create health insights
- ✅ Implement alert history

**Phase 6 provides the data foundation for all future analytics features!**
