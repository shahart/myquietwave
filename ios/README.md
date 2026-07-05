# Volume Cycler iOS App -- Deprecated # 
## See new impl at myQuietChannel folder ##

An iOS app that automatically cycles media volume between muted and 30% volume according to a specific schedule.

## Features

- **4-hour mute cycles**: Mutes media volume for 4 hours
- **3-minute play cycles**: Increases volume to 30% for 3 minutes
- **Continuous cycling**: Repeats the cycle indefinitely
- **Background operation**: Continues running when app is in background
- **Real-time status**: Shows current state and time remaining
- **Modern UI**: Clean SwiftUI interface

## Requirements

- iOS 15.0 or later
- Xcode 14.0 or later
- iPhone device (volume control requires physical device)

## Setup Instructions

### 1. Create Xcode Project

1. Open Xcode
2. Create a new iOS App project
3. Choose "SwiftUI" as the interface
4. Set deployment target to iOS 15.0 or later

### 2. Add Files to Project

Add these files to your Xcode project:
- `App.swift`
- `ContentView.swift`
- `VolumeManager.swift`
- `SceneDelegate.swift`
- `Info.plist`
- `VolumeCycler.entitlements`

### 3. Configure Project Settings

1. **Bundle Identifier**: Set a unique bundle identifier
2. **Deployment Target**: iOS 15.0 or later
3. **Capabilities**: Enable "Background Modes" and add:
   - Background processing
   - Background fetch

### 4. Configure Entitlements

1. Go to your target's "Signing & Capabilities" tab
2. Add "Background Modes" capability
3. Check "Background processing" and "Background fetch"

### 5. Update Info.plist

Make sure your `Info.plist` includes:
- Background modes for background processing
- Proper bundle configuration

## Usage

1. **Launch the app** on your iPhone
2. **Tap "Start Cycling"** to begin the volume cycle
3. **Monitor status** - the app shows current state and time remaining
4. **Background operation** - the app continues cycling even when backgrounded

## How It Works

1. **Mute Phase**: Sets media volume to 0% for 4 hours
2. **Play Phase**: Sets media volume to 30% for 3 minutes
3. **Repeat**: Automatically cycles between these two phases

## Important Notes

- **Device Required**: This app must run on a physical iPhone device
- **Volume Control**: Uses `MPVolumeView` to control system media volume
- **Background Tasks**: Registers background tasks to continue operation
- **Audio Session**: Configures audio session for media playback

## Technical Details

- **SwiftUI**: Modern declarative UI framework
- **AVFoundation**: Audio session management
- **MediaPlayer**: Volume control via MPVolumeView
- **Background Tasks**: iOS background processing capabilities
- **Timer-based**: Precise timing for volume changes

## Troubleshooting

- **Volume not changing**: Ensure app is running on physical device
- **Background stops**: Check background app refresh is enabled
- **Audio issues**: Verify audio session permissions

## Privacy

This app only controls media volume and does not:
- Access microphone
- Record audio
- Access personal data
- Connect to internet

## License

This project is provided as-is for educational and personal use.

# New

What’s included:
- Native SwiftUI app target: MyQuietWaveiOS.xcodeproj
- Shared SwiftPM package for source + tests: Package.swift
- Radio station playback/scheduling with AVPlayer
- Continuous radio mode
- Upcoming-news schedule parsing
- Hebcal Shabbat/zmanim/daf-yomi API integration
- Location/station/settings persistence
- Android-style main screen, todo list, links, share action
- Unit tests for schedule parsing, Hebrew date utilities, and location conversion

Validation passed:
- `swift test` passed: 5 tests, 0 failures
- `xcodebuild -project MyQuietWaveiOS.xcodeproj -scheme MyQuietWaveiOS -destination "generic/platform=iOS Simulator" build` succeeded

Note: iOS does not allow an app to mute global system audio like the Android foreground service does. The migrated behavior uses in-app radio playback, timers, and notifications. A physical-device build will require selecting a Development Team in Xcode signing settings.

## Sanity tests ##

Done also on real device.

Passed

- AVPlayer works
- Timers

Failed

- Trim zero (05/07 -> 5/7)
- No seconds in current time
- Mevarchin link
- geo/gps. DONE remove from list (temp)
- volume changes "
- no UTC+03:00
- short locations list. TODO equalize
- no icon
- Dark theme


