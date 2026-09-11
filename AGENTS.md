# AGENTS.md

Instructions, architecture context, and conventions for AI coding agents working on the CarManager repository.

## Project Overview

- **App Name**: Fuel & Energy Tracker (CarManager)
- **Namespace**: `today.takaki`
- **Application ID**: `today.takaki.fueltracker.vxqmkz`
- **Platform**: Android (`minSdk = 24`, `targetSdk = 36`, `compileSdk = 36.1`)
- **Tech Stack**: Kotlin 2.2, Jetpack Compose (Material 3), Room Database, Coroutines/StateFlow, KSP, Roborazzi, Kover.

## Setup & Build Commands

All build operations use the Gradle wrapper with Java 21:

```bash
# Build debug APK
./gradlew assembleDebug

# Build release bundle (AAB)
./gradlew bundleRelease

# Run unit tests
./gradlew testDebugUnitTest

# Generate test code coverage (HTML report in app/build/reports/kover/html/index.html)
./gradlew koverHtmlReport

# Generate test code coverage (XML report for CI in app/build/reports/kover/report.xml)
./gradlew koverXmlReport

# Verify Roborazzi screenshot tests
./gradlew verifyRoborazziDebug

# Record new baseline screenshots
./gradlew recordRoborazziDebug
```

> **Note**: `local.properties` must declare `sdk.dir=<path-to-android-sdk>`. This file is git-ignored.

## Code Style & Conventions

### Language & Modern Android Standards
- Target Kotlin 2.2+ features and strict nullability.
- Use coroutines and `StateFlow` for asynchronous and reactive state. Avoid callbacks or blocking calls on the main thread.
- Follow Kotlin standard style: 2 spaces indentation, clear naming conventions (`PascalCase` for classes/composables, `camelCase` for functions and variables).

### Jetpack Compose & UI
- Use Material 3 (`androidx.compose.material3`) exclusively.
- All composables live under `today.takaki.ui`.
- Deconstruct UI into reusable components in `today.takaki.ui.components` and screen containers in `today.takaki.ui.screens`.
- Composable functions that emit UI must be named in `PascalCase` and accept an optional `modifier: Modifier = Modifier` as their first optional parameter.
- Use auto-mirrored icons (`Icons.AutoMirrored.*`) when dealing with directional icons (e.g., arrows, trends, lists) to support RTL layouts.

### Architecture & Data Flow
- **Pattern**: Unidirectional Data Flow (UDF) with MVVM.
- **Repository**: Single source of truth for database and calculations (`today.takaki.data.repository.FuelTrackerRepository`).
- **ViewModel**: Exposes immutable `StateFlow` to composables via `today.takaki.ui.viewmodel.FuelTrackerViewModel`.
- **Database**: Room database defined in `today.takaki.data.db.AppDatabase`. All DAOs must expose `Flow` for reactive queries.
- **Models**: Data classes located in `today.takaki.data.model`.

## Directory Structure

```text
CarManager/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/today/takaki/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── advisor/          # On-device AI analysis service
│   │   │   │   │   ├── db/               # Room Database, DAOs
│   │   │   │   │   ├── model/            # Entities, data classes, enums
│   │   │   │   │   ├── repository/       # Data repository layer
│   │   │   │   │   └── util/             # CSV import/export helpers
│   │   │   │   └── ui/
│   │   │   │       ├── components/       # Reusable UI cards, dialogs, charts
│   │   │   │       ├── screens/          # Top-level composable screens
│   │   │   │       ├── theme/            # Material 3 theme, colors, typography
│   │   │   │       └── viewmodel/        # ViewModel managing app state
│   │   │   └── res/                      # Android resources
│   │   └── test/java/today/takaki/       # Unit & Robolectric tests
│   └── build.gradle.kts                  # App module build configuration
├── gradle/
│   └── libs.versions.toml                # Version catalog for dependencies and plugins
├── local.properties                      # Android SDK path (git-ignored)
├── build.gradle.kts                      # Root Gradle configuration
└── settings.gradle.kts                   # Settings and repositories
```

## Signing & Security Rules

- **Never commit credentials**: Never commit `.jks`, `.keystore`, `.env`, or passwords. Both `*.jks` and `*.keystore` are protected in `.gitignore`.
- **Debug signing**: The repository uses `${rootDir}/debug.keystore` for consistent debug builds.
- **Release signing**: Pass credentials using environment variables:
  - `KEYSTORE_PATH`
  - `STORE_PASSWORD`
  - `KEY_PASSWORD`
  - `upload` is the designated release key alias.

## Testing & Quality Assurance

- Always run `./gradlew testDebugUnitTest` to verify changes before completing tasks.
- Keep unit test coverage healthy. Check coverage metrics using `./gradlew koverHtmlReport`.
- Test classes should be placed under `app/src/test/java/today/takaki/` matching the main source package hierarchy.
