# Google Sign-In Setup Guide

## Quick Setup for Firebase Google Authentication

This guide will help you enable Google Sign-In for your Android app in under 10 minutes.

---

## Prerequisites

- Firebase project created
- Android app registered in Firebase
- `google-services.json` in `app/` directory ✅
- Android Studio installed

---

## Step 1: Get Your SHA-1 Fingerprint

### For Debug Build (Development)

Open Terminal in Android Studio and run:

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Output will look like**:
```
Certificate fingerprints:
     SHA1: A1:B2:C3:D4:E5:F6:G7:H8:I9:J0:K1:L2:M3:N4:O5:P6:Q7:R8:S9:T0
     SHA256: ...
```

**Copy the SHA-1 value** (the part after `SHA1:`)

### For Release Build (Production)

If you have a release keystore:

```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias
```

Enter your keystore password when prompted.

---

## Step 2: Add SHA-1 to Firebase

1. **Go to Firebase Console**: https://console.firebase.google.com/
2. **Select your project**
3. **Click the gear icon** (⚙️) → **Project settings**
4. **Scroll down** to "Your apps" section
5. **Find your Android app** (`com.example.sensorycontrol`)
6. **Click "Add fingerprint"**
7. **Paste your SHA-1** fingerprint
8. **Click "Save"**

---

## Step 3: Enable Google Sign-In in Firebase

1. **In Firebase Console**, go to **Authentication**
2. **Click "Sign-in method"** tab
3. **Find "Google"** in the list
4. **Click "Google"** to expand
5. **Toggle "Enable"** switch
6. **Enter project support email** (your email)
7. **Click "Save"**

---

## Step 4: Get Web Client ID

### Option A: From Firebase Console (Recommended)

1. **Firebase Console** → **Project settings**
2. **Scroll down** to "Your apps"
3. **Find "Web client ID"** under "SDK setup and configuration"
4. **Copy the entire ID** (format: `123456789-abc123xyz.apps.googleusercontent.com`)

### Option B: From google-services.json

Open `app/google-services.json` and find:

```json
{
  "oauth_client": [
    {
      "client_id": "123456789-abc123xyz.apps.googleusercontent.com",
      "client_type": 3
    }
  ]
}
```

Copy the `client_id` value where `client_type` is `3`.

---

## Step 5: Update strings.xml

**File**: `app/src/main/res/values/strings.xml`

Find this line:
```xml
<string name="default_web_client_id" translatable="false">680374870007-YOUR_CLIENT_ID.apps.googleusercontent.com</string>
```

Replace with your actual Web Client ID:
```xml
<string name="default_web_client_id" translatable="false">123456789-abc123xyz.apps.googleusercontent.com</string>
```

**Important**: Keep the entire ID including `.apps.googleusercontent.com`

---

## Step 6: Download Updated google-services.json

1. **Firebase Console** → **Project settings**
2. **Scroll down** to your Android app
3. **Click "google-services.json"** to download
4. **Replace** the existing file in `app/google-services.json`

---

## Step 7: Rebuild the App

In Android Studio:

1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**
3. **Run the app**

---

## Testing Google Sign-In

### Test on Emulator

1. **Open Android Emulator**
2. **Add a Google account**:
   - Settings → Accounts → Add account → Google
   - Sign in with your Google account
3. **Run your app**
4. **Click "Sign in with Google"**
5. **Select your Google account**
6. **Should authenticate successfully**

### Test on Physical Device

1. **Connect your Android device**
2. **Ensure device has Google Play Services**
3. **Run your app**
4. **Click "Sign in with Google"**
5. **Select your Google account**
6. **Should authenticate successfully**

---

## Troubleshooting

### Error: "Developer Error" or Status Code 10

**Cause**: SHA-1 fingerprint not added or incorrect

**Solution**:
1. Verify SHA-1 is correct (run keytool command again)
2. Add SHA-1 to Firebase Console
3. Wait 5 minutes for changes to propagate
4. Rebuild app

### Error: "Sign-in was cancelled" (Status Code 12501)

**Cause**: User cancelled or Web Client ID incorrect

**Solution**:
1. Verify Web Client ID in `strings.xml`
2. Ensure you copied the **Web** client ID (not Android)
3. Rebuild app

### Error: "Network error" (Status Code 7)

**Cause**: No internet connection

**Solution**:
1. Check device internet connection
2. Try again

### Error: "API not enabled"

**Cause**: Google Sign-In not enabled in Firebase

**Solution**:
1. Firebase Console → Authentication → Sign-in method
2. Enable Google
3. Save

### Google Account Picker Doesn't Appear

**Cause**: No Google account on device

**Solution**:
1. Add Google account to device
2. Settings → Accounts → Add account → Google

---

## Verification Checklist

- [ ] SHA-1 fingerprint added to Firebase
- [ ] Google Sign-In enabled in Firebase Console
- [ ] Web Client ID copied correctly
- [ ] `strings.xml` updated with Web Client ID
- [ ] `google-services.json` downloaded and replaced
- [ ] App rebuilt (Clean + Rebuild)
- [ ] Google account added to test device/emulator
- [ ] Google Sign-In button appears in app
- [ ] Clicking button shows Google account picker
- [ ] Selecting account authenticates successfully
- [ ] User appears in Firebase Console → Authentication

---

## Expected Behavior

### Successful Flow

1. **Click "Sign in with Google"**
2. **Google account picker appears**
3. **Select account**
4. **Loading dialog**: "Signing in with Google..."
5. **Success dialog**: "Welcome!"
6. **Navigate to MainActivity**
7. **User appears in Firebase Console**

### Firebase Console Verification

**Authentication → Users**:
```
Email: user@gmail.com
Provider: google.com
Created: Just now
```

**Firestore → users collection**:
```json
{
  "userId": "abc123...",
  "name": "John Doe",
  "email": "user@gmail.com",
  "childAge": 0,
  "createdAt": 1234567890
}
```

---

## Common Mistakes

### ❌ Wrong Client ID Type
**Mistake**: Using Android client ID instead of Web client ID  
**Fix**: Use the Web client ID (type 3 in google-services.json)

### ❌ Missing SHA-1
**Mistake**: Forgot to add SHA-1 fingerprint  
**Fix**: Add SHA-1 to Firebase Console

### ❌ Old google-services.json
**Mistake**: Using old file before enabling Google Sign-In  
**Fix**: Download fresh google-services.json after enabling

### ❌ Not Rebuilding
**Mistake**: Changed strings.xml but didn't rebuild  
**Fix**: Build → Clean Project → Rebuild Project

### ❌ Wrong Package Name
**Mistake**: Package name in Firebase doesn't match app  
**Fix**: Verify package name is `com.example.sensorycontrol`

---

## Alternative: Email/Password Only

If you don't want to set up Google Sign-In, you can:

1. **Remove Google Sign-In button** from layouts:
   - `activity_login.xml`
   - `activity_signup.xml`

2. **Comment out Google Sign-In code** in activities:
   ```java
   // setupGoogleSignIn();
   // googleSignInButton.setOnClickListener(...);
   ```

3. **Use Email/Password only** (already working ✅)

---

## Production Considerations

### Release Build SHA-1

Before publishing to Play Store:

1. **Generate release keystore** (if not done):
   ```bash
   keytool -genkey -v -keystore release.keystore -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Get release SHA-1**:
   ```bash
   keytool -list -v -keystore release.keystore -alias my-key-alias
   ```

3. **Add release SHA-1** to Firebase Console

4. **Update google-services.json**

### Play Store SHA-1

After uploading to Play Store:

1. **Google Play Console** → **Release** → **Setup** → **App signing**
2. **Copy "SHA-1 certificate fingerprint"**
3. **Add to Firebase Console**

---

## Summary

**Setup Time**: ~10 minutes

**Steps**:
1. Get SHA-1 fingerprint
2. Add to Firebase Console
3. Enable Google Sign-In in Firebase
4. Get Web Client ID
5. Update strings.xml
6. Download google-services.json
7. Rebuild app
8. Test

**Result**: Working Google Sign-In authentication! 🎉

---

## Need Help?

### Firebase Documentation
https://firebase.google.com/docs/auth/android/google-signin

### Common Issues
https://firebase.google.com/docs/auth/android/errors

### Stack Overflow
Search: "Firebase Google Sign-In Android"

---

**Status**: Optional but recommended for better UX  
**Fallback**: Email/Password authentication works without Google Sign-In
