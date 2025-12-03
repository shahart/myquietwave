# AI Coding Agent Instructions for Automations

## Project Overview

**automations** is a multi-platform automation suite primarily for radio news automation, with implementations across:
- **Android**: "My Quiet Channel" app (Kotlin/Compose) - automates media volume cycling
- **iOS**: VolumeCycler app (SwiftUI) - background-based volume cycling
- **Backend**: Java Selenium automation for headless browser-based radio streaming

**Key Purpose**: Enable scheduled, automated listening to hourly radio news broadcasts with volume management, Shabbat time awareness, and location-based customization.

## Architecture & Major Components

### Android App (`android/app/`)
- **Build**: Gradle 9.1.0, Kotlin 2.2.21, Java 11, Compose, Firebase Analytics
- **Main Components**:
  - `MainActivity.kt`: UI controller with settings, location picker, Hebrew calendar integration
  - `VolumeCycleService.kt`: Foreground service managing volume timing cycles (4h mute, 3m play)
  - `parasha/` package: Retrofit integration with HebCal API for Jewish holidays/Shabbat times
  - `Utils.kt`: Hebrew number/city name conversions, location utilities
- **Key Dependencies**: Retrofit2 (API calls), Firebase Analytics, Google Play Services Location, AndroidX Compose
- **Data Flow**: UI settings → VolumeCycleService → AudioManager (native volume control)

### iOS App (`ios/`)
- **Build**: Swift Package Manager, iOS 15.0+
- **Main Components**:
  - `App.swift`: App entry point with SwiftUI
  - `VolumeManager.swift`: Core volume cycling logic (AVFoundation, MediaPlayer)
  - `SceneDelegate.swift`: Background task management
- **Key Feature**: Background modes (background processing, background fetch) enable continuous operation when app backgrounded

### Java Backend (`src/main/java/`)
- **Purpose**: Automated radio streaming with scheduled news capture
- **Tech**: Selenium WebDriver (Chrome remote debugging), runs continuously monitoring hourly schedule
- **Logic**: Attaches to Chrome browser on port 9222, navigates to streaming URL at specific hours (1,3,5,7,8,9,11,13,15,17,19,21,23), listens for ~6 minutes
- **Dependencies**: Selenium 4.31.0, Java 21

## Critical Developer Workflows

### Build & Test

**Android**:
```bash
cd android
./gradlew build                    # Full build with Gradle wrapper
./gradlew assembleDebug            # Build APK
./gradlew test                     # Run unit tests
./gradlew connectedAndroidTest     # Run instrumented tests on device/emulator
```

**iOS**:
```bash
cd ios
xcodebuild -scheme VolumeCycler -configuration Debug -arch arm64 -sdk iphoneos build
```

**Java Backend**:
```bash
./gradlew build
java -cp "build/libs/*" edu.automations.news.Radio
# Requires Chrome with remote debugging enabled:
# chrome --remote-debugging-port=9222 --user-data-dir="path/to/profile"
```

### Deployment & Release

**Android**:
- Published to Google Play as "My Quiet Channel"
- Current version: 1.21 (versionCode 32)
- Signing with debug keystore in `release` build type
- CI/CD via GitHub Actions (see `AndroidBuild.yml`)

### Key Considerations

- **Gradle Wrapper**: Always use `./gradlew` (not system gradle) to ensure consistency
- **API Integration**: HebCal API (`https://www.hebcal.com/`) returns Jewish calendar/Shabbat times; uses Retrofit with LENIENT Gson parsing
- **Background Execution**: Android foreground service + iOS background tasks required for continuous operation
- **Location Permissions**: Both platforms require location access for Shabbat time calculations

## Project-Specific Patterns

### Kotlin/Android Conventions
- **Build DSL**: Kotlin DSL (`build.gradle.kts`) with `libs.versions.toml` for centralized dependency management
- **Compose UI**: MainActivity uses mix of Compose + traditional Views (legacy widgets like Spinner)
- **Coroutines**: VolumeCycleService uses kotlinx.coroutines for async timing operations
- **Localization**: Hebrew strings (`values-iw/strings.xml`), Russian (`values-ru/`), English (`values-en/`)

### API Integration Pattern (Retrofit)
- Singleton `RetrofitInstance` with LENIENT Gson parsing (handles lenient JSON)
- Scalars converter fallback for non-JSON responses
- Example: `JsonHebCalShabbatApi` queries HebCal for locations, holidays, Shabbat times

### Time & Calendar Handling
- Uses `java.time` (LocalDateTime, ZonedDateTime, ZoneId) for timezone-aware scheduling
- Hebrew calendar support: `HebrewCalendar` for date conversions, `Utils.getYY()` for Hebrew numeral display
- Scheduling based on hour boundaries (triggers at :00 minute mark each hour)

### Resource Strings Pattern
- Define all UI strings in `res/values/strings.xml` with translation files for each locale
- Reference via `getString(R.string.key_name)` in code

## External Dependencies & Integration Points

### HebCal API
- **Endpoint**: `https://www.hebcal.com/`
- **Usage**: Fetch Shabbat times, holiday dates, candle lighting times for location-based automation
- **Integration**: `parasha/` package models + Retrofit

### Firebase Analytics
- Tracks app usage patterns, user locations for anonymized insights
- Configured via `google-services.json` (Android) and Firebase SDK initialization

### Android Location Services
- Google Play Services Location (FusedLocationProviderClient) provides GPS/network-based location
- Required for Shabbat time lookup (different times per location)

### Radio Streaming
- **URL**: `https://glzwizzlv.bynetcdn.com/glglz_mp3?awCollectionId=misc&awEpisodeId=glglz`
- Accessed via Selenium (backend) and intended for volume cycling app triggering

## Testing & Debugging Checklist

- **Android Device Requirements**: Physical device needed for volume control testing (emulator volume control limited)
- **Background Testing**: Enable "Background App Refresh" in device settings
- **Location Spoofing**: Use mock location provider for testing different Shabbat times
- **Gradle Sync Issues**: Run `./gradlew clean` + IDE cache invalidation if build fails
- **Selenium Debug**: Chrome must have `--remote-debugging-port=9222` before Java process starts

## Common Gotchas

1. **Version Mismatch**: Gradle 9.1.0 requires Java 11+ (not Java 8)
2. **Location Permissions**: Android 6.0+ requires runtime permissions; add to `AndroidManifest.xml` and request at runtime
3. **Foreground Service**: Android 12+ requires `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK` permissions
4. **Shabbat Calendar**: Times vary by location; always geolocate before scheduling
5. **Selenium Remote Debugging**: Java process crashes silently if Chrome remote port unavailable—verify port 9222 open first
