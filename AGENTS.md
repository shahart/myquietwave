# Repository Guidelines

These instructions apply only to the current Android app in `android/` and the companion website in `docs/`. Ignore the iOS project and other repository areas when following this file.

## Project Structure

- `android/`: Android app. Main code is under `android/app/src/main/java/com/shahartal/myquietchannel`, unit tests under `android/app/src/test`, instrumentation tests under `android/app/src/androidTest`, and resources under `android/app/src/main/res`.
- `docs/`: companion website. TypeScript source is under `docs/ts`, tests under `docs/tests`, static assets and page markup are at the directory root, and compiled browser scripts are under `docs/dist`.

## Build, Test, and Development Commands

- From `android/`, run `./gradlew assembleDebug` to build the Android debug APK.
- From `android/`, run `./gradlew testDebugUnitTest` for Android unit tests.
- From `android/`, run `./gradlew connectedDebugAndroidTest` for device or emulator tests.
- From `docs/`, run `npm run build` to compile TypeScript into `dist/`.
- From `docs/`, run `npm test` to build and run the Node.js test suite.

## Coding Style

Use standard language conventions and match nearby code. Kotlin classes use `PascalCase`, methods and properties use `camelCase`, and Android resource names use `snake_case`. Keep changes focused and update generated `docs/dist` scripts when changing website TypeScript.

## Testing

Add or update Android JUnit 4 or instrumentation tests for behavior changes in the Android app. Add or update Node.js tests under `docs/tests` when changing website logic.

## Commit and Pull Request Guidelines

Use short, area-first commit subjects such as `Android: ...` or `web: ...`. Pull requests should describe the change, include relevant test evidence, and provide screenshots for UI changes. Call out config-sensitive files such as `google-services.json` and do not commit secrets.
