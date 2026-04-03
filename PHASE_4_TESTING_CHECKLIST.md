# Phase 4: Authentication Testing Checklist

## Complete Testing Guide for Firebase Authentication

Use this checklist to verify all authentication features are working correctly.

---

## 🔧 Pre-Testing Setup

### Firebase Configuration
- [ ] Firebase project created
- [ ] Android app registered in Firebase Console
- [ ] `google-services.json` present in `app/` directory
- [ ] Email/Password authentication enabled in Firebase Console
- [ ] (Optional) Google Sign-In enabled in Firebase Console
- [ ] (Optional) SHA-1 fingerprint added for Google Sign-In
- [ ] (Optional) Web Client ID updated in `strings.xml`

### Build Configuration
- [ ] App builds successfully without errors
- [ ] No Gradle sync issues
- [ ] Firebase dependencies resolved
- [ ] Google Services plugin applied

### Device/Emulator Setup
- [ ] Test device/emulator has internet connection
- [ ] (For Google Sign-In) Google account added to device
- [ ] (For Google Sign-In) Google Play Services installed

---

## 📱 Test Cases

### TC-01: First Launch (New User)

**Objective**: Verify app routing for unauthenticated users

**Steps**:
1. Install app (fresh install, no previous data)
2. Launch app
3. Observe splash screen (2 seconds)

**Expected Result**:
- [ ] Splash screen appears
- [ ] After 2 seconds, navigates to LoginActivity
- [ ] Login screen shows email, password fields
- [ ] "Sign Up" link visible
- [ ] Google Sign-In button visible (if enabled)

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-02: Email/Password Signup - Valid Data

**Objective**: Create new account with valid credentials

**Steps**:
1. From LoginActivity, click "Sign Up"
2. Enter name: "Test User"
3. Enter email: "testuser@example.com"
4. Enter password: "password123"
5. Enter confirm password: "password123"
6. Click "Sign Up"

**Expected Result**:
- [ ] Loading dialog appears: "Creating Your Account"
- [ ] Success dialog appears: "Account Created!"
- [ ] Navigates to MainActivity
- [ ] Bottom navigation visible (Dashboard, Scan, Monitoring, Settings)
- [ ] User appears in Firebase Console → Authentication → Users
- [ ] User profile created in Firestore → users collection

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-03: Email/Password Signup - Validation Errors

**Objective**: Verify input validation

**Test 3a: Empty Name**
- [ ] Leave name empty
- [ ] Click "Sign Up"
- [ ] Error: "Name is required"

**Test 3b: Short Name**
- [ ] Enter name: "A"
- [ ] Click "Sign Up"
- [ ] Error: "Please enter your full name"

**Test 3c: Invalid Email**
- [ ] Enter email: "notanemail"
- [ ] Click "Sign Up"
- [ ] Error: "Please enter a valid email address"

**Test 3d: Short Password**
- [ ] Enter password: "12345"
- [ ] Click "Sign Up"
- [ ] Error: "Password must be at least 6 characters"

**Test 3e: Password Mismatch**
- [ ] Enter password: "password123"
- [ ] Enter confirm: "password456"
- [ ] Click "Sign Up"
- [ ] Error: "Passwords do not match"

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-04: Email/Password Signup - Duplicate Email

**Objective**: Verify duplicate email handling

**Steps**:
1. Try to sign up with email already registered
2. Enter name: "Another User"
3. Enter email: "testuser@example.com" (existing)
4. Enter password: "password123"
5. Enter confirm: "password123"
6. Click "Sign Up"

**Expected Result**:
- [ ] Loading dialog appears
- [ ] Error dialog: "This email is already registered"
- [ ] Email field shows error
- [ ] User stays on SignupActivity

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-05: Email/Password Login - Valid Credentials

**Objective**: Login with existing account

**Steps**:
1. From LoginActivity
2. Enter email: "testuser@example.com"
3. Enter password: "password123"
4. Click "Login"

**Expected Result**:
- [ ] Loading dialog appears: "Signing In"
- [ ] Success dialog: "Welcome Back!"
- [ ] Navigates to MainActivity
- [ ] User can access all features

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-06: Email/Password Login - Invalid Credentials

**Objective**: Verify error handling for wrong credentials

**Test 6a: Wrong Password**
- [ ] Enter correct email
- [ ] Enter wrong password
- [ ] Click "Login"
- [ ] Error: "Incorrect password"
- [ ] Password field shows error

**Test 6b: Non-existent Email**
- [ ] Enter email not registered
- [ ] Enter any password
- [ ] Click "Login"
- [ ] Error: "No account found with this email"
- [ ] Email field shows error

**Test 6c: Empty Fields**
- [ ] Leave email empty
- [ ] Click "Login"
- [ ] Error: "Email is required"

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-07: Google Sign-In - First Time User

**Objective**: Create account via Google Sign-In

**Prerequisites**: Google Sign-In enabled, SHA-1 added, Web Client ID configured

**Steps**:
1. From LoginActivity
2. Click "Sign in with Google"
3. Select Google account from picker
4. Authorize app (if prompted)

**Expected Result**:
- [ ] Google account picker appears
- [ ] Loading dialog: "Signing in with Google..."
- [ ] Success dialog: "Welcome!"
- [ ] Navigates to MainActivity
- [ ] User appears in Firebase Console (Provider: google.com)
- [ ] User profile created in Firestore with Google name/email

**Status**: ⬜ Pass | ⬜ Fail | ⬜ N/A (Google Sign-In not enabled)

---

### TC-08: Google Sign-In - Returning User

**Objective**: Login with existing Google account

**Steps**:
1. Logout from app
2. From LoginActivity, click "Sign in with Google"
3. Select same Google account

**Expected Result**:
- [ ] Google account picker appears
- [ ] Loading dialog appears
- [ ] Success dialog: "Welcome Back!"
- [ ] Navigates to MainActivity
- [ ] No duplicate user created in Firebase

**Status**: ⬜ Pass | ⬜ Fail | ⬜ N/A

---

### TC-09: Google Sign-In - Cancellation

**Objective**: Handle user cancelling Google Sign-In

**Steps**:
1. Click "Sign in with Google"
2. Press back button or cancel in account picker

**Expected Result**:
- [ ] Returns to LoginActivity
- [ ] Error message: "Sign-in was cancelled"
- [ ] No crash
- [ ] Can try again

**Status**: ⬜ Pass | ⬜ Fail | ⬜ N/A

---

### TC-10: Session Persistence - Stay Logged In

**Objective**: Verify user stays logged in after app restart

**Steps**:
1. Login successfully (any method)
2. Verify in MainActivity
3. Close app completely (swipe away from recent apps)
4. Reopen app

**Expected Result**:
- [ ] Splash screen appears
- [ ] Automatically navigates to MainActivity (skips login)
- [ ] User still authenticated
- [ ] Can access all features

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-11: Session Persistence - After Device Restart

**Objective**: Verify session persists after device reboot

**Steps**:
1. Login successfully
2. Restart device
3. Open app

**Expected Result**:
- [ ] User still logged in
- [ ] Navigates to MainActivity
- [ ] No need to login again

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-12: Logout Functionality

**Objective**: Verify logout works correctly

**Steps**:
1. Login and navigate to MainActivity
2. Go to Settings tab
3. Verify user name and email displayed
4. Click "Logout" button
5. Confirm logout in dialog

**Expected Result**:
- [ ] Confirmation dialog appears: "Are you sure?"
- [ ] After confirming, navigates to LoginActivity
- [ ] User signed out from Firebase
- [ ] Cannot access MainActivity without login
- [ ] Reopening app shows LoginActivity

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-13: Access Control - Unauthenticated User

**Objective**: Verify unauthenticated users cannot access protected features

**Steps**:
1. Ensure user is logged out
2. Try to access MainActivity directly (if possible)

**Expected Result**:
- [ ] Cannot access MainActivity
- [ ] Redirected to LoginActivity
- [ ] Must authenticate to proceed

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-14: Access Control - Authenticated User

**Objective**: Verify authenticated users can access all features

**Steps**:
1. Login successfully
2. Navigate to each tab in MainActivity

**Expected Result**:
- [ ] Dashboard tab accessible
- [ ] Scan tab accessible
- [ ] Monitoring tab accessible
- [ ] Settings tab accessible
- [ ] No authentication prompts

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-15: Network Error Handling

**Objective**: Verify graceful handling of network errors

**Steps**:
1. Turn off internet (WiFi and mobile data)
2. Try to login with valid credentials
3. Turn internet back on
4. Try again

**Expected Result**:
- [ ] Error message: "Network error. Check your internet connection"
- [ ] No crash
- [ ] After internet restored, login works

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-16: User Profile Display

**Objective**: Verify user profile information displayed correctly

**Steps**:
1. Login successfully
2. Navigate to Settings tab

**Expected Result**:
- [ ] User name displayed correctly
- [ ] User email displayed correctly
- [ ] Logout button visible

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-17: Multiple Account Switching

**Objective**: Verify switching between accounts works

**Steps**:
1. Login with Account A
2. Logout
3. Login with Account B
4. Verify correct user info displayed

**Expected Result**:
- [ ] Account A info shown when logged in as A
- [ ] Account B info shown when logged in as B
- [ ] No data mixing between accounts

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-18: UI Responsiveness

**Objective**: Verify UI remains responsive during operations

**Test 18a: Login Loading State**
- [ ] During login, all inputs disabled
- [ ] Login button disabled
- [ ] Progress bar visible
- [ ] Cannot interact with form

**Test 18b: Signup Loading State**
- [ ] During signup, all inputs disabled
- [ ] Signup button disabled
- [ ] Progress bar visible
- [ ] Cannot interact with form

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-19: Dialog Handling

**Objective**: Verify dialogs work correctly

**Test 19a: Loading Dialog**
- [ ] Shows during authentication
- [ ] Cannot be dismissed by tapping outside
- [ ] Dismisses automatically on success/failure

**Test 19b: Success Dialog**
- [ ] Shows on successful auth
- [ ] "Continue" button works
- [ ] Navigates to MainActivity after dismissal

**Test 19c: Error Dialog**
- [ ] Shows on auth failure
- [ ] "OK" button works
- [ ] Stays on current screen after dismissal

**Status**: ⬜ Pass | ⬜ Fail

---

### TC-20: Firebase Console Verification

**Objective**: Verify data correctly stored in Firebase

**Steps**:
1. Create new account via app
2. Check Firebase Console

**Expected Result**:

**Authentication → Users**:
- [ ] User appears in list
- [ ] Email correct
- [ ] Provider correct (password or google.com)
- [ ] UID generated

**Firestore → users collection**:
- [ ] Document created with UID as document ID
- [ ] Fields present: userId, name, email, childAge, createdAt
- [ ] Values correct

**Status**: ⬜ Pass | ⬜ Fail

---

## 🔍 Edge Cases

### EC-01: Rapid Button Clicks
- [ ] Click login button multiple times rapidly
- [ ] Should not create multiple requests
- [ ] Button disabled during processing

### EC-02: App Backgrounding During Auth
- [ ] Start login process
- [ ] Press home button (background app)
- [ ] Return to app
- [ ] Should handle gracefully (no crash)

### EC-03: Very Long Email/Password
- [ ] Enter 100+ character email
- [ ] Enter 100+ character password
- [ ] Should handle without crash

### EC-04: Special Characters in Name
- [ ] Enter name with emojis: "Test 😀 User"
- [ ] Should accept and store correctly

### EC-05: Simultaneous Logins
- [ ] Login on Device A
- [ ] Login same account on Device B
- [ ] Both should work (Firebase allows multiple sessions)

---

## 📊 Test Summary

### Overall Results

**Total Test Cases**: 20  
**Passed**: ___  
**Failed**: ___  
**N/A**: ___  

**Pass Rate**: ____%

### Critical Issues Found

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

### Minor Issues Found

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

### Recommendations

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

---

## 🎯 Acceptance Criteria

Phase 4 is considered complete when:

- [ ] All critical test cases pass (TC-01 to TC-14)
- [ ] No crashes during normal usage
- [ ] Error messages are user-friendly
- [ ] Session persistence works
- [ ] Logout works correctly
- [ ] Firebase Console shows correct data
- [ ] UI is responsive and professional
- [ ] Network errors handled gracefully

---

## 📝 Testing Notes

**Tester Name**: _______________  
**Date**: _______________  
**Device/Emulator**: _______________  
**Android Version**: _______________  
**App Version**: _______________  

**Additional Comments**:
_______________________________________________
_______________________________________________
_______________________________________________

---

## 🚀 Next Steps After Testing

If all tests pass:
1. ✅ Mark Phase 4 as complete
2. ✅ Document results for dissertation
3. ✅ Proceed to Phase 5 (BLE Data Storage & Sync)

If tests fail:
1. ❌ Document failures
2. ❌ Fix issues
3. ❌ Re-test
4. ❌ Repeat until all pass

---

**Testing Status**: ⬜ Not Started | ⬜ In Progress | ⬜ Complete  
**Phase 4 Status**: ⬜ Pass | ⬜ Fail
