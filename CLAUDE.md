# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**myquietwave** is a multi-platform automation suite for radio news automation with Jewish calendar/Shabbat time awareness:
- **Android** (`android/`): "My Quiet Channel" Kotlin/Compose app (Google Play: `com.shahartal.myquietchannel`)
- **iOS** (`ios/`): "VolumeCycler" SwiftUI app
- **Java Backend** (root `src/`): Selenium-based automated radio streaming

## Build Commands

### Android
```bash
cd android
./gradlew build              # Full build
./gradlew assembleDebug      # Build APK
./gradlew test               # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests (requires device/emulator)
./gradlew clean              # Clean build artifacts
```

### iOS
```bash
cd ios
xcodebuild -scheme VolumeCycler -configuration Debug -arch arm64 -sdk iphoneos build
```

### Java Backend
```bash
./gradlew build
java -cp "build/libs/*" edu.automations.news.Radio
# Chrome must be running with remote debugging BEFORE starting Java:
# chrome --remote-debugging-port=9222 --user-data-dir="path/to/profile"
```

## Architecture

### Data Flow
```
User settings (location, volume, hours)
  → HebCal API (https://www.hebcal.com/) — Shabbat times, holidays, Zmanim
  → VolumeCycleService (Android) / VolumeManager (iOS) — timing scheduler
  → AudioManager (Android) / MPVolumeView (iOS) — system volume control
```

The core cycle is: **4-hour mute → 3-minute play** on repeat, paused during Shabbat.

### Android (`android/app/src/main/java/`)
- **`MainActivity.kt`**: All UI (Jetpack Compose + legacy Spinner), settings persistence, location picker, Hebrew calendar display, Firebase integration, in-app update management
- **`VolumeCycleService.kt`**: Foreground service (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`), coroutine-based volume cycle scheduler, Shabbat guard
- **`parasha/`**: Retrofit + HebCal API integration
  - `JsonHebCalShabbatApi.kt`: Retrofit interface (Shabbat, Zmanim, Daf Yomi endpoints)
  - `RetrofitInstance.kt`: Singleton with LENIENT Gson parsing (required—HebCal returns lenient JSON)
  - Data models: `HebCal.kt`, `HebCalZmanimModel.kt`, `HebCalLocationModel.kt`, `Item.kt`, `Leyning.kt`
- **`Utils.kt`**: Hebrew numeral conversion, city-name→GeoName ID mappings, location helpers

### iOS (`ios/`)
- **`App.swift`**: Entry point, AppDelegate, background task scheduling
- **`VolumeManager.swift`**: Volume cycle state machine (Timer-based), AVFoundation + MediaPlayer
- **`SceneDelegate.swift`**: Background task delegation

### Java Backend (`src/main/java/edu/automations/news/Radio.java`)
- Attaches to Chrome on port 9222 via Selenium RemoteWebDriver
- Monitors clock, triggers at hours: 1,3,5,7,8,9,11,13,15,17,19,21,23
- Navigates to Galatz stream URL and listens for ~6 minutes per session

## Key Patterns

**Dependency versions**: Centralized in `android/gradle/libs.versions.toml` (Kotlin 2.3.20, Compose BOM 2026.03.01, Gradle 9.0.1)

**Localization**: UI strings in `res/values/strings.xml` with translations in `values-iw/` (Hebrew) and `values-ru/` (Russian) and `values-es/` (Spanish) and `values-fr/` (French)

**Time handling**: Uses `java.time` (LocalDateTime, ZonedDateTime, ZoneId) for timezone-aware scheduling; `HebrewCalendar` for Hebrew date display

**Location**: `FusedLocationProviderClient` provides lat/lon for HebCal queries; city name → GeoName ID mappings in `Utils.kt`

## Common Gotchas

- Always use `./gradlew` wrapper (not system gradle) — Gradle 9.x requires Java 11+
- Physical Android device required for volume control testing; emulator volume control is limited
- Android 12+ requires both `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` permissions
- Selenium backend crashes silently if Chrome isn't already running on port 9222
- HebCal API returns non-strict JSON — `RetrofitInstance` must use LENIENT Gson parsing
