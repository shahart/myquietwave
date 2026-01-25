import AVFoundation
import MediaPlayer
import UIKit

class VolumeManager: ObservableObject {
    @Published var isCycling = false
    @Published var currentState: VolumeState = .muted
    @Published var timeRemaining: TimeInterval = 0
    
    private var timer: Timer?
    private var backgroundTaskID: UIBackgroundTaskIdentifier = .invalid
    
    enum VolumeState {
        case muted
        case playing
    }
    
    private let muteDuration: TimeInterval = 4 * 60 * 60 // 4 hours in seconds
    private let playDuration: TimeInterval = 3 * 60 // 3 minutes in seconds
    private let targetVolume: Float = 0.3 // 30% of maximum volume
    
    init() {
        setupAudioSession()
    }
    
    private func setupAudioSession() {
        do {
            let audioSession = AVAudioSession.sharedInstance()
            try audioSession.setCategory(.playback, mode: .default, options: [])
            try audioSession.setActive(true)
        } catch {
            print("Failed to setup audio session: \(error)")
        }
    }
    
    func startCycling() {
        guard !isCycling else { return }
        
        isCycling = true
        currentState = .muted
        timeRemaining = muteDuration
        
        // Set initial volume to muted
        setVolume(0.0)
        
        // Start the cycle
        scheduleNextStateChange()
        
        // Register for background task
        registerBackgroundTask()
    }
    
    func stopCycling() {
        isCycling = false
        timer?.invalidate()
        timer = nil
        endBackgroundTask()
    }
    
    private func scheduleNextStateChange() {
        timer?.invalidate()
        
        let duration = currentState == .muted ? muteDuration : playDuration
        
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            self?.updateTimer()
        }
        
        // Schedule the actual state change
        DispatchQueue.main.asyncAfter(deadline: .now() + duration) { [weak self] in
            self?.switchToNextState()
        }
    }
    
    private func updateTimer() {
        guard isCycling else { return }
        
        timeRemaining -= 1
        
        if timeRemaining <= 0 {
            timeRemaining = 0
        }
    }
    
    private func switchToNextState() {
        guard isCycling else { return }
        
        switch currentState {
        case .muted:
            // Switch to playing state
            currentState = .playing
            timeRemaining = playDuration
            setVolume(targetVolume)
            
        case .playing:
            // Switch to muted state
            currentState = .muted
            timeRemaining = muteDuration
            setVolume(0.0)
        }
        
        // Schedule next state change
        scheduleNextStateChange()
    }
    
    private func setVolume(_ volume: Float) {
        DispatchQueue.main.async {
            MPVolumeView.setVolume(volume)
        }
    }
    
    private func registerBackgroundTask() {
        backgroundTaskID = UIApplication.shared.beginBackgroundTask(withName: "VolumeCycling") { [weak self] in
            self?.endBackgroundTask()
        }
    }
    
    private func endBackgroundTask() {
        if backgroundTaskID != .invalid {
            UIApplication.shared.endBackgroundTask(backgroundTaskID)
            backgroundTaskID = .invalid
        }
    }
    
    func formatTime(_ timeInterval: TimeInterval) -> String {
        let hours = Int(timeInterval) / 3600
        let minutes = Int(timeInterval) % 3600 / 60
        let seconds = Int(timeInterval) % 60
        
        if hours > 0 {
            return String(format: "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            return String(format: "%02d:%02d", minutes, seconds)
        }
    }
}

// Extension to set volume programmatically
extension MPVolumeView {
    static func setVolume(_ volume: Float) {
        let volumeView = MPVolumeView()
        let slider = volumeView.subviews.first(where: { $0 is UISlider }) as? UISlider
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.01) {
            slider?.value = volume
        }
    }
}

