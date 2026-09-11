# Fuel & Energy Tracker (CarManager)

An Android application built with Kotlin and Jetpack Compose for tracking vehicle fuel and energy consumption, recurring and total maintenance spending, average fuel prices, and providing on-device AI efficiency analysis.

## Features

- **Fuel & Energy Logs**: Record odometer readings, fuel amount, price per unit, total cost, fuel grades, fuel stations, and full-tank flags.
- **Analytics & Dashboard**: Visual breakdown of spending by week, month, and year; price history trends; and fuel economy tracking (L/100km, MPG US, MPG UK, km/L, km/kWh).
- **On-Device Smart Advisor**: Automated heuristic and AI-assisted analysis that generates efficiency scores, trend predictions, and actionable advice.
- **Multi-Vehicle Support**: Manage profiles for multiple vehicles with archiving and restore capabilities.
- **Backup & Portability**: Export and import complete vehicle histories and logs to CSV format.

## Tech Stack

- **UI**: 100% Jetpack Compose with Material 3 design system.
- **Language**: Kotlin (v2.2+), Coroutines, and StateFlow.
- **Database**: Room Database with SQLite.
- **Architecture**: MVVM with Repository Pattern and Uni-directional Data Flow.
- **Testing**: JUnit 4, Robolectric (SDK 34), Roborazzi screenshot testing, Kotlinx-Kover.
- **Build System**: Gradle 9.3.1 with Android Gradle Plugin 9.1.1.

## Getting Started

### Prerequisites

- **JDK**: Java 21 (e.g., Amazon Corretto 21)
- **Android SDK**: Platform 36.1 (`compileSdk = 36`, `minSdk = 24`)
- Ensure `local.properties` contains your Android SDK path:
  ```properties
  sdk.dir=/Users/<username>/Library/Android/sdk
  ```

### Build Commands

Build the debug APK:
```bash
./gradlew assembleDebug
```
The output APK will be placed in `app/build/outputs/apk/debug/`.

Run unit tests:
```bash
./gradlew testDebugUnitTest
```

Generate code coverage reports:
```bash
./gradlew koverHtmlReport
```
Open `app/build/reports/kover/html/index.html` in your browser.

Record / verify screenshot tests with Roborazzi:
```bash
./gradlew recordRoborazziDebug
./gradlew verifyRoborazziDebug
```

## Signing and Publishing

- **Namespace**: `today.takaki`
- **Application ID**: `today.takaki.fueltracker.vxqmkz`

### Keystores

- **Debug Keystore**: Generated locally at `debug.keystore` (git-ignored) with standard Android debug credentials (`android / androiddebugkey`).
- **Release Keystore**: Signs release builds using `my-upload-key.jks` or a custom path defined by `KEYSTORE_PATH`.

To build a release bundle:
```bash
export KEYSTORE_PATH="/path/to/my-upload-key.jks"
export STORE_PASSWORD="your-store-password"
export KEY_PASSWORD="your-key-password"
./gradlew bundleRelease
```

### Certificate Fingerprints for Cloud & AI Studio

When registering your package name (`today.takaki.fueltracker.vxqmkz`) in Google Cloud Console, Firebase, or Google AI Studio, add your SHA-256 certificate fingerprint:

```bash
# Debug fingerprint
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android

# Release upload fingerprint
keytool -list -v -keystore my-upload-key.jks -alias upload
```
