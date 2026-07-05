import AVFoundation
import Combine
import Foundation
import UserNotifications

#if os(iOS)
@MainActor
public final class RadioScheduler: ObservableObject {
    public enum State: Equatable {
        case stopped
        case continuous
        case playingNews(until: Date)
        case waiting(next: Date?)
    }

    @Published public private(set) var state: State = .stopped
    @Published public private(set) var statusText = "My quiet wave 📻 Disabled"
    @Published public private(set) var countdownText = ""

    private var player: AVPlayer?
    private var timer: Timer?
    private var settings = AppSettings()

    public init() {}

    public func start(settings: AppSettings) {
        self.settings = settings
        configureAudioSession()
        requestNotifications()

        if settings.continuousRadio {
            play()
            state = .continuous
            statusText = "My quiet wave 📻 Enabled"
            scheduleNotification(title: "My quiet wave - In action", body: "Radio is playing continuously.", date: Date().addingTimeInterval(1))
        } else {
            beginNewsWindow()
        }
        startTimer()
    }

    public func stop() {
        timer?.invalidate()
        timer = nil
        player?.pause()
        player = nil
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
        state = .stopped
        statusText = "My quiet wave 📻 Disabled"
        countdownText = ""
    }

    private func startTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            Task { @MainActor in self?.tick() }
        }
    }

    private func tick() {
        switch state {
        case .playingNews(let until):
            if Date() >= until {
                player?.pause()
                let next = NewsSchedule(text: settings.nextHours).nextDate(after: Date())
                state = .waiting(next: next)
                if let next {
                    scheduleNotification(title: "My quiet wave - In action", body: "News audio is scheduled.", date: next)
                }
            }
            updateCountdown(until)
        case .waiting(let next):
            if let next, Date() >= next {
                beginNewsWindow()
            } else if let next {
                updateCountdown(next)
            } else {
                countdownText = "No upcoming news time"
            }
        case .continuous:
            countdownText = "Enjoy"
        case .stopped:
            countdownText = ""
        }
    }

    private func beginNewsWindow() {
        play()
        let duration = TimeInterval(max(1, min(59, settings.newsDurationMinutes)) * 60)
        let until = Date().addingTimeInterval(duration)
        state = .playingNews(until: until)
        statusText = "My quiet wave 📻 Enabled"
        scheduleNotification(title: "My quiet wave - In action", body: "Low audio for the upcoming \(settings.newsDurationMinutes) minutes.", date: Date().addingTimeInterval(1))
    }

    private func play() {
        let station = RadioStation.named(settings.stationName)
        if player == nil {
            player = AVPlayer(url: station.streamURL)
        }
        player?.play()
    }

    private func updateCountdown(_ date: Date) {
        let remaining = max(0, Int(date.timeIntervalSinceNow))
        let hours = remaining / 3600
        let minutes = (remaining % 3600) / 60
        let seconds = remaining % 60
        countdownText = hours > 0
            ? String(format: "%02d:%02d:%02d", hours, minutes, seconds)
            : String(format: "%02d:%02d", minutes, seconds)
    }

    private func configureAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            statusText = "Audio setup failed"
        }
    }

    private func requestNotifications() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound]) { _, _ in }
    }

    private func scheduleNotification(title: String, body: String, date: Date) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        let trigger = UNCalendarNotificationTrigger(dateMatching: Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: date), repeats: false)
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
    }
}
#endif
