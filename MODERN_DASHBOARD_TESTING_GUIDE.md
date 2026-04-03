# Modern Dashboard Testing Guide

## Quick Start Testing

### 1. Build and Run
```bash
# From Android Studio
./gradlew clean assembleDebug
# Or use "Run" button in Android Studio
```

### 2. First Launch - What You Should See

#### App Bar (Top)
- 🍼 Baby face icon on the left
- "Child Health Monitor" text
- Small dot + "Disconnected" text below app name
- Circular avatar on the right

#### Status Card (Center Top)
- "Overall Health Status" label
- Large circular dot (yellow/gray initially)
- "Waiting for data..." text

#### Three Vital Cards
Each card should have:
- Icon with colored circle background (left side)
- Vital name label
- Large "--" value (48sp, very readable)
- Unit label (BPM, %, °C)
- Normal range text below
- No trend arrow initially

#### Bottom Buttons
- Blue "Connect Device" button (left)
- Outlined "History" button (right)

### 3. Test Connection Flow

#### Step 1: Click "Connect Device"
**Expected:**
- Button text changes to "Scanning..."
- Button becomes disabled
- Connection badge changes to "Scanning..."
- Badge dot turns yellow

#### Step 2: Device Found & Connected
**Expected:**
- Button text changes to "Disconnect"
- Button icon changes to X
- Connection badge changes to "Connected"
- Badge dot turns green
- ❤️ Heart icon starts pulsing (smooth scale animation)

#### Step 3: Receiving Data
**Expected:**
- Values change from "--" to actual numbers
- Each value change has a subtle scale animation
- Trend arrows appear (↑ or ↓)
- Card borders appear (colored, 4dp)
- Status dot changes color based on vitals
- Status text updates

### 4. Test Health Status Scenarios

#### Scenario A: All Normal (Green)
**Simulate:** HR=80, SpO2=98, Temp=36.8
**Expected:**
- Status dot = GREEN
- Status text = "All Vitals Normal"
- All card borders = GREEN
- No blinking animation
- Heart pulse continues normally

#### Scenario B: Warning (Yellow)
**Simulate:** HR=125, SpO2=93, Temp=37.8
**Expected:**
- Status dot = YELLOW
- Status text = "Attention Needed"
- Affected card borders = YELLOW
- No blinking animation

#### Scenario C: Critical (Red)
**Simulate:** HR=160, SpO2=88, Temp=38.8
**Expected:**
- Status dot = RED
- Status text = "CRITICAL"
- Affected card borders = RED
- ⚠️ Status dot BLINKS (alpha animation)
- Heart pulse continues

### 5. Test Animations

#### Pulse Animation (Heart Icon)
- Should run continuously when connected
- Smooth scale: 1.0 → 1.15 → 1.0
- Duration: ~1 second per cycle
- No stuttering or lag

#### Blink Animation (Critical Status)
- Only appears when status = CRITICAL
- Status dot fades in/out
- Alpha: 1.0 → 0.3 → 1.0
- Duration: ~0.5 seconds per cycle

#### Value Change Animation
- Happens when any vital value updates
- Quick scale: 1.0 → 1.1 → 1.0
- Duration: ~0.3 seconds
- Subtle and smooth

### 6. Test Trend Arrows

#### Increasing Values
**Test:** HR goes from 80 → 85 → 90
**Expected:**
- ↑ arrow appears
- Arrow color = RED (concerning for HR)
- Arrow size = 32sp

#### Decreasing Values
**Test:** HR goes from 90 → 85 → 80
**Expected:**
- ↓ arrow appears
- Arrow color = GREEN (good for HR)
- Arrow size = 32sp

#### Stable Values
**Test:** HR stays at 80
**Expected:**
- No arrow visible

### 7. Test Navigation

#### History Button
**Action:** Click "History" button
**Expected:**
- Navigates to History screen
- Shows charts and statistics
- Can navigate back to dashboard

#### Connect/Disconnect
**Action:** Click "Disconnect" when connected
**Expected:**
- Disconnects from device
- Button text changes to "Connect Device"
- Connection badge changes to "Disconnected"
- Heart pulse animation stops
- Values remain visible (last known)

### 8. Visual Quality Checks

#### Colors
- [ ] Gradient background is smooth (blue-green)
- [ ] Status dots are vibrant (green/yellow/red)
- [ ] Card borders are visible and colored
- [ ] Text is high contrast and readable
- [ ] Icons are properly colored

#### Typography
- [ ] App name is bold and clear
- [ ] Vital values are LARGE (48sp)
- [ ] Labels are medium weight
- [ ] Units are smaller than values
- [ ] All text is readable

#### Spacing
- [ ] Cards have proper margins (16dp between)
- [ ] Padding inside cards is generous (24dp)
- [ ] Icons are properly spaced from text
- [ ] No cramped or overlapping elements

#### Corners & Shadows
- [ ] Cards have rounded corners (24dp)
- [ ] Subtle shadows on cards (2-4dp)
- [ ] Status dot is perfectly circular
- [ ] Avatar is circular

### 9. Edge Cases

#### No Data
**Test:** Launch app without connecting
**Expected:**
- All values show "--"
- Status text = "Waiting for data..."
- No trend arrows
- No card borders
- No crashes

#### Connection Loss
**Test:** Disconnect device during monitoring
**Expected:**
- Connection badge updates
- Button text changes
- Animations stop
- Last values remain visible
- No crashes

#### Rapid Value Changes
**Test:** Simulate rapid BLE data updates
**Expected:**
- UI updates smoothly
- No lag or stuttering
- Animations don't overlap badly
- No crashes

#### Fragment Lifecycle
**Test:** Rotate device, navigate away and back
**Expected:**
- State is preserved
- Animations restart properly
- No memory leaks
- No crashes

### 10. Performance Checks

#### Smooth Animations
- [ ] 60 FPS during animations
- [ ] No dropped frames
- [ ] No stuttering

#### Memory Usage
- [ ] No memory leaks
- [ ] Animations clean up properly
- [ ] ViewModel survives rotation

#### Battery Impact
- [ ] Animations use hardware acceleration
- [ ] No excessive CPU usage
- [ ] Efficient BLE scanning

## Common Issues & Solutions

### Issue: Heart pulse animation not starting
**Solution:** Check if connection state is CONNECTED

### Issue: Status dot not blinking
**Solution:** Verify health status is CRITICAL (not just WARNING)

### Issue: Trend arrows not appearing
**Solution:** Need at least 2 data points for comparison

### Issue: Card borders not visible
**Solution:** Check if strokeWidth is set to 4dp

### Issue: Values not updating
**Solution:** Verify ViewModel is observing BLE data correctly

### Issue: Animations continue after disconnect
**Solution:** Check onDestroyView() cleanup

## Success Criteria

✅ All visual elements render correctly
✅ Connection flow works smoothly
✅ Vital values update in real-time
✅ Animations are smooth and appropriate
✅ Status evaluation is accurate
✅ Navigation works correctly
✅ No crashes or memory leaks
✅ Professional medical aesthetic
✅ Child-friendly but trustworthy
✅ High contrast and readable

## Screenshots to Capture

1. **Initial state** - Disconnected, no data
2. **Scanning state** - Button disabled, yellow badge
3. **Connected state** - Green badge, pulse animation
4. **Normal vitals** - All green, good status
5. **Warning vitals** - Yellow cards, attention needed
6. **Critical vitals** - Red cards, blinking status
7. **Trend arrows** - Both up and down arrows
8. **History navigation** - Showing history screen

## Next Steps After Testing

1. Fix any visual issues found
2. Adjust animation timings if needed
3. Fine-tune colors for better contrast
4. Optimize performance if lag detected
5. Add any missing accessibility features
6. Document any bugs or improvements needed

---

**Happy Testing! 🎉**
