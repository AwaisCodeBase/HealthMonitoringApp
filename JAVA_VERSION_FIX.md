# Java Version Fix

## Problem
You're encountering a Gradle build error because you're using Java 25 (preview/early access), which is not yet supported by Gradle.

Error: `java.lang.IllegalArgumentException: 25`

## Solution

You need to use Java 17 (LTS) which is the version specified in your project configuration.

### Option 1: Use Java 17 (Recommended)

#### On macOS (using Homebrew):

```bash
# Install Java 17 if not already installed
brew install openjdk@17

# Set JAVA_HOME for current session
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# Or for Intel Macs:
export JAVA_HOME=/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# Verify Java version
java -version
# Should show: openjdk version "17.x.x"
```

#### Make it permanent (add to ~/.zshrc):

```bash
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home' >> ~/.zshrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### Option 2: Use SDKMAN (Recommended for managing multiple Java versions)

```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 17
sdk install java 17.0.9-tem

# Use Java 17
sdk use java 17.0.9-tem

# Make it default
sdk default java 17.0.9-tem

# Verify
java -version
```

### Option 3: Specify Java version for Gradle only

Create/edit `gradle.properties` in your project root:

```properties
org.gradle.java.home=/path/to/java17
```

## After Fixing Java Version

1. **Verify Java version:**
```bash
java -version
# Should show Java 17
```

2. **Clean and rebuild:**
```bash
./gradlew clean
./gradlew build
```

3. **Sync Gradle in Android Studio:**
- File → Sync Project with Gradle Files

## Quick Fix Script

Save this as `fix-java.sh` and run it:

```bash
#!/bin/bash

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    echo "Homebrew not found. Please install it first:"
    echo '/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"'
    exit 1
fi

# Install Java 17
echo "Installing Java 17..."
brew install openjdk@17

# Detect architecture
if [[ $(uname -m) == 'arm64' ]]; then
    JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
else
    JAVA_HOME="/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
fi

# Add to shell profile
echo "export JAVA_HOME=$JAVA_HOME" >> ~/.zshrc
echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\"" >> ~/.zshrc

echo "Java 17 installed! Please run: source ~/.zshrc"
echo "Then verify with: java -version"
```

## Verify Installation

```bash
# Check Java version
java -version

# Check JAVA_HOME
echo $JAVA_HOME

# Check Gradle can find Java
./gradlew --version
```

## Expected Output

```
openjdk version "17.0.9" 2023-10-17
OpenJDK Runtime Environment (build 17.0.9+9)
OpenJDK 64-Bit Server VM (build 17.0.9+9, mixed mode, sharing)
```

## Now Add OkHttp Dependency

After fixing Java version, the OkHttp dependency will work. The dependencies have already been added to `app/build.gradle.kts`:

```kotlin
// WebSocket client for WiFi communication
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// JSON parsing
implementation("com.google.code.gson:gson:2.10.1")
```

## Build the Project

```bash
# Clean build
./gradlew clean

# Build
./gradlew build

# Or in Android Studio
# Build → Rebuild Project
```

## Troubleshooting

### If you still get Java version errors:

1. **Kill Gradle daemon:**
```bash
./gradlew --stop
```

2. **Clear Gradle cache:**
```bash
rm -rf ~/.gradle/caches/
```

3. **Restart Android Studio**

4. **Invalidate caches:**
- File → Invalidate Caches → Invalidate and Restart

### If OkHttp still not found:

1. **Sync Gradle:**
```bash
./gradlew --refresh-dependencies
```

2. **In Android Studio:**
- File → Sync Project with Gradle Files

3. **Check internet connection** (Gradle needs to download dependencies)

## Summary

1. Install Java 17 (LTS)
2. Set JAVA_HOME environment variable
3. Verify with `java -version`
4. Run `./gradlew clean build`
5. Sync Gradle in Android Studio

The OkHttp dependency is already added to your build.gradle.kts file, so once Java 17 is configured, everything should work!
