# Repository Guidelines

## Project Structure & Module Organization

This repository contains three active code areas:

- `src/main/java/edu/automations/news/`: the original Java/Selenium utility (`Radio.java`).
- `android/`: the current Android app project. Main code lives in `android/app/src/main/java/com/shahartal/myquietchannel`, unit tests in `android/app/src/test`, UI/device tests in `android/app/src/androidTest`, and resources in `android/app/src/main/res`.
- `ios/`: SwiftUI-based iOS prototype files and setup notes. See `ios/README.md` for Xcode wiring.

Supporting material lives in `docs/`. The top-level `app/` directory is an older Android mirror; prefer `android/` for new work.

## Build, Test, and Development Commands

- `.\gradlew build`: builds the root Java utility and runs its Gradle checks.
- `.\gradlew test`: runs root JVM tests from `src/test`.
- `cd android; .\gradlew assembleDebug`: builds the Android debug APK.
- `cd android; .\gradlew testDebugUnitTest`: runs Android unit tests.
- `cd android; .\gradlew connectedDebugAndroidTest`: runs Android instrumentation and Compose UI tests on a device/emulator.

The iOS app is developed from Xcode rather than a repo-level script. Open the files under `ios/` in an iOS app target and run on a physical device.

## Coding Style & Naming Conventions

Use standard language conventions with 4-space indentation. Keep Java and Kotlin classes in `PascalCase`, methods and properties in `camelCase`, and resource names in Android `snake_case` style where required. Match the existing package name `com.shahartal.myquietchannel` for Android code.

No formatter is enforced in the repo today. Detekt is referenced but commented out, so keep changes small and consistent with surrounding code.

## Testing Guidelines

Android unit tests use JUnit 4; instrumentation tests use AndroidX, Espresso, and Compose test APIs. Name tests after the subject under test, for example `VolumeCycleServiceTest.kt`. Add or update tests when changing scheduling, API parsing, or UI flows.

## Commit & Pull Request Guidelines

Recent commits are short and scope-first, for example `Android 1.41...`, `web. Fix race condition...`, and `claude /init...`. Follow that pattern: start with the area (`Android`, `web`, `ios`, `java`) and then a concise imperative summary.

Pull requests should include a clear description, linked issue if applicable, test evidence, and screenshots for UI or store-asset changes. Call out config-sensitive files such as `google-services.json` explicitly and avoid committing secrets.
