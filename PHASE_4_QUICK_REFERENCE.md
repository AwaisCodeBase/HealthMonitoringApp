# Phase 4: Quick Reference Card

## 🚀 5-Minute Setup

### Step 1: Enable Authentication (2 minutes)
1. Go to https://console.firebase.google.com/
2. Select your project
3. Click **Authentication** → **Sign-in method**
4. Enable **Email/Password**
5. Click **Save**

### Step 2: Test the App (3 minutes)
1. Build and run the app
2. Click "Sign Up"
3. Enter test credentials
4. Verify you can login
5. Done! ✅

---

## 📱 User Flow

```
Launch → Splash (2s) → Login → Signup → Main App
                         ↓                    ↓
                    Email/Password        Dashboard
                    Google Sign-In        BLE Features
                                          Settings
                                          Logout
```

---

## 🔑 Key Classes

### AuthManager
```java
// Singleton instance
AuthManager auth = AuthManager.getInstance();

// Check if logged in
boolean isLoggedIn = auth.isUserLoggedIn();

// Get current user
FirebaseUser user = auth.getCurrentUser();

// Sign up
auth.signUp(email, password, name, callback);

// Sign in
auth.signIn(email, password, callback);

// Sign out
auth.signOut(context);
```

### Activities
- **SplashActivity** - Checks auth, routes to Login or Main
- **LoginActivity** - Email/Password + Google Sign-In
- **SignupActivity** - Account creation
- **MainActivity** - Protected app (requires auth)
- **SettingsFragment** - Profile + Logout

---

## ✅ Testing Checklist

### Must Test
- [ ] Signup with email/password
- [ ] Login with email/password
- [ ] Session persists (close/reopen app)
- [ ] Logout works
- [ ] Error messages show correctly

### Optional (If Google Sign-In Enabled)
- [ ] Google Sign-In works
- [ ] Google account picker appears
- [ ] User created in Firebase

---

## 🔧 Common Issues & Fixes

### Issue: "No user found"
**Fix**: User hasn't signed up yet. Use signup first.

### Issue: "Wrong password"
**Fix**: Check password is correct (minimum 6 characters).

### Issue: "Network error"
**Fix**: Check internet connection.

### Issue: Google Sign-In not working
**Fix**: 
1. Add SHA-1 to Firebase Console
2. Enable Google Sign-In in Firebase
3. Update Web Client ID in strings.xml
4. Rebuild app

---

## 📊 Firebase Console Quick Links

### Check Users
**Authentication** → **Users**
- See all registered users
- Check email and provider

### Check Profiles
**Firestore Database** → **users** collection
- See user profiles
- Verify data structure

### Enable Methods
**Authentication** → **Sign-in method**
- Enable Email/Password
- Enable Google (optional)

---

## 🎯 Success Indicators

✅ App launches to splash screen  
✅ Routes to login if not authenticated  
✅ Can create account  
✅ Can login  
✅ Stays logged in after restart  
✅ Can logout  
✅ Users appear in Firebase Console  

---

## 📝 Code Snippets

### Check Auth in Any Activity
```java
AuthManager auth = AuthManager.getInstance();
if (!auth.isUserLoggedIn()) {
    // Redirect to login
    startActivity(new Intent(this, LoginActivity.class));
    finish();
}
```

### Get User Info
```java
FirebaseUser user = auth.getCurrentUser();
String email = user.getEmail();
String uid = user.getUid();
```

### Update Profile
```java
Map<String, Object> updates = new HashMap<>();
updates.put("childAge", 5);
auth.updateUserProfile(updates, callback);
```

---

## 🔒 Security Notes

- Passwords hashed by Firebase (bcrypt)
- Tokens stored securely
- HTTPS automatic
- Session auto-refreshed
- Firestore rules protect data

---

## 📚 Documentation Files

1. **PHASE_4_AUTHENTICATION_COMPLETE.md** - Full guide
2. **GOOGLE_SIGNIN_SETUP_GUIDE.md** - Google Sign-In setup
3. **PHASE_4_TESTING_CHECKLIST.md** - Testing guide
4. **PHASE_4_SUMMARY.md** - Overview
5. **PHASE_4_QUICK_REFERENCE.md** - This file

---

## 🎓 For Dissertation

### Key Points to Mention
- Firebase Authentication integration
- Email/Password + OAuth 2.0 (Google)
- Session persistence
- User profile management
- Security considerations
- Error handling strategy

### Demo Flow
1. Show signup process
2. Show login process
3. Show session persistence
4. Show logout
5. Show Firebase Console data

---

## 🚀 Next Phase

**Phase 5: BLE Data Storage & Sync**
- Store sensor readings locally
- Sync to Firestore
- Real-time visualization
- Historical data

---

## ⚡ Quick Commands

### Get SHA-1 (for Google Sign-In)
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Build App
```bash
./gradlew clean build
```

### Run Tests
```bash
./gradlew test
```

---

## 📞 Need Help?

- **Firebase Docs**: https://firebase.google.com/docs/auth
- **Stack Overflow**: Search "Firebase Authentication Android"
- **Firebase Console**: https://console.firebase.google.com/

---

**Phase 4 Status**: ✅ COMPLETE  
**Setup Time**: 5 minutes  
**Testing Time**: 10 minutes  
**Total Time**: 15 minutes

**You're ready to go!** 🎉
