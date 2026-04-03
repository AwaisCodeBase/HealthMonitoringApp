# Phase 4: Authentication Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Android Application                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐  │
│  │ SplashActivity│      │LoginActivity │      │SignupActivity│  │
│  │              │      │              │      │              │  │
│  │ - Check Auth │─────▶│ - Email/Pass │◀────▶│ - Email/Pass │  │
│  │ - Route User │      │ - Google     │      │ - Google     │  │
│  └──────┬───────┘      └──────┬───────┘      └──────┬───────┘  │
│         │                     │                      │           │
│         │                     └──────────┬───────────┘           │
│         │                                │                       │
│         │                                ▼                       │
│         │                      ┌──────────────────┐             │
│         │                      │   AuthManager    │             │
│         │                      │   (Singleton)    │             │
│         │                      │                  │             │
│         │                      │ - signUp()       │             │
│         │                      │ - signIn()       │             │
│         │                      │ - signOut()      │             │
│         │                      │ - isLoggedIn()   │             │
│         │                      │ - getCurrentUser()│            │
│         │                      └────────┬─────────┘             │
│         │                               │                       │
│         ▼                               ▼                       │
│  ┌──────────────────────────────────────────────────────┐      │
│  │              MainActivity (Protected)                 │      │
│  │  ┌────────────────────────────────────────────────┐  │      │
│  │  │  Bottom Navigation                             │  │      │
│  │  │  ┌──────────┬──────────┬──────────┬─────────┐ │  │      │
│  │  │  │Dashboard │   Scan   │Monitoring│Settings │ │  │      │
│  │  │  │Fragment  │ Fragment │ Fragment │Fragment │ │  │      │
│  │  │  │          │          │          │         │ │  │      │
│  │  │  │          │          │          │ Logout  │ │  │      │
│  │  │  └──────────┴──────────┴──────────┴─────────┘ │  │      │
│  │  └────────────────────────────────────────────────┘  │      │
│  └──────────────────────────────────────────────────────┘      │
│                                                                  │
└──────────────────────────┬───────────────────────────────────────┘
                           │
                           │ Firebase SDK
                           │
┌──────────────────────────▼───────────────────────────────────────┐
│                      Firebase Services                            │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────┐         ┌─────────────────────┐        │
│  │ Firebase Auth       │         │ Cloud Firestore     │        │
│  │                     │         │                     │        │
│  │ - Email/Password    │◀───────▶│ users/              │        │
│  │ - Google OAuth 2.0  │         │   {userId}/         │        │
│  │ - Session Tokens    │         │     - name          │        │
│  │ - User Management   │         │     - email         │        │
│  │                     │         │     - childAge      │        │
│  └─────────────────────┘         │     - createdAt     │        │
│                                   └─────────────────────┘        │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

---

## Authentication Flow Diagram

```
┌─────────┐
│  Start  │
└────┬────┘
     │
     ▼
┌─────────────────┐
│ SplashActivity  │
│   (2 seconds)   │
└────┬────────────┘
     │
     ▼
┌─────────────────┐
│ Check Firebase  │
│ getCurrentUser()│
└────┬────────────┘
     │
     ├─────────────────────────────┐
     │                             │
     ▼                             ▼
┌─────────────┐              ┌─────────────┐
│ User Exists │              │  User NULL  │
└─────┬───────┘              └─────┬───────┘
      │                            │
      │                            ▼
      │                      ┌──────────────┐
      │                      │LoginActivity │
      │                      └──────┬───────┘
      │                             │
      │                      ┌──────┴──────┐
      │                      │             │
      │                      ▼             ▼
      │              ┌──────────────┐  ┌──────────────┐
      │              │ Email/Pass   │  │ Google Sign  │
      │              │   Login      │  │     In       │
      │              └──────┬───────┘  └──────┬───────┘
      │                     │                 │
      │                     └────────┬────────┘
      │                              │
      │                              ▼
      │                      ┌──────────────┐
      │                      │ Has Account? │
      │                      └──────┬───────┘
      │                             │
      │                      ┌──────┴──────┐
      │                      │             │
      │                      ▼             ▼
      │                   ┌─────┐    ┌──────────────┐
      │                   │ Yes │    │SignupActivity│
      │                   └──┬──┘    └──────┬───────┘
      │                      │              │
      │                      │              ▼
      │                      │       ┌──────────────┐
      │                      │       │Create Account│
      │                      │       └──────┬───────┘
      │                      │              │
      │                      └──────┬───────┘
      │                             │
      │                             ▼
      │                      ┌──────────────┐
      │                      │ Auth Success │
      │                      └──────┬───────┘
      │                             │
      └─────────────────────────────┘
                                    │
                                    ▼
                            ┌──────────────┐
                            │ MainActivity │
                            │  (Protected) │
                            └──────┬───────┘
                                   │
                            ┌──────┴──────┐
                            │             │
                            ▼             ▼
                    ┌──────────────┐  ┌──────────────┐
                    │ Use Features │  │   Settings   │
                    │ - Dashboard  │  │   - Logout   │
                    │ - BLE Scan   │  └──────┬───────┘
                    │ - Monitoring │         │
                    └──────────────┘         │
                                             ▼
                                      ┌──────────────┐
                                      │ Sign Out     │
                                      │ Clear Session│
                                      └──────┬───────┘
                                             │
                                             ▼
                                      ┌──────────────┐
                                      │LoginActivity │
                                      └──────────────┘
```

---

## Data Flow Diagram

### Signup Flow

```
User Input                AuthManager              Firebase Auth         Firestore
    │                         │                         │                    │
    │  Enter credentials      │                         │                    │
    ├────────────────────────▶│                         │                    │
    │                         │                         │                    │
    │                         │  createUserWithEmail    │                    │
    │                         ├────────────────────────▶│                    │
    │                         │                         │                    │
    │                         │  ◀─── User Created ────│                    │
    │                         │                         │                    │
    │                         │  Create Profile         │                    │
    │                         ├─────────────────────────┼───────────────────▶│
    │                         │                         │                    │
    │                         │  ◀─── Profile Saved ───┼────────────────────│
    │                         │                         │                    │
    │  ◀─── Success ──────────│                         │                    │
    │                         │                         │                    │
    │  Navigate to Main       │                         │                    │
    │                         │                         │                    │
```

### Login Flow

```
User Input                AuthManager              Firebase Auth         Firestore
    │                         │                         │                    │
    │  Enter credentials      │                         │                    │
    ├────────────────────────▶│                         │                    │
    │                         │                         │                    │
    │                         │  signInWithEmail        │                    │
    │                         ├────────────────────────▶│                    │
    │                         │                         │                    │
    │                         │  ◀─── Auth Token ──────│                    │
    │                         │                         │                    │
    │  ◀─── Success ──────────│                         │                    │
    │                         │                         │                    │
    │  Navigate to Main       │                         │                    │
    │                         │                         │                    │
```

### Google Sign-In Flow

```
User Action              AuthManager         Google Sign-In      Firebase Auth    Firestore
    │                        │                     │                  │               │
    │  Click Google Button   │                     │                  │               │
    ├───────────────────────▶│                     │                  │               │
    │                        │                     │                  │               │
    │                        │  Launch Picker      │                  │               │
    │                        ├────────────────────▶│                  │               │
    │                        │                     │                  │               │
    │  ◀─── Select Account ──┼─────────────────────│                  │               │
    │                        │                     │                  │               │
    │                        │  ◀─── ID Token ─────│                  │               │
    │                        │                     │                  │               │
    │                        │  signInWithCredential                  │               │
    │                        ├────────────────────────────────────────▶│               │
    │                        │                     │                  │               │
    │                        │  ◀─── Auth Success ────────────────────│               │
    │                        │                     │                  │               │
    │                        │  Check/Create Profile                  │               │
    │                        ├────────────────────────────────────────┼──────────────▶│
    │                        │                     │                  │               │
    │  ◀─── Success ─────────│                     │                  │               │
    │                        │                     │                  │               │
    │  Navigate to Main      │                     │                  │               │
    │                        │                     │                  │               │
```

---

## Class Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        AuthManager                          │
├─────────────────────────────────────────────────────────────┤
│ - instance: AuthManager (static)                            │
│ - auth: FirebaseAuth                                        │
│ - db: FirebaseFirestore                                     │
│ - googleSignInClient: GoogleSignInClient                    │
├─────────────────────────────────────────────────────────────┤
│ + getInstance(): AuthManager                                │
│ + initializeGoogleSignIn(context, webClientId): void        │
│ + getGoogleSignInClient(): GoogleSignInClient               │
│ + signUp(email, password, name, callback): void             │
│ + signIn(email, password, callback): void                   │
│ + signInWithGoogle(task, callback): void                    │
│ + signOut(context): void                                    │
│ + isUserLoggedIn(): boolean                                 │
│ + getCurrentUser(): FirebaseUser                            │
│ + getUserProfile(callback): void                            │
│ + updateUserProfile(updates, callback): void                │
│ - createUserProfile(userId, name, email, callback): void    │
│ - checkAndCreateProfile(user, callback): void               │
│ - firebaseAuthWithGoogle(idToken, callback): void           │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ uses
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│SplashActivity│    │LoginActivity │    │SignupActivity│
├──────────────┤    ├──────────────┤    ├──────────────┤
│+ onCreate()  │    │+ onCreate()  │    │+ onCreate()  │
│- checkAuth() │    │- attemptLogin│    │- attemptSignup│
└──────────────┘    │- setupGoogle │    │- setupGoogle │
                    │- showError() │    │- showError() │
                    └──────────────┘    └──────────────┘
                            │
                            │ navigates to
                            │
                            ▼
                    ┌──────────────┐
                    │MainActivity  │
                    ├──────────────┤
                    │+ onCreate()  │
                    │- checkPerms()│
                    └──────────────┘
                            │
                            │ contains
                            │
                    ┌───────┴────────┐
                    │                │
                    ▼                ▼
            ┌──────────────┐  ┌──────────────┐
            │Dashboard     │  │Settings      │
            │Fragment      │  │Fragment      │
            ├──────────────┤  ├──────────────┤
            │+ onViewCreated│ │+ onViewCreated│
            └──────────────┘  │- loadUserInfo│
                              │- performLogout│
                              └──────────────┘
```

---

## Callback Pattern

```
┌──────────────────────────────────────────────────────────┐
│                    AuthCallback Interface                 │
├──────────────────────────────────────────────────────────┤
│ + onSuccess(): void                                       │
│ + onFailure(error: String): void                          │
└──────────────────────────────────────────────────────────┘
                          ▲
                          │ implements
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│LoginActivity │  │SignupActivity│  │Settings      │
│              │  │              │  │Fragment      │
│new Callback()│  │new Callback()│  │new Callback()│
│{             │  │{             │  │{             │
│  onSuccess() │  │  onSuccess() │  │  onSuccess() │
│  onFailure() │  │  onFailure() │  │  onFailure() │
│}             │  │}             │  │}             │
└──────────────┘  └──────────────┘  └──────────────┘
```

---

## State Diagram

```
                    ┌──────────────┐
                    │ Unauthenticated│
                    └───────┬────────┘
                            │
                    ┌───────┴────────┐
                    │                │
                    ▼                ▼
            ┌──────────────┐  ┌──────────────┐
            │   Signing Up │  │  Signing In  │
            └───────┬──────┘  └───────┬──────┘
                    │                 │
                    └────────┬────────┘
                             │
                             ▼
                    ┌──────────────┐
                    │ Authenticated │
                    └───────┬───────┘
                            │
                    ┌───────┴────────┐
                    │                │
                    ▼                ▼
            ┌──────────────┐  ┌──────────────┐
            │ Using App    │  │ Signing Out  │
            └──────────────┘  └───────┬──────┘
                                      │
                                      ▼
                            ┌──────────────┐
                            │Unauthenticated│
                            └──────────────┘
```

---

## Security Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Client (Android App)                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────────────────────────────────────────┐     │
│  │              User Input Layer                      │     │
│  │  - Email/Password validation                       │     │
│  │  - Input sanitization                              │     │
│  │  - Client-side checks                              │     │
│  └────────────────┬───────────────────────────────────┘     │
│                   │                                          │
│                   ▼                                          │
│  ┌────────────────────────────────────────────────────┐     │
│  │           AuthManager (Business Logic)             │     │
│  │  - Callback pattern                                │     │
│  │  - Error handling                                  │     │
│  │  - State management                                │     │
│  └────────────────┬───────────────────────────────────┘     │
│                   │                                          │
└───────────────────┼──────────────────────────────────────────┘
                    │ HTTPS (TLS 1.2+)
                    │ Encrypted
                    │
┌───────────────────▼──────────────────────────────────────────┐
│                  Firebase Services (Cloud)                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────────────────────────────────────────┐     │
│  │           Firebase Authentication                  │     │
│  │  - Password hashing (bcrypt)                       │     │
│  │  - Token generation (JWT)                          │     │
│  │  - Session management                              │     │
│  │  - OAuth 2.0 (Google)                              │     │
│  └────────────────┬───────────────────────────────────┘     │
│                   │                                          │
│                   ▼                                          │
│  ┌────────────────────────────────────────────────────┐     │
│  │            Cloud Firestore                         │     │
│  │  - Security rules                                  │     │
│  │  - User-based access control                       │     │
│  │  - Data encryption at rest                         │     │
│  │  - Audit logging                                   │     │
│  └────────────────────────────────────────────────────┘     │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

---

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Development Environment                   │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Android Studio                                              │
│  ├─ app/                                                     │
│  │  ├─ google-services.json (Debug)                         │
│  │  ├─ build.gradle.kts                                     │
│  │  └─ src/                                                 │
│  │     └─ main/                                             │
│  │        ├─ java/                                          │
│  │        │  └─ com/example/sensorycontrol/                 │
│  │        │     ├─ auth/AuthManager.java                    │
│  │        │     └─ activities/                              │
│  │        └─ res/                                           │
│  │           ├─ layout/                                     │
│  │           └─ values/strings.xml                          │
│  │                                                           │
│  └─ Debug Build                                             │
│     ├─ Debug Keystore (~/.android/debug.keystore)           │
│     └─ SHA-1 Fingerprint → Firebase Console                 │
│                                                               │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ Deploy
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                  Firebase Project (Cloud)                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Project: sensory-control-xxxxx                              │
│  ├─ Authentication                                           │
│  │  ├─ Email/Password (Enabled)                             │
│  │  └─ Google Sign-In (Optional)                            │
│  │                                                           │
│  ├─ Firestore Database                                      │
│  │  ├─ users/ (Collection)                                  │
│  │  └─ Security Rules                                       │
│  │                                                           │
│  ├─ Project Settings                                        │
│  │  ├─ Android App (com.example.sensorycontrol)            │
│  │  ├─ SHA-1 Fingerprints                                  │
│  │  └─ google-services.json                                │
│  │                                                           │
│  └─ Analytics (Optional)                                    │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## For Dissertation

Use these diagrams in your dissertation to illustrate:

1. **System Architecture** - Overall structure
2. **Authentication Flow** - User journey
3. **Data Flow** - How data moves through the system
4. **Class Diagram** - Code organization
5. **Security Architecture** - Security layers
6. **Deployment Architecture** - Development to production

These diagrams demonstrate:
- Professional software design
- Security awareness
- Proper architecture patterns
- Clear separation of concerns
- Industry best practices

---

**Tip**: You can recreate these diagrams using tools like:
- Draw.io (diagrams.net)
- Lucidchart
- PlantUML
- Microsoft Visio
- Or include as ASCII art in documentation
